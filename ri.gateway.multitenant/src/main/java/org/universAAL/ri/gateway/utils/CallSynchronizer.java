/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

import java.util.Hashtable;
import java.util.Map;

/**
 * A Helper class that enables to perform calls and wait for the asynchronous
 * response.
 * 
 * @author amedrano
 * 
 */
public abstract class CallSynchronizer<ID, INPUT, OUTPUT> {

    private class CallStatus {
	OUTPUT output = null;
	boolean returned = false;
	boolean purged = false;

	void setResutlt(OUTPUT o) {
	    synchronized (this) {
		returned = true;
		output = o;
		notifyAll();
	    }
	}
    }

    /**
     * The pile of waiting calls.
     */
    Map<ID, CallStatus> waiting;

    /**
     * Constructor.
     */
    public CallSynchronizer() {
	waiting = new Hashtable<ID, CallStatus>();
    }

    /**
     * This method is the main method to syncronize the caller thread. The
     * caller {@link Thread} will be stoped until another thread calls
     * {@link CallSynchronizer#performResponse(ID, Object)}.
     * 
     * @param callerID
     *            the Id to identify the call.
     * @param input
     *            The input needed to perform the call.
     * @return the call response.
     * 
     * @throws InterruptedException
     *             when the call was aborted by other thread.
     * @see CallSynchronizer#performResponse(ID, Object)
     */
    public OUTPUT performCall(ID callerID, INPUT input)
	    throws InterruptedException {
	CallStatus status = new CallStatus();
	synchronized (waiting) {
	    waiting.put(callerID, status);
	}
	operate(callerID, input);
	synchronized (status) {
	    while (!status.returned) {
		try {
		    status.wait();
		} catch (InterruptedException e) {
		}
	    }
	}
	if (status.purged) {
	    throw new InterruptedException();
	}
	return status.output;
    }

    /**
     * This method operates the call, it performs the adequate operations to
     * ensure there will be a response calling on another thread.
     * 
     * @param callerID
     *            the call identifier
     * @param input
     *            the input needed for the call
     */
    protected abstract void operate(ID callerID, INPUT input);

    /**
     * When a response for a call is found, this method should be used to notify
     * the Caller {@link Thread} waiting for it.
     * 
     * @param id
     * @param output
     */
    public void performResponse(ID id, OUTPUT output) {
	synchronized (waiting) {
	    CallStatus st = waiting.remove(id);
	    if (st != null) {
		st.setResutlt(output);
	    }
	}
    }

    /**
     * Abort the call with the given ID. it unblocks the call and the
     * {@link CallSynchronizer#performCall(Object, Object)} will throw a
     * {@link InterruptedException}.
     * 
     * @param id
     */
    public void abortCall(ID id) {
	synchronized (waiting) {
	    CallStatus st = waiting.remove(id);
	    if (st != null) {
		st.purged = true;
		st.setResutlt(null);
	    }
	}
    }

    /**
     * When there will be no more responses this method aborts all calls,
     */
    public void purge() {
	for (CallStatus st : waiting.values()) {
	    st.purged = true;
	    st.setResutlt(null);
	}
	waiting.clear();
    }
}
