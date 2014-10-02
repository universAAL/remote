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
import java.util.Set;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ProxyBusMemberFactory;
import org.universAAL.ri.gateway.proxies.ProxyPool;
import org.universAAL.ri.gateway.proxies.updating.RegistrationParametersAdder;
import org.universAAL.ri.gateway.proxies.updating.RegistrationParametersRemover;
import org.universAAL.ri.gateway.proxies.updating.Updater;

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

    /**
     * Receives Gateway Level layer messages (aka {@link ImportMessage}
     * protocol), and manages the importer protocol for each case: <br>
     * 
     * <h3>handles Import-request protocol:<h3>
     * <img src="doc-files/Import-ImportRequest.png">
     * 
     * <h3>handles Import-remove protocol:<h3>
     * <img src="doc-files/Import-ImportRemove.png">
     * 
     * <h3>handles Import-refresh protocol:<h3>
     * <img src="doc-files/Import-ImportRefresh.png">
     * 
     * @param msg
     */
    public synchronized void handleImportMessage(final ImportMessage msg) {
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportRequest)) {
	    // request
	    if (session.getImportOperationChain().check(msg.getParameters())
		    .equals(OperationChain.OperationResult.ALLOW)) {
		// it is allowed
		// search for a compatible proxy
		ProxyBusMember pbm = pool.searchCompatible(msg.getParameters());
		if (pbm == null) {
		    // create a new one otherwise;
		    pbm = ProxyBusMemberFactory.createImport(msg
			    .getParameters());
		    if (pbm == null) {
			LogUtils.logError(
				Gateway.getInstance().context,
				getClass(),
				"handleImportMessage",
				"Unable to create import proxy for: "
					+ msg.getBusMemberId());
			session.send(ImportMessage.importResponse(msg, null));
			return;
		    }
		    pool.add(pbm);
		}
		// Associate remote proxy
		pbm.addRemoteProxyReference(new BusMemberReference(session, msg
			.getBusMemberId()));
		// send response
		session.send(ImportMessage.importResponse(msg,
			pbm.getBusMemberId()));

		// note import
		imports.put(msg.getBusMemberId(), pbm);
		LogUtils.logDebug(
			Gateway.getInstance().context,
			getClass(),
			"handleImportMessage",
			"Imported " + msg.getBusMemberId() + " from "
				+ session.getScope());
	    } else {
		// import denied
		session.send(ImportMessage.importResponse(msg, null));
	    }
	    return;
	}
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportRemove)) {
	    // remove
	    /*
	     * When remote importer sends importRemove, it will be received
	     * here. Maybe import security is changed and remote proxy is no
	     * longer allowed.
	     * 
	     * It has to tell the exporter to remove a reference not the
	     * exported proxy.
	     */
	    if (!Gateway.getInstance().getExporter()
		    .isRemoveExport(msg.getBusMemberId(), session)) {
		remove(msg.getBusMemberId());
	    }
	    return;
	}
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportAddSubscription)) {
	    // refresh add
	    checkAndRefresh(msg,
		    new RegistrationParametersAdder(msg.getParameters()));
	    return;

	}
	if (msg.getMessageType().equals(
		ImportMessage.ImportMessageType.ImportRemoveSubscription)) {
	    // refresh remove
	    checkAndRefresh(msg,
		    new RegistrationParametersRemover(msg.getParameters()));
	    return;

	}
    }

    /**
     * Checks the security for the new parameters and then Deals with accepted
     * Refresh requests (addSubscriptionParameters or
     * removeSubscriptionParameters).<br>
     * 
     * Responds to Import-refresh protocol: <br>
     * <img src="doc-files/Import-ImportRequest.png">
     * 
     * <br>
     * Where refresh is either add or remove registration parameters.
     * 
     * @param msg
     *            the add or remove subscription message.
     * @param up
     *            the updater that will add or remove the parameters.
     * @return the Message to respond
     */
    private Message checkAndRefresh(final ImportMessage msg, final Updater up) {
	final String remoteBusMemberId = msg.getBusMemberId();
	final ProxyBusMember local = imports.get(remoteBusMemberId);

	if (local != null) {
	    // it has been imported
	    // build the new parameters.
	    final Resource[] newParams = up.newParameters(local
		    .getSubscriptionParameters());

	    if (session.getImportOperationChain().check(newParams)
		    .equals(OperationChain.OperationResult.ALLOW)) {
		// it is allowed by security config.

		// newReference is the local ProxyBusMember that will hold the
		// new reference to the updated remote proxy.
		String newReference = null;

		if (local.getRemoteProxiesReferences().size() == 1) {
		    /*
		     * the local proxy has only one reference, we assume this is
		     * the reference of the remote proxy being updated,
		     * therefore the proxy can be directly updated.
		     */
		    up.update(local);
		    newReference = local.getBusMemberId();
		}

		/*
		 * Else, a new reference has to be provided. This reference can
		 * either be a new proxy or an existing compatible one. first
		 * remove the reference to the current import proxy (the
		 * parameters have been updated and are no longer compatible.
		 */
		remove(remoteBusMemberId);

		/*
		 * no need to check if proxy needs to be deleted, because we
		 * have already checked that the size of the references is more
		 * than 1, so deleting one reference will always leave at least
		 * 1.
		 */

		// search for a compatible proxy
		ProxyBusMember newPBM = pool.searchCompatible(newParams);
		if (newPBM == null) {
		    // create a new one otherwise;
		    newPBM = ProxyBusMemberFactory.createImport(newParams);
		    pool.add(newPBM);
		}
		newPBM.addRemoteProxyReference(new BusMemberReference(session,
			remoteBusMemberId));
		imports.put(remoteBusMemberId, newPBM);
		newReference = newPBM.getBusMemberId();
		return ImportMessage.importResponse(msg, newReference);
	    } else {
		// refresh denied
		remove(remoteBusMemberId);
		return ImportMessage.importResponse(msg, null);
	    }
	} else {
	    LogUtils.logWarn(
		    Gateway.getInstance().context,
		    getClass(),
		    "refresh",
		    "refresh requested but: "
			    + remoteBusMemberId
			    + " Proxy is not in the imported proxies for the session.");
	    return new ErrorMessage(
		    "Proxy is not in the imported proxies for the session.",
		    msg);
	}
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
	    LogUtils.logDebug(Gateway.getInstance().context, getClass(),
		    "remove", "removed Reference " + session.getScope()
			    + "from " + binded.getBusMemberId());
	    pool.removeProxyIfOrphan(binded);
	} else {
	    LogUtils.logWarn(
		    Gateway.getInstance().context,
		    getClass(),
		    "remove",
		    "Remove requested but: "
			    + remoteBusMemberId
			    + " Proxy is not in the imported proxies for the session.");
	}
    }

    /**
     * Recheck security method, to check imports when security policies may have
     * changed. Removes any imported {@link ProxyBusMember} that is no longer
     * allowed.
     */
    public void reCheckSecurity() {
	final Set<ProxyBusMember> checks = new HashSet<ProxyBusMember>(
		imports.values());
	for (final ProxyBusMember pbm : checks) {
	    if (session.getImportOperationChain()
		    .check(pbm.getSubscriptionParameters())
		    .equals(OperationChain.OperationResult.DENY)) {
		pool.removeProxyWithSend(pbm);
	    }
	}
    }

    /**
     * @return
     */
    public Collection<ProxyBusMember> getImports() {
	return imports.values();
    }
}
