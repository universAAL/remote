/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
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

package org.universAAL.ioc.dependencies.impl;

import java.util.Arrays;

import org.universAAL.ioc.dependencies.DependencyProxy;



/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class WaitingDependencyProxy<T> implements DependencyProxy<T>  {

    private static final long DEFAULT_SLEEP_STEP = 500;
    private static final long DEFAULT_TIMEOUT = 60 * 1000;
    private static final long NO_TIMEOUT = 0;
    private Object[] filters;
    private T proxy;
    private long sleep;
    private long timeout;

    public WaitingDependencyProxy(Object[] filters)  {
        this(filters, DEFAULT_SLEEP_STEP, DEFAULT_TIMEOUT);
    }

    public WaitingDependencyProxy(Object[] filters, long sleep, long timeout)  {
        this.filters = Arrays.copyOf(filters, filters.length);
        this.sleep = sleep;
        this.timeout = timeout;
    }

    public boolean isResolved() {
        synchronized (this) {
            return proxy != null;
        }
    }

    public Object[] getFilters() {
        return filters;
    }

    public T getObject() {
        if ( timeout < NO_TIMEOUT ) {
            return getNoTimeout();
        } else {
            return getWithTimeout();
        }
    }

    private T getWithTimeout() {
        long now = System.currentTimeMillis();
        long end = System.currentTimeMillis() + timeout;
        synchronized (this) {
            while ( proxy == null && now < end ) {
                try {
                    wait(sleep);
                    now = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    return proxy;
                }
            }
            return proxy;
        }
    }

    private T getNoTimeout() {
        synchronized (this) {
            while ( proxy == null ) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return proxy;
                }
            }
            return proxy;
        }
    }

    public void setObject(T value){
        synchronized (this) {
            this.proxy = value;
            notifyAll();
        }
    }
}
