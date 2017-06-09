/*
    Copyright 2015-2015 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    See the NOTICE file distributed with this work for additional
    information regarding copyright ownership

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.universAAL.ri.gateway.protocol;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public abstract class LinkMessage extends Message {

	/**
	 *
	 */
	private static final long serialVersionUID = -5253428613643147326L;

	public LinkMessage() {
		super();
	}

	public LinkMessage(LinkMessage msg) {
		super(msg);
	}

	public enum LinkMessageType {
		CONNECTION_REQUEST, CONNECTION_RESPONSE, DISCONNECTION_REQUEST, RECONNECTION_REQUEST,
	}

	public abstract int getType();

}
