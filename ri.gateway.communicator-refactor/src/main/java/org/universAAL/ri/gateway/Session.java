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

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.ComunicationEventListener;
import org.universAAL.ri.gateway.communicator.service.LinkContext;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.operations.ExportOpertaionChain;
import org.universAAL.ri.gateway.operations.ImportOpertaionChain;
import org.universAAL.ri.gateway.operations.MessageOperationChain;
import org.universAAL.ri.gateway.operations.OperationChainManager;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.MessageReceiver;
import org.universAAL.ri.gateway.protocol.MessageSender;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.ProxyPool;

/**
 * Representation of a one to one link between 2 ASGs. It is in charge of
 * connecting the communication layer with the Importer and proxies.
 * 
 * @author amedrano
 * 
 */
public class Session implements ComunicationEventListener, MessageSender,
	MessageReceiver, OperationChainManager {

    private Importer importer;
    private ProxyPool pool;
    private final Configuration config;
    private String remoteScope;

    public Session(final Configuration config) {
	this.config = config;
	//

    }

    public boolean onConnect(final CommunicationHandler ch, final LinkContext lc) {
	// TODO Auto-generated method stub
	return true;
    }

    public boolean onFailure(final CommunicationHandler ch, final LinkContext lc) {
	// TODO Auto-generated method stub
	return true;
    }

    public boolean onDicconect(final CommunicationHandler ch,
	    final LinkContext lc) {
	// TODO Auto-generated method stub
	return true;
    }

    public String getScope() {
	return remoteScope;
    }

    public void send(final Message message) {
	// TODO Auto-generated method stub

    }

    public Message sendRequest(final Message message) {
	// TODO Auto-generated method stub
	return null;
    }

    public ImportOpertaionChain getImportOperationChain() {
	return config.getImportOperationChain();
    }

    public ExportOpertaionChain getExportOperationChain() {
	return config.getExportOperationChain();
    }

    public MessageOperationChain getIncomingMessageOperationChain() {
	return config.getIncomingMessageOperationChain();
    }

    public MessageOperationChain getOutgoingMessageOperationChain() {
	return config.getOutgoingMessageOperationChain();
    }

    public void handleMessage(final Message msg) {
	if (msg instanceof ImportMessage) {
	    importer.handleImportMessage((ImportMessage) msg);
	} else if (msg instanceof WrappedBusMessage) {
	    final WrappedBusMessage wbm = (WrappedBusMessage) msg;
	    pool.get(wbm.getRemoteProxyRegistrationId()).handleMessage(wbm);
	} else if (msg instanceof ErrorMessage) {
	    final ErrorMessage em = (ErrorMessage) msg;
	    LogUtils.logError(Gateway.getInstance().context, getClass(),
		    "handleMessage",
		    "Received Error Message: " + em.getDescription());
	}
    }

}
