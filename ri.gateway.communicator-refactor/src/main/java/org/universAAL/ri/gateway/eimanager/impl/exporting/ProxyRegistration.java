/*
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
package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.Serializable;

@Deprecated
public class ProxyRegistration implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7598585122564189371L;
    private String id;
    private Object returnedValues;
    private boolean success;
    private String errorMessage;

    public ProxyRegistration(final String id, final Object returnedValues) {
	super();
	this.id = id;
	this.returnedValues = returnedValues;
	this.success = true;
	this.errorMessage = "";
    }

    public ProxyRegistration(final String errorMessage) {
	this.id = "";
	this.returnedValues = "";
	this.success = false;
	this.errorMessage = errorMessage;
    }

    public String getId() {
	return id;
    }

    public void setId(final String id) {
	this.id = id;
    }

    public Object getReturnedValues() {
	return returnedValues;
    }

    public void setReturnedValues(final Object returnedValues) {
	this.returnedValues = returnedValues;
    }

    @Override
    public String toString() {
	return String.format("Proxy: id: %s, returns: %s", id, returnedValues);
    }

    public boolean isSuccess() {
	return success;
    }

    public void setSuccess(final boolean success) {
	this.success = success;
    }

    public String getErrorMessage() {
	return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
	this.errorMessage = errorMessage;
    }

}
