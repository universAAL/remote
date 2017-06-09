/*******************************************************************************
 * Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
 * Institute of Information Science and Technologies
 * of the Italian National Research Council
 *   
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Main Message class. All Messages interchanged between ASGs should be
 * subclasses of this class.
 * 
 * @author amedrano
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * 
 */
public abstract class Message implements Serializable {

	/**
	 * Serial version
	 */
	private static final long serialVersionUID = 5115999991466796697L;

	/**
	 * Next Sequence id to be issued. this will give a "unique" number per
	 * Message.
	 */
	private static short currentSequence = 0;

	/**
	 * The sequence of this Message.
	 */
	private final short sequence;

	/**
	 * If this message is a response, this is the request sequence.
	 */
	protected short inResponseTo;

	/**
	 * Constructor for a {@link Message}.
	 */
	public Message() {
		sequence = currentSequence;
		currentSequence = (short) ((currentSequence + 1) % Short.MAX_VALUE);
		inResponseTo = -1;
	}

	/**
	 * Constructor for a {@link Message} in response to another.
	 * 
	 * @param respondTo
	 *            message to which to respond to.
	 */
	public Message(final Message respondTo) {
		this();
		inResponseTo = respondTo.sequence;
	}

	public short getSequence() {
		return sequence;
	}

	public short getInResponseTo() {
		return inResponseTo;
	}

	public boolean isResponse() {
		return inResponseTo != -1;
	}

	public byte[] getBytes() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ObjectOutputStream objOut;
			objOut = new ObjectOutputStream(output);
			objOut.writeObject(this);
			objOut.flush();
			objOut.close();
			return output.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Unable to generates bytes", e);
		}
	}
}
