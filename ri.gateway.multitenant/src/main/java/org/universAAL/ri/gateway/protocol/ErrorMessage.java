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

/**
 * Message to send Errors to remote peers.
 *
 * @author amedrano
 *
 */
public class ErrorMessage extends Message {

	/**
	 * The serial Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The description to be logged.
	 */
	private final String description;

	/**
	 * Message that provoked the error, for reference or actions to be taken.
	 */
	private Message previousMessage;

	/**
	 * Create an Error Message with a description and a {@link Message}.
	 *
	 * @param description
	 * @param onMessage
	 */
	public ErrorMessage(final String description, final Message onMessage) {
		super(onMessage);
		this.description = description;
		this.previousMessage = onMessage;
	}

	/**
	 * Create an Error message with only the description of the error.
	 *
	 * @param description
	 */
	public ErrorMessage(final String description) {
		super();
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the previousMessage
	 */
	public Message getErroneousMessage() {
		return previousMessage;
	}

}
