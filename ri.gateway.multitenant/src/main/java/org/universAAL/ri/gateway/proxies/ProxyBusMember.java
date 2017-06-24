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
package org.universAAL.ri.gateway.proxies;

import java.util.Collection;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.protocol.ImportMessage.ImportMessageType;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;

/**
 * Generic interface for any proxy representing any imported or exported
 * {@link BusMember}, independently of the bus they associate to.
 *
 * @author amedrano
 *
 */
public interface ProxyBusMember {

	/**
	 * Get the busmemberId. <br>
	 * For imported proxies this ID of the internal busMember of the proxy. <br>
	 * For exported proxies this ID is of the represented {@link BusMember}.
	 *
	 * @return
	 */
	String getBusMemberId();

	/**
	 * Add a {@link BusMemberReference} to the proxy. This means that from this
	 * moment on the remote proxy and the local proxy can (and should)
	 * communicate with each other. <br>
	 *
	 * A proxy may have more than one remote reference, each time the local
	 * proxy is to send a message it should send it to the whole list of remote
	 * proxies (list of references), provided for each the outgoing security
	 * checks per message is passed.
	 *
	 * @param remoteReference
	 *            The reference to a remote proxy, its busmemberid and the
	 *            session through which to find it.
	 */
	void addRemoteProxyReference(BusMemberReference remoteReference);

	/**
	 * Remove a concrete remote proxy. Does not send
	 * {@link ImportMessageType#ImportRemove Remove message}
	 *
	 * @param remoteReference
	 *            the remote's proxy reference to stop communicating with.
	 */
	void removeRemoteProxyReference(BusMemberReference remoteReference);

	/**
	 * Disconnect from all references that use the same session.Does not send
	 * {@link ImportMessageType#ImportRemove Remove message}
	 *
	 * @param session
	 *            the session that is to be disconnected.
	 */
	void removeRemoteProxyReferences(Session session);

	/**
	 * List all current references for this proxy. <br>
	 * usually used to check the size, when the size of the set of references is
	 * 0 it means that the proxy should be closed and all Java-references so the
	 * object can be collected.
	 *
	 * @return the set of referneces.
	 */
	Collection<BusMemberReference> getRemoteProxiesReferences();

	/**
	 * Get the subscription parameters of the {@link BusMember} being proxied by
	 * this proxy.
	 *
	 * @return
	 */
	Resource[] getSubscriptionParameters();

	/**
	 * When a session receives a {@link WrappedBusMessage}, it delivers it to
	 * the {@link ProxyBusMember} which its
	 * {@link ProxyBusMember#getBusMemberId() busMemberId} matches the
	 * {@link WrappedBusMessage#getRemoteProxyRegistrationId()
	 * WrappedBusMessage's destination}. The delivery is received through this
	 * method.
	 *
	 * @param session
	 *            the session through which the message was received.
	 * @param busMessage
	 *            the actual message received.
	 */
	void handleMessage(Session session, WrappedBusMessage busMessage);

	/**
	 * Used to check if a {@link ProxyBusMember} is capable of assuming the
	 * proxy duties of a requested proxy with the given parameters. <br>
	 *
	 * Usually this is just the comparison of arrays of registration parameters
	 *
	 * @param registrationParameters
	 * @return true if the proxy is compatible with the parameters.
	 */
	boolean isCompatible(Resource[] registrationParameters);

	/**
	 * Called when the proxy has to disconnect from the bus.
	 */
	void close();

	/**
	 * When a update of registration parameters is required.
	 *
	 * @param newParams
	 *            the new parameters to use.
	 */
	void addSubscriptionParameters(Resource[] newParams);

	/**
	 * When a update of registration parameters is required.
	 *
	 * @param newParams
	 *            the new parameters to use.
	 */
	void removeSubscriptionParameters(Resource[] newParams);

}
