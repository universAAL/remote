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

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.proxies.BusMemberIdentifier;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ProxyBusMemberFactory;
import org.universAAL.ri.gateway.proxies.ProxyPool;

/**
 * In charge of interpreting import requests coming from remote peer, and
 * associating a local proxy to the request. <br>
 * There should be an instance of {@link Importer} per {@link Session}; import
 * operations are restricted to each session.
 * 
 * @author amedrano
 * 
 */
public class Importer {

    /**
     * Reference to the {@link Session} this Imported is linked to.
     */
    private final Session session;

    /**
     * The proxyPool where all the proxies are.
     */
    private final ProxyPool pool;

    /**
     * A map of imports for the session, Maps remoteProxyBusMemberId to local
     * {@link ProxyBusMember}.
     */
    private final Map<String, ProxyBusMember> imports;

    /**
     * @param session
     * @param pool
     */
    public Importer(final Session session, final ProxyPool pool) {
	super();
	this.session = session;
	this.pool = pool;
	imports = new HashMap<String, ProxyBusMember>();
    }

    public synchronized void handleImportMessage(final ImportMessage msg) {
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportRequest)) {
	    // request
	    if (session.getImportOperationChain().canBeImported(msg)
		    .equals(OperationChain.OperationResult.ALLOW)) {
		// it is allowed
		// search for a compatible proxy
		ProxyBusMember pbm = pool.searchCompatible(msg.getParameters());
		if (pbm == null) {
		    // create a new one otherwise;
		    pbm = ProxyBusMemberFactory.create(msg.getParameters());
		    pool.add(pbm);
		}
		// Associate remote proxy
		pbm.addRemoteProxyReference(new BusMemberIdentifier(session,
			msg.getBusMemberId()));
		// send response
		session.Send(ImportMessage.importResponse(msg,
			pbm.getBusMemberId()));
		// note import
		imports.put(msg.getBusMemberId(), pbm);
	    } else {
		// import denied
		session.Send(ImportMessage.importResponse(msg, null));
	    }
	}
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportRemove)) {
	    // remove
	    remove(msg.getBusMemberId());

	}
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportRefresh)) {
	    // refresh
	    if (session.getImportOperationChain().canBeImported(msg)
		    .equals(OperationChain.OperationResult.ALLOW)) {
		session.send(ImportMessage.importResponse(msg,
			refresh(msg.getBusMemberId(), msg.getParameters())));
	    } else {
		// refresh denied
		session.Send(ImportMessage.importResponse(msg, null));
		remove(msg.getBusMemberId());
	    }
	}
    }

    private String refresh(final String remoteBusMemberId,
	    final Resource[] newParameters) {
	final ProxyBusMember local = imports.get(remoteBusMemberId);
	if (local != null) {
	    if (local.getRemoteProxiesReferences().size() == 1) {
		/*
		 * the local proxy has only one reference, we assume this is the
		 * reference of the remote proxy being updated, therefore the
		 * proxy can be directly updated.
		 */
		local.update(newParameters);
		return local.getBusMemberId();
	    }
	    /*
	     * first remove the reference to the current import proxy (the
	     * parameters have been updated and are no longer compatible.
	     */
	    remove(remoteBusMemberId);

	    // search for a compatible proxy
	    ProxyBusMember newPBM = pool.searchCompatible(newParameters);
	    if (newPBM == null) {
		// create a new one otherwise;
		newPBM = ProxyBusMemberFactory.create(newParameters);
		pool.add(newPBM);
	    }
	    newPBM.addRemoteProxyReference(new BusMemberIdentifier(session,
		    remoteBusMemberId));
	    imports.put(remoteBusMemberId, newPBM);

	} else {
	    LogUtils.logWarn(
		    Gateway.getInstance().context,
		    getClass(),
		    "refresh",
		    "refresh requested but: "
			    + remoteBusMemberId
			    + " Proxy is not in the exported proxies for the session.");
	}
	return null;
    }

    /**
     * Remove a reference to the remoteBusMemberId.
     * 
     * @param remoteBusMemberId
     */
    private void remove(final String remoteBusMemberId) {
	final ProxyBusMember binded = imports.get(remoteBusMemberId);
	if (binded != null) {
	    binded.removeRemoteProxyReferences(session);
	    imports.remove(remoteBusMemberId);
	    if (binded.getRemoteProxiesReferences().isEmpty()) {
		pool.removeProxyWithSend(binded);
	    }
	} else {
	    LogUtils.logWarn(
		    Gateway.getInstance().context,
		    getClass(),
		    "remove",
		    "Remove requested but: "
			    + remoteBusMemberId
			    + " Proxy is not in the exported proxies for the session.");
	}
    }

}
