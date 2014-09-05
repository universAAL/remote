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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCallee;
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

    private final Map<String, Resource[]> tracked;

    private final ProxyPool pool;

    /**
     *  
     */
    public Exporter(final ProxyPool pool) {
	this.pool = pool;
	tracked = new HashMap<String, Resource[]>();
	// busMembers = new HashMap<String, String>();
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
     * @param channel
     *            the channel to attempt exporting.
     */
    private void checkAndExport(final String busMemberId, final Session session) {
	final Resource[] params = tracked.get(busMemberId);
	// check exportOperationChain of the session
	if (session.getExportOperationChain().check(params)
		.equals(OperationChain.OperationResult.ALLOW)) {
	    // export
	    // get or create
	    ProxyBusMember exportProxy = pool.searchCompatible(params);
	    if (exportProxy == null) {
		exportProxy = ProxyBusMemberFactory.create(params);
	    }

	    // send ImportRequest, and wait for response
	    final Message resp = session.sendRequest(ImportMessage
		    .importRequest(exportProxy.getBusMemberId(),
			    exportProxy.getSubscriptionParameters()));
	    if (resp != null && resp instanceof ImportMessage
		    && ((ImportMessage) resp).isAccepted()) {
		// if response is positive associate
		exportProxy.addRemoteProxyReference(new BusMemberReference(
			session, ((ImportMessage) resp).getBusMemberId()));
	    }

	    if (!exportProxy.getRemoteProxiesReferences().isEmpty()) {
		// add to pool if it has associated remote proxies
		pool.add(exportProxy);
	    } else {
		exportProxy.close();
	    }
	}
    }

    /**
     * To be called when a new {@link Session} is created. Checks all the
     * possible {@link BusMember}s to be exported and checks for each if it
     * should be exported in the new session.
     * 
     * @param session
     */
    public void newSession(final Session session) {
	for (final String bmId : tracked.keySet()) {
	    checkAndExport(bmId, session);
	}
    }

    public void busMemberAdded(final BusMember member, final BusType type) {
	if (isExportable(member)) {
	    // mark as ready to receive params.
	    tracked.put(member.getURI(), null);
	}
    }

    /**
     * Invoked when a new BusMember is registered in the bus. <br>
     * 
     */
    private void newBusMember(final String busMemberId) {
	final Collection<Session> allSessions = Gateway.getInstance()
		.getSessions();
	for (final Session s : allSessions) {
	    // for each session attempt exporting
	    checkAndExport(busMemberId, s);
	}
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
	return isForExport(member) && !(member instanceof ProxyBusMember);
	// and they are not imported proxies!
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
	    final ProxyBusMember pbm = pool.get(bmId);
	    tracked.remove(bmId);
	    pool.removeProxyWithSend(pbm);
	}
    }

    /** {@inheritDoc} */
    public void regParamsAdded(final String busMemberID, final Resource[] params) {

	final Resource[] currentParams = tracked.get(busMemberID);

	if (tracked.containsKey(busMemberID) && currentParams == null) {
	    // a virgin busmember has registered, ie a newBusMember!
	    tracked.put(busMemberID, params);
	    newBusMember(busMemberID);
	} else {
	    refresh(busMemberID, new Updater() {

		public void update(final ProxyBusMember pbm) {
		    pbm.addSubscriptionParameters(params);
		}

		public ImportMessage createMessage() {
		    return ImportMessage.importAddSubscription(busMemberID,
			    params);
		}
	    });
	}
    }

    /** {@inheritDoc} */
    public void regParamsRemoved(final String busMemberID,
	    final Resource[] params) {
	refresh(busMemberID, new Updater() {

	    public void update(final ProxyBusMember pbm) {
		pbm.removeSubscriptionParameters(params);
	    }

	    public ImportMessage createMessage() {
		return ImportMessage.importRemoveSubscription(busMemberID,
			params);
	    }
	});
    }

    private interface Updater {
	void update(ProxyBusMember pbm);

	ImportMessage createMessage();
    }

    /**
     * Called when registration parameters change for an exported proxy.<br>
     * 
     * Initiates Import-refresh protocol: <br>
     * <img src="doc-files/Import-ImportRefresh.png">
     * 
     * @param busMemberID
     * @param orgigParams
     */
    private void refresh(final String busMemberID, final Updater up) {
	// locate local proxy representative in pool
	final ProxyBusMember pbm = pool.get(busMemberID);
	if (pbm != null) {
	    // update proxy registrations
	    up.update(pbm);
	    // update the tracked record
	    tracked.put(busMemberID, pbm.getSubscriptionParameters());
	    final Collection<BusMemberReference> refs = pbm
		    .getRemoteProxiesReferences();
	    final HashSet<BusMemberReference> toBeRemoved = new HashSet<BusMemberReference>(
		    refs);
	    final HashSet<BusMemberReference> toBeAdded = new HashSet<BusMemberReference>();
	    // Send refresh message per channel.
	    for (final BusMemberReference bmr : refs) {
		final Session s = bmr.getChannel();
		// check the new parameters are allowed to be exported
		if (s.getExportOperationChain()
			.check(pbm.getSubscriptionParameters())
			.equals(OperationChain.OperationResult.ALLOW)) {
		    final Message resp = s.sendRequest(up.createMessage());
		    if (resp != null && resp instanceof ImportMessage
			    && ((ImportMessage) resp).isAccepted()) {
			toBeAdded.add(new BusMemberReference(s,
				((ImportMessage) resp).getBusMemberId()));
		    }
		} else {
		    // new parameters are not allowed, send remove
		    s.send(ImportMessage.importRemove(busMemberID));
		}
	    }
	    // update all references
	    for (final BusMemberReference bm : toBeRemoved) {
		pbm.removeRemoteProxyReference(bm);
	    }
	    for (final BusMemberReference bm : toBeAdded) {
		pbm.addRemoteProxyReference(bm);
	    }
	}
    }

    public boolean isRemoveExport(final String busMemberId,
	    final Session session) {
	final ProxyBusMember member = pool.get(busMemberId);
	if (isForExport(member)) {

	    member.removeRemoteProxyReferences(session);
	    if (member.getRemoteProxiesReferences().isEmpty()) {
		pool.removeProxyWithSend(member);
	    }
	    return true;
	}
	return false;
    }

    public static boolean isForExport(final Object member) {
	return member != null
		&& (member instanceof ServiceCallee || member instanceof ContextSubscriber);
    }
}
