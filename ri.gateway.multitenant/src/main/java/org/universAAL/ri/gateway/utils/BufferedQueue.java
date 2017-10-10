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
package org.universAAL.ri.gateway.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author amedrano
 * 
 */
public class BufferedQueue<A> extends ConcurrentLinkedQueue<A> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long maxSize;

	/**
	 * 
	 */
	public BufferedQueue(long qs) {
		this.maxSize = qs;
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(A e) {
		if (size() > maxSize) {
			remove();
		}
		return super.add(e);
	}

	/** {@inheritDoc} */
	@Override
	public boolean offer(A e) {
		if (size() > maxSize) {
			remove();
		}
		return super.offer(e);
	}

}
