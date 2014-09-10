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

import java.util.UUID;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.gateway.communicator.service.impl.ClientSocketCommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.impl.MessageType;
import org.universAAL.ri.gateway.communicator.service.impl.MessageWrapper;
import org.universAAL.ri.gateway.communicator.service.impl.SessionManager;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.configuration.Configuration.ConnectionMode;
import org.universAAL.ri.gateway.operations.MessageOperationChain;
import org.universAAL.ri.gateway.operations.OperationChainManager;
import org.universAAL.ri.gateway.operations.ParameterCheckOpertaionChain;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.MessageReceiver;
import org.universAAL.ri.gateway.protocol.MessageSender;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ProxyPool;

/**
 * Representation of a one to one link between 2 ASGs. It is in charge of
 * connecting the communication layer with the Importer and proxies.
 * 
 * @author amedrano
 * 
 */
public class Session implements MessageSender, MessageReceiver,
	OperationChainManager {

    private Importer importer;
    private final ProxyPool pool;
    private final Configuration config;
    private String remoteScope;
    private ClientSocketCommunicationHandler comunication;

    public Session(final Configuration config, final ProxyPool proxyPool) {
	this.config = config;
	this.pool = proxyPool;

	if (config.getConnectionMode() != ConnectionMode.CLIENT) {
	    throw new UnsupportedOperationException(
		    "Single session supports only the " + ConnectionMode.CLIENT);
	}
	comunication = new ClientSocketCommunicationHandler(config, this);
    }

    public String getScope() {
	return remoteScope;
    }

    public void send(final Message message) {
	org.universAAL.ri.gateway.communicator.service.Message content = new org.universAAL.ri.gateway.communicator.service.Message(
		message);
	MessageWrapper wrap = new MessageWrapper(MessageType.HighPush, content,
		"");
	SessionManager session = SessionManager.getInstance();
	UUID[] active = session.getSessionIds();
	if (active.length != 1) {
	    if (active.length == 0) {
		throw new IllegalStateException(
			"Trying to send a message but we no active session");
	    } else {
		throw new IllegalStateException(
			"Trying to send a message but we too many session");
	    }
	}
	comunication.sendMessage(wrap, new String[] { active[0].toString() });
    }

    public Message sendRequest(final Message message) {
	org.universAAL.ri.gateway.communicator.service.Message content = new org.universAAL.ri.gateway.communicator.service.Message(
		message);
	MessageWrapper wrap = new MessageWrapper(MessageType.HighReqRsp,
		content, "");
	SessionManager session = SessionManager.getInstance();
	UUID[] active = session.getSessionIds();
	if (active.length != 1) {
	    if (active.length == 0) {
		throw new IllegalStateException(
			"Trying to send a message but we no active session");
	    } else {
		throw new IllegalStateException(
			"Trying to send a message but we too many session");
	    }
	}
	wrap = comunication.sendMessage(wrap,
		new String[] { active[0].toString() });
	if (wrap.getType() != MessageType.HighReqRsp) {
	    throw new IllegalStateException(
		    "Expecting HighReqRsp message, but recieved "
			    + wrap.getType());
	}
	return (Message) wrap.getMessage().getContent();
    }

    public ParameterCheckOpertaionChain getImportOperationChain() {
	return config.getImportOperationChain();
    }

    public ParameterCheckOpertaionChain getExportOperationChain() {
	return config.getExportOperationChain();
    }

    public MessageOperationChain getIncomingMessageOperationChain() {
	return config.getIncomingMessageOperationChain();
    }

    public MessageOperationChain getOutgoingMessageOperationChain() {
	return config.getOutgoingMessageOperationChain();
    }

    /**
     * Receives the Message from the communication layer, delivering it to the
     * appropriate subcomponent.
     */
    public void handleMessage(final Message msg) {
	if (msg instanceof ImportMessage) {
	    importer.handleImportMessage((ImportMessage) msg);
	} else if (msg instanceof WrappedBusMessage) {
	    final WrappedBusMessage wbm = (WrappedBusMessage) msg;
	    final ProxyBusMember pbm = pool.get(wbm
		    .getRemoteProxyRegistrationId());
	    if (pbm != null) {
		pbm.handleMessage(this, wbm);
	    }
	} else if (msg instanceof ErrorMessage) {
	    final ErrorMessage em = (ErrorMessage) msg;
	    LogUtils.logError(Gateway.getInstance().context, getClass(),
		    "handleMessage",
		    "Received Error Message: " + em.getDescription());
	}
    }

}
