/*
    Copyright 2014 Universidad Politécnica de Madrid UPM
    
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

import org.universAAL.ioc.dependencies.DependencyProxy;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;

/**
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author amedrano
 * @version $LastChangedRevision: 386 $ ($LastChangedDate: 2014-07-22 11:47:16
 *          +0200 (mar, 22 jul 2014) $)
 * 
 * @param <T>
 */
public class PassiveDependencyProxy<T> implements DependencyProxy<T>,
	SharedObjectListener {

    private Object[] filters;
    private T proxy;
    private Object remH;

    public PassiveDependencyProxy(final ModuleContext context,
	    final Object[] filters) {
	final Object[] ref = context.getContainer().fetchSharedObject(context,
		filters, this);
	if (ref != null && ref.length > 0) {
	    try {
		proxy = (T) ref[0];
	    } catch (final Exception e) {
	    }
	}
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
	synchronized (this) {
	    while (proxy == null) {
		try {
		    wait();
		} catch (final InterruptedException e) {
		    return proxy;
		}
	    }
	    return proxy;
	}
    }

    public void setObject(final T value) {
	synchronized (this) {
	    this.proxy = value;
	    notifyAll();
	}
    }

    public void sharedObjectAdded(final Object sharedObj,
	    final Object removeHook) {
	try {
	    setObject((T) sharedObj);
	    this.remH = removeHook;
	} catch (final Exception e) {

	}
    }

    public void sharedObjectRemoved(final Object removeHook) {
	if (removeHook == remH) {
	    proxy = null;
	}

    }
}
