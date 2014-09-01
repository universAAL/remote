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
package org.universAAL.ri.gateway.protocol;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;

/**
 * @author amedrano
 * 
 */
public class ImportMessage extends Message {

    /**
     * The serial Version
     */
    private static final long serialVersionUID = -3533596854780040065L;

    /**
     * The type of the current {@link ImportMessage}.
     */
    private ImportMessageType messageType;

    /**
     * When a Request, Remove or Refresh type messages are issued, they refer to
     * this Local {@link ProxyBusMember}. A successful Response returns the
     * proxied busMember reference.
     */
    private String busMemberId;

    /**
     * When a Request or a Refresh type messages are issued, the referred
     * {@link ProxyBusMember} is to have these parameters.
     */
    private Resource[] parameters;

    /**
     * Different type of import messages.
     * 
     * @author amedrano
     * 
     */
    public enum ImportMessageType {
	ImportRequest, ImportResponse, ImportRemove, ImportRefresh,
    }

    /**
     * @return the messageType
     */
    public final ImportMessageType getMessageType() {
	return messageType;
    }

    /**
     * @return the busMemberId
     */
    public final String getBusMemberId() {
	return busMemberId;
    }

    /**
     * @return the parameters
     */
    public final Resource[] getParameters() {
	return parameters;
    }

    /**
     * Constructor.
     */
    protected ImportMessage() {
	super();
    }

    /**
     * Response constructor.
     * 
     * @param respondTo
     */
    protected ImportMessage(final Message respondTo) {
	super(respondTo);
    }

    /**
     * Check that an ImportResponse type Message has been accepted.
     * 
     * @return
     */
    public boolean isAccepted() {
	return isResponse()
		&& this.messageType == ImportMessageType.ImportResponse
		&& this.busMemberId != null;
    }

    /**
     * Create a importRequest {@link ImportMessage} for a particular
     * {@link ProxyBusMember} with certain parameters.
     * 
     * @param requestedBusMember
     *            the busMember identification for future reference, This should
     *            be the reference of the proxy created locally.
     * @param busMemberParameters
     * @return the message.
     */
    public static ImportMessage importRequest(final String requestedBusMember,
	    final Resource[] busMemberParameters) {
	final ImportMessage im = new ImportMessage();
	im.messageType = ImportMessageType.ImportRequest;
	im.busMemberId = requestedBusMember;
	im.parameters = busMemberParameters;
	return im;
    }

    /**
     * Create a importResponse {@link ImportMessage}.
     * 
     * @param request
     *            the request to respond to.
     * @param proxyIdentifyer
     *            The identifier of the local proxy created (so remote
     *            associates it to its local), null if the request is denied.
     * @return the message.
     */
    public static ImportMessage importResponse(final ImportMessage request,
	    final String proxyIdentifyer) {
	if (!request.getMessageType().equals(ImportMessageType.ImportRequest)
		|| !request.getMessageType().equals(
			ImportMessageType.ImportRefresh)) {
	    throw new RuntimeException(
		    "Response must be in response to a request or a refresh.");
	}
	final ImportMessage im = new ImportMessage(request);
	im.messageType = ImportMessageType.ImportResponse;
	im.busMemberId = proxyIdentifyer;
	return im;
    }

    /**
     * Create a importRefresh {@link ImportMessage} for a particular
     * {@link ProxyBusMember} with certain parameters.
     * 
     * @param requestedBusMember
     *            the busMember identification for future reference, This should
     *            be the local reference.
     * @param busMemberParameters
     * @return the message.
     */
    public static ImportMessage importRefresh(final String requestedBusMember,
	    final Resource[] newBusMemberParameters) {
	final ImportMessage im = new ImportMessage();
	im.messageType = ImportMessageType.ImportRefresh;
	im.busMemberId = requestedBusMember;
	im.parameters = newBusMemberParameters;
	return im;
    }

    /**
     * Create a importRemove {@link ImportMessage} for a particular
     * {@link ProxyBusMember} .
     * 
     * @param requestedBusMember
     *            the busMember identification for future reference, This should
     *            be the local proxy reference.
     * @return the message.
     */
    public static ImportMessage importRemove(final String requestedBusMember) {
	final ImportMessage im = new ImportMessage();
	im.messageType = ImportMessageType.ImportRemove;
	im.busMemberId = requestedBusMember;
	return im;
    }
}
