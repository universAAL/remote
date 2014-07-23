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
public class NPEDependencyProxy<T> implements DependencyProxy<T>  {

    private Object[] filters;
    private T proxy;

    public NPEDependencyProxy(Object[] filters){
        this.filters = Arrays.copyOf(filters, filters.length);
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
            return proxy;
        }
    }

    public void setObject(T value){
        synchronized (this) {
            this.proxy = value;
        }
    }

}