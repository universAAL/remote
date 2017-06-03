/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ProxyBusMemberFactory;
import org.universAAL.ri.gateway.proxies.ProxyPool;
import org.universAAL.ri.gateway.proxies.updating.RegistrationParametersAdder;
import org.universAAL.ri.gateway.proxies.updating.RegistrationParametersRemover;
import org.universAAL.ri.gateway.proxies.updating.Updater;
import org.universAAL.ri.gateway.utils.ArraySet;

/**
 * Tracks {@link BusMember} through the {@link IBusMemberRegistry}, when a new
 * {@link BusMember} is registered it is checked if it should be exported,
 * through the exporting operation chain, and if it should then it sends all
 * active sessions an import request. <br>
 * When a new session is created, it has to be notified to the exporter to send
 * the new session all the issued import requests. <br>
 * There should only be one {@link Exporter} per ASG.
 * 
 * @author amedrano
 * @TODO adapt to new tracker interface
 */
public class Exporter implements IBusMemberRegistryListener {

    /**
     * Track all busMembers that register to the buses to their actual
     * registration parameters.
     */
    private final Map<String, Resource[]> tracked;

    /**
     * Pool of Proxies.
     */
    private final ProxyPool pool;

    /**
     * Map of Exported BusMemberURIs to Proxies. Several BusMembers can be
     * represented by the same Proxy.
     */
    private final Map<String, ProxyBusMember> exported;

    /**
     * Executor to execute concurrent tasks in order.
     */
    private final ExecutorService executor;

    /**
     * Main Constructor.
     * 
     * @param pool
     *            the pool of proxies to use.
     */
    public Exporter(final ProxyPool pool) {
	this.pool = pool;
	tracked = new ConcurrentHashMap<String, Resource[]>();
	exported = new ConcurrentHashMap<String, ProxyBusMember>();
	executor = Executors.newSingleThreadExecutor();
    }

    private abstract class ExporterTask implements Runnable {

	protected int remaining_attempts = 3;

	protected void reattempt() {
	    remaining_attempts--;
	    if (remaining_attempts <= 0) {
		// if attempts over limit
		executor.execute(this);
		LogUtils.logInfo(Gateway.getInstance().context, getClass(),
			"reattempt", "queing for reattempt: " + this + " , "
				+ Integer.toString(remaining_attempts)
				+ " attempts remaining.");
	    } else {
		LogUtils.logError(Gateway.getInstance().context, getClass(),
			"reattempt", "exceeded reattempt limit: " + this);
	    }
	    Thread.currentThread().interrupt();
	    return;
	}
    }

    /**
     * Checks if a proxy with the given params can be created and exported. If
     * so, sends the import request message, and waits for response, if the
     * import is accepted, then the proxy will be associated to the remote proxy
     * in the response. <br>
     * 
     * Initiates Import-request protocol: <br>
     * <img src="doc-files/Import-ImportRequest.png">
     * 
     * @param busMemberId
     *            the id of the busMember to attempt export.
     * @param session
     *            the channel to attempt exporting.
     */
    private class ExportTask extends ExporterTask implements Runnable {

	private String busMemberId;
	private Session session;

	ExportTask(final String busMemberId, final Session session) {
	    this.busMemberId = busMemberId;
	    this.session = session;
	    // TODO configure reAttempts from session config
	    // this.remaining_attempts =
	    // this.session.getConfiguration().getAttempts();
	}

	public void run() {
	    if (!session.isActive()) {
		LogUtils.logInfo(Gateway.getInstance().context, getClass(),
			"run", "Session is no longer active, cannot export: "
				+ session.getScope() + ", " + busMemberId);
		return;
	    }

	    final Resource[] params = tracked.get(busMemberId);
	    // check exportOperationChain of the session
	    if (session.getExportOperationChain().check(params)
		    .equals(OperationChain.OperationResult.ALLOW)) {
		// export
		// get or create
		ProxyBusMember exportProxy = pool.searchCompatible(params);
		if (exportProxy == null) {
		    exportProxy = ProxyBusMemberFactory.createExport(params,
			    busMemberId);
		    if (exportProxy == null) {
			LogUtils.logWarn(Gateway.getInstance().context,
				getClass(), "run",
				"could not find nor create a export proxy with the given parameters for "
					+ busMemberId);
			reattempt();
			return;
		    }
		}

		LogUtils.logDebug(Gateway.getInstance().context, getClass(),
			"run", "Requesting import " + busMemberId + " in "
				+ session.getScope());

		// send ImportRequest, and wait for response
		Message resp = null;
		try {
		    resp = session.sendRequest(ImportMessage.importRequest(
			    exportProxy.getBusMemberId(), params));
		} catch (TimeoutException e) {
		    exportProxy.close();
		    reattempt();
		}

		// annotate export map
		exported.put(busMemberId, exportProxy);

		if (resp != null && resp instanceof ImportMessage
			&& ((ImportMessage) resp).isAccepted()) {
		    // if response is positive associate
		    exportProxy.addRemoteProxyReference(new BusMemberReference(
			    session, ((ImportMessage) resp).getBusMemberId()));
		    LogUtils.logDebug(Gateway.getInstance().context,
			    getClass(), "run", "Exported " + busMemberId
				    + " to " + session.getScope());
		}

		if (!exportProxy.getRemoteProxiesReferences().isEmpty()) {
		    // add to pool if it has associated remote proxies
		    pool.add(exportProxy);
		} else {
		    // either the remote reject it, or something went wrong
		    LogUtils.logDebug(Gateway.getInstance().context,
			    getClass(), "run", "Discarding created proxy: "
				    + exportProxy.getBusMemberId());
		    exportProxy.close();
		}
	    } else {
		LogUtils.logInfo(Gateway.getInstance().context, getClass(),
			"run",
			"Bus member can not be exported due to security constrains: "
				+ busMemberId);
	    }
	}
    }

    /**
     * A task to handle Refresh task per {@link BusMemberReference}. It checks
     * if the new parameters are allowed by remote {@link Session}, if not
     * remote {@link BusMemberReference} is removed from {@link ProxyBusMember}.
     * If it is allowed remote importer will be notified about the update,
     * Remote may deny new parameters, or something in the communication is
     * wrong; then {@link BusMemberReference} is removed. If Local parameters
     * are allowed and accepted by remote, then local {@link ProxyBusMember} is
     * updated (via remove then add BMR). Finally the task checks if the
     * {@link ProxyBusMember} is still connected, and recycles ioc.
     * 
     * @author amedrano
     *
     */
    private class RefresSubTask extends ExporterTask implements Runnable {

	ProxyBusMember pbm;
	BusMemberReference bmr;
	Updater up;
	String busMemberID;

	/**
	 * @param busMemberID
	 * @param pbm
	 * @param bmr
	 */
	public RefresSubTask(String busMemberID, ProxyBusMember pbm,
		BusMemberReference bmr, Updater up) {
	    super();
	    this.busMemberID = busMemberID;
	    this.pbm = pbm;
	    this.bmr = bmr;
	    this.up = up;
	}

	/** {@ inheritDoc} */
	public void run() {
	    final Session s = bmr.getChannel();
	    if (!s.isActive()) {
		LogUtils.logInfo(Gateway.getInstance().context, getClass(),
			"run", "Session no longer active, aborting refresh: "
				+ s.getScope() + ", " + busMemberID);
		return;
	    }
	    // check the new parameters are allowed to be exported
	    if (s.getExportOperationChain().check(tracked.get(busMemberID))
		    .equals(OperationChain.OperationResult.ALLOW)) {
		Message resp = null;
		try {
		    resp = s.sendRequest(up.createExportMessage(pbm
			    .getBusMemberId()));
		} catch (TimeoutException e) {
		    reattempt();
		    return;
		}
		if (resp != null && resp instanceof ImportMessage
			&& ((ImportMessage) resp).isAccepted()) {
		    // toBeAdded.add(new BusMemberReference(s,
		    // ((ImportMessage) resp).getBusMemberId()));
		    pbm.removeRemoteProxyReference(bmr);
		    pbm.addRemoteProxyReference(bmr);
		} else {
		    // Either Update Rejected from remote or something
		    // went wrong
		    pbm.removeRemoteProxyReference(bmr);
		    s.send(ImportMessage.importRemove(busMemberID));
		    LogUtils.logWarn(
			    Gateway.getInstance().context,
			    getClass(),
			    "run",
			    "The new parameters of proxy: "
				    + pbm.getBusMemberId()
				    + " could not be updated remotely for scope: "
				    + s.getScope());
		}
	    } else {
		// new parameters are not allowed, remove
		pbm.removeRemoteProxyReference(bmr);
		LogUtils.logWarn(Gateway.getInstance().context, getClass(),
			"refresh",
			"new parameters are not allowed locally, sending remove.");
		// and send remove message
		s.send(ImportMessage.importRemove(busMemberID));
	    }
	    // Check the pbm is still connected
	    if (pbm.getRemoteProxiesReferences().isEmpty()) {
		// if not recycle
		LogUtils.logDebug(Gateway.getInstance().context, getClass(),
			"refresh",
			"Proxy has no references after refresh, deleting proxy: "
				+ pbm.getBusMemberId());
		pool.removeProxyWithSend(pbm); // XXX it doesn't really matter
					       // there are any remotes to send
					       // the message to
		exported.remove(busMemberID);
	    }
	}

    }

    /**
     * Executed when registration parameters change for an exported proxy.<br>
     * 
     * Initiates Import-refresh protocol: <br>
     * <img src="doc-files/Import-ImportRefresh.png"> <br>
     * Where refresh is either add or remove registration parameters.
     * 
     */
    private class RefreshTask implements Runnable {

	private String busMemberID;
	private Updater up;

	RefreshTask(final String busMemberID, final Updater up) {
	    this.busMemberID = busMemberID;
	    this.up = up;
	}

	public void run() {
	    // locate exported proxy representative
	    final ProxyBusMember pbm = exported.get(busMemberID);
	    if (pbm != null) {
		LogUtils.logDebug(
			Gateway.getInstance().context,
			getClass(),
			"refresh",
			"Local BusMember for proxy has changed parameters,"
				+ " updating proxy (or creating new if shared), "
				+ "informing remote about parameter change.");
		// update proxy registrations
		up.update(pbm);
		// up.newParameters(tracked.get(busMemberID)));
		final Collection<BusMemberReference> refs = pbm
			.getRemoteProxiesReferences();
		// Send refresh message per channel.
		for (final BusMemberReference bmr : refs) {
		    // add new task per BMR
		    executor.execute(new RefresSubTask(busMemberID, pbm, bmr,
			    up));
		}
	    } else {
		/*
		 * busmember is not exported, maybe it used to be exported then
		 * it was updated to no longer be exported, and now it is being
		 * once more updated and export may be possible: attempt export
		 */
		// check all sessions
		final Collection<Session> allSessions = Gateway.getInstance()
			.getSessions();
		for (final Session s : allSessions) {
		    if (s.isActive()) {
			// for each session attempt exporting
			executor.execute(new ExportTask(busMemberID, s));
		    }
		}
	    }
	}
    }

    private class SessionStopTask implements Runnable {

	Session session;

	/**
	 * @param session
	 *            the {@link Session} to check for removal.
	 */
	public SessionStopTask(Session session) {
	    super();
	    this.session = session;
	}

	public void run() {
	    final Collection<Entry<String, ProxyBusMember>> ex = exported
		    .entrySet();
	    Set<String> tbr = new HashSet<String>();
	    for (final Entry<String, ProxyBusMember> entry : ex) {
		final ProxyBusMember pbm = entry.getValue();
		Collection<BusMemberReference> refs = pbm.getRemoteProxiesReferences();
		for (BusMemberReference bmr : refs) {
		    if (bmr.getChannel().equals(session) 
			    && session.isActive()) {
			// Send remove message to remote
			session.send(ImportMessage.importRemove(bmr
				.getBusMemberid()));
		    }
		}
		pbm.removeRemoteProxyReferences(session);
		if (pool.removeProxyIfOrphan(pbm)) {
		    tbr.add(entry.getKey());
		}
	    }
	    for (String id : tbr) {
		exported.remove(id);
	    }

	}

    }

    /**
     * To be called when a new {@link Session} is become active. Checks all the
     * possible {@link BusMember}s to be exported and checks for each if it
     * should be exported in the new session.
     * 
     * @param session
     */
    public void activatedSession(final Session session) {
	for (final String bmId : tracked.keySet()) {
	    executor.execute(new ExportTask(bmId, session));
	}
    }

    /**
     * To be called when a Session is about to be closed, or is disconnected.
     * Checks all the exported proxies and removes all references, stops them if
     * required. <br>
     * Does not send remove-requests to peers, {@link Importer} in peers will be
     * {@link Importer#reset() reseted} as soon as the connection is lost.
     * 
     * @param session
     */
    public void stopedSession(final Session session) {
	executor.execute(new SessionStopTask(session));
    }

    /**
     * Checks the {@link BusMember} to see if it can be exported, i.e: it is the
     * correct type of BusMember. No specific security checks are done.
     * 
     * @param member
     * @return
     */
    private boolean isExportable(final BusMember member) {
	// XXX in future add general security checks.
	return !(member instanceof ProxyBusMember)
		&& ProxyBusMemberFactory.isForExport(member);
	// and they are not imported proxies!
    }

    public void busMemberAdded(final BusMember member, final BusType type) {
	if (isExportable(member)) {
	    // mark as ready to receive params.
	    // TODO check for errors: is this really the first time the
	    // busMember is added?
	    if (tracked.containsKey(member.getURI())) {
		LogUtils.logInfo(
			Gateway.getInstance().context,
			getClass(),
			"busMemberAdded",
			"Bus member already added, ignoring: "
				+ member.getURI());
		return;
	    }
	    tracked.put(member.getURI(), null);
	    Resource[] initParams = ProxyBusMemberFactory
		    .initialParameters(member);
	    if (initParams != null) {
		regParamsAdded(member.getURI(), initParams);
	    }
	}
    }

    /**
     * Invoked when an existing BusMember is unregistered from the bus.<br>
     * 
     * Initiates Import-remove protocol: <br>
     * <img src="doc-files/Import-ImportRemove.png">
     */
    public void busMemberRemoved(final BusMember member, final BusType type) {
	final String bmId = member.getURI();
	if (tracked.containsKey(bmId)) {
	    // get proxy representative
	    final ProxyBusMember pbm = exported.get(bmId);
	    tracked.remove(bmId);
	    exported.remove(bmId);
	    if (pbm == null) {
		return;
	    }
	    // if there are no left bus members that use this export proxy
	    if (!exported.values().contains(pbm)) {
		// then remove proxy.
		LogUtils.logDebug(Gateway.getInstance().context, getClass(),
			"busMemberRemoved",
			"All local BusMember for proxy have been un registered, removing proxy");
		pool.removeProxyWithSend(pbm);
	    }
	}
    }

    /** {@inheritDoc} */
    public void regParamsAdded(final String busMemberID, final Resource[] params) {

	final Resource[] currentParams = tracked.get(busMemberID);

	boolean isContained = tracked.containsKey(busMemberID);

	if (isContained && currentParams == null) {
	    // a virgin bus member has registered, ie a newBusMember!
	    tracked.put(busMemberID, params);
	    // check all sessions
	    final Collection<Session> allSessions = Gateway.getInstance()
		    .getSessions();
	    for (final Session s : allSessions) {
		if (s.isActive()) {
		    // for each session attempt exporting
		    executor.execute(new ExportTask(busMemberID, s));
		}
	    }
	} else if (isContained && currentParams != null) {
	    tracked.put(busMemberID, new ArraySet.Union<Resource>().combine(
		    currentParams, params, new Resource[] {}));
	    executor.execute(new RefreshTask(busMemberID,
		    new RegistrationParametersAdder(params)));
	} else {
	    // else -> a notification from a non exportable bus member ->
	    // ignore.
	    LogUtils.logDebug(Gateway.getInstance().context, getClass(),
		    "regParamsAdded", "Local non-exportable BusMember: "
			    + busMemberID + " has changed parameters.");
	}
    }

    /** {@inheritDoc} */
    public void regParamsRemoved(final String busMemberID,
	    final Resource[] params) {
	/*
	 * TODO check if new params of the BusMember is [], Then ???
	 */

	tracked.put(busMemberID, new ArraySet.Union<Resource>().combine(
		tracked.get(busMemberID), params, new Resource[] {}));
	executor.execute(new RefreshTask(busMemberID,
		new RegistrationParametersRemover(params)));
    }

    /**
     * Called to check (and handle) if the remote {@link Importer} is sending a
     * remove request.
     * 
     * @param busMemberId
     * @param session
     * @return
     */
    public boolean isRemoveExport(final String busMemberId,
	    final Session session) {
	final ProxyBusMember member = exported.get(busMemberId);
	if (member != null) {
	    member.removeRemoteProxyReferences(session);
	    if (pool.removeProxyIfOrphan(member)) {
		exported.remove(busMemberId);
	    }
	    return true;
	}
	return false;
    }

    /**
     * 
     * Stop all operations.
     */

    public void stop() {
	// Graceful termination of pending tasks
	// specially waiting for session closure tasks
	try {
	    while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
		LogUtils.logInfo(Gateway.getInstance().context, getClass(),
			"stop",
			"Timeout waiting for session end operations, waiting some more.");
	    }
	} catch (InterruptedException e) {
	    stop();
	    return;
	}

	final Collection<ProxyBusMember> ex = exported.values();
	exported.clear();
	for (final ProxyBusMember pbm : ex) {
	    pool.removeProxyWithSend(pbm);
	}
	tracked.clear();
    }

    /**
     * Check whether the URI corresponds to a local {@link BusMember} (excluding
     * {@link ProxyBusMember}s).
     * 
     * @param bmURI
     *            the String containing the URI of the BusMember to check.
     * @return true iif there is a local {@link BusMember} that corresponds to
     *         bmURI.
     */
    public boolean isTracked(String bmURI) {
	return tracked.containsKey(bmURI);
    }
}
