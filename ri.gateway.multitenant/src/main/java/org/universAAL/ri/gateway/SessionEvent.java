/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
    Faculty of Computer Science, Electronics and Telecommunications
    Department of Computer Science

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
package org.universAAL.ri.gateway;

/**
 * This is the event used for notifying the {@link SessionEventListener}
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public interface SessionEvent {

    public enum SessionStatus {
	/**
	 * This is the first status as soon session object is created. The valid
	 * next status are: CONNECTING or CLOSED
	 */
	OPENING,
	/**
	 * This status is reached as soon as network is active the session will
	 * try to connect, or when the connection was already active but the
	 * link used by the session brake. The valid next status are CONNECTED
	 * or CLOSED
	 */
	CONNECTING,
	/**
	 * The status is 100% up and running. The valid next status are CLOSED
	 * or CONNECTING
	 */
	CONNECTED,
	/**
	 * The session as been request to close and it is now closed. No other
	 * status are valid
	 */
	CLOSED

    }

    public Session getSession();

    public SessionEvent.SessionStatus getCurrentStatus();

    public SessionEvent.SessionStatus getOldStatus();

}
