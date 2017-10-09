/*******************************************************************************
 * Copyright 2017 2011 Universidad Politécnica de Madrid
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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * @author amedrano
 * 
 */
public class BusMemberReferenceTemporal extends BusMemberReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Queue<ScopedResource> mq;

	/**
	 * @param session
	 */
	public BusMemberReferenceTemporal(Session session) {
		super(session);
		mq = new ConcurrentLinkedQueue<ScopedResource>();
	}

	/** {@inheritDoc} */
	@Override
	public void send(ScopedResource message) {
		// check queue size
		// if (sender.getMaxQueueSize() > 0
		// && mq.size() >= sender.getMaxQueueSize()) {
		// // queue overflowing, remove first
		// mq.remove();
		// }
		// add to message buffer and wait to flush.
		mq.add(message);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Message sendRequest(ScopedResource message)
			throws TimeoutException {
		// wait a maximum of timeout before giving up.
		long timeout = sender.getTimeout();
		try {
			if (timeout >= 0)
				wait(timeout);
			else
				wait();
		} catch (InterruptedException e) {
			throw new TimeoutException(e.toString());
		}
		if (busMemberid != null) {
			return super.sendRequest(message);
		} else {
			throw new TimeoutException();
		}
	}

	/**
	 * Send all pending events / requests, now that we know the busmemberID.
	 * 
	 * @param busMemeberID
	 *            The remote bus member that we did not know before.
	 */
	public void flush(String busMemeberID) {
		this.busMemberid = busMemeberID;
		while (!mq.isEmpty()) {
			ScopedResource m = mq.poll();
			super.send(m);
		}

		notify();
	}

}
