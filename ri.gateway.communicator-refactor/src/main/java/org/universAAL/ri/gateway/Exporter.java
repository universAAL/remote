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
import java.util.List;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.MessageSender;
import org.universAAL.ri.gateway.proxies.BusMemberIdentifier;
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

    // private Map<String, ProxyBusMember> exported;

    private List<BusMember> tracked;

    private ProxyPool pool;

    /**
     *  
     */
    public Exporter() {
	// TODO Auto-generated constructor stub
    }

    /**
     * Checks if the {@link ProxyBusMember} can be exported. Is so, sends the
     * import request message, and waits for response, if the import is
     * accepted, then the proxy will be associated to the remote proxy in the
     * response.
     * 
     * @param proxy
     *            the proxy to export.
     * @param channel
     *            the channel to attempt exporting.
     */
    private void checkAndExport(final ProxyBusMember proxy,
	    final Session session) {
	// check exportOperationChain of the session
	if (session.getExportOperationChain().canBeExported(proxy)
		.equals(OperationChain.OperationResult.ALLOW)) {
	    // export
	    // send ImportRequest, and wait for response
	    final Message resp = session.send(ImportMessage.importRequest(
		    proxy.getBusMemberId(), proxy.getSubscriptionParameters()));
	    if (resp != null && resp instanceof ImportMessage
		    && ((ImportMessage) resp).isAccepted()) {
		// if response is positive associate
		proxy.addRemoteProxyReference(new BusMemberIdentifier(session,
			((ImportMessage) resp).getBusMemberId()));
	    }

	}
    }

    /**
     * For a given {@link BusMember} searches for an existing compatible Proxy,
     * or creates a new one.
     * 
     * @param bm
     * @return never null.
     */
    private ProxyBusMember getOrCreate(final BusMember bm) {
	final ProxyBusMember test = ProxyBusMemberFactory.create(bm);
	/*
	 * Initially, exports are not merged.
	 */
	// final ProxyBusMember existing = pool.searchCompatible(test);
	// if (existing != null) {
	// return existing;
	// } else {
	return test;
	// }
    }

    /**
     * To be called when a new {@link Session} is created. Checks all the
     * possible {@link BusMember}s to be exported and checks for each if it
     * should be exported in the new session.
     * 
     * @param session
     */
    public void newSession(final Session session) {
	for (final BusMember bm : tracked) {
	    // get, or create proxy
	    final ProxyBusMember proxy = getOrCreate(bm);

	    checkAndExport(proxy, session);
	    addToPool(proxy);
	}
    }

    private void addToPool(final ProxyBusMember proxy) {
	if (!proxy.getRemoteProxiesReferences().isEmpty()) {
	    // add to pool if it has associated remote proxies
	    pool.add(proxy);
	} else {
	    proxy.close();
	}
    }

    /** {@inheritDoc} */
    public void busMemberAdded(final BusMember member, final BusType type) {
	if (isExportable(member)) {
	    tracked.add(member);
	    // create proxy
	    final ProxyBusMember proxy = getOrCreate(member);
	    // get all sessions
	    final Collection<Session> allSessions = Gateway.getInstance()
		    .getSessions();
	    for (final Session s : allSessions) {
		// for each session attempt exporting
		checkAndExport(proxy, s);
	    }
	    addToPool(proxy);
	}
    }

    /**
     * Checks the {@link BusMember} to see if it can be exported, i.e: it is
     * either a {@link ServiceCallee} or a {@link ContextPublisher}. No specific
     * security checks are done.
     * 
     * @param member
     * @return
     */
    private boolean isExportable(final BusMember member) {
	// XXX in future add general security checks.
	return member instanceof ServiceCallee
		|| member instanceof ContextPublisher;
    }

    public void busMemberRemoved(final BusMember member, final BusType type) {
	if (isExportable(member)) {
	    // get proxy representative
	    final ProxyBusMember pbm = pool.get(member.getURI());
	    pool.removeProxyWithSend(pbm);
	}
    }

    /*
     * TODO a receiver method when importer sends importRemove, maybe import
     * security is changed and remote proxy is no longer allowed. maybe it is
     * directly delivered to pool.
     */

    public void regParamsAdded(final String busMemberID, final Resource[] params) {
	refresh(busMemberID, params);
    }

    public void regParamsRemoved(final String busMemberID,
	    final Resource[] params) {
	refresh(busMemberID, params);
    }

    private void refresh(final String busMemberID, final Resource[] orgigParams) {
	// TODO
	// locate local proxy representative in pool
	final ProxyBusMember pbm = pool.get(busMemberID);
	if (pbm != null) {
	    // update proxy registrations
	    pbm.update(orgigParams);
	    final Collection<BusMemberIdentifier> refs = pbm
		    .getRemoteProxiesReferences();
	    final HashSet<BusMemberIdentifier> toBeRemoved = new HashSet<BusMemberIdentifier>(
		    refs);
	    final HashSet<BusMemberIdentifier> toBeAdded = new HashSet<BusMemberIdentifier>();
	    // Send refresh message per channel.
	    for (final BusMemberIdentifier bmId : refs) {
		final MessageSender s = bmId.getChannel();
		// check the new parameters are allowed to be exported
		if (((Session) s).getExportOperationChain().canBeExported(pbm)
			.equals(OperationChain.OperationResult.ALLOW)) {
		    final Message resp = s.send(ImportMessage.importRefresh(
			    busMemberID, orgigParams));
		    if (resp != null && resp instanceof ImportMessage
			    && ((ImportMessage) resp).isAccepted()) {
			toBeAdded.add(new BusMemberIdentifier(s,
				((ImportMessage) resp).getBusMemberId()));
		    }
		} else {
		    // new parameters are not allowed, send remove

		}
	    }
	    // update all references
	    for (final BusMemberIdentifier bm : toBeRemoved) {
		pbm.removeRemoteProxyReference(bm);
	    }
	    for (final BusMemberIdentifier bm : toBeAdded) {
		pbm.addRemoteProxyReference(bm);
	    }
	}
    }
}
