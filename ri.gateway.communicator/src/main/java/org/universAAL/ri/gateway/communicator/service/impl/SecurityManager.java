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
package org.universAAL.ri.gateway.communicator.service.impl;

import java.util.HashSet;
import java.util.Set;


public enum SecurityManager {
    Instance;
    
    static{
    	allowImportSecurityEntries = new HashSet<SecurityEntry>();
    	denyImportSecurityEntries = new HashSet<SecurityEntry>();
    	
    	allowExportSecurityEntries = new HashSet<SecurityEntry>();
    	denyExportSecurityEntries = new HashSet<SecurityEntry>();
    }
    
    private static Set<SecurityEntry> allowImportSecurityEntries;
    private static Set<SecurityEntry> denyImportSecurityEntries;
    
    private static Set<SecurityEntry> allowExportSecurityEntries;
    private static Set<SecurityEntry> denyExportSecurityEntries;
   
    public static boolean isOperationAllowed(String uri, Type type){
    	switch (type) {
		case Import:
			if (matchesAnyValue(denyImportSecurityEntries, uri)){
	    		return false;
	    	}
	    	if (matchesAnyValue(allowImportSecurityEntries, uri)){
	    		return true;
	    	}
			break;

		case Export:
			if (matchesAnyValue(denyExportSecurityEntries, uri)){
	    		return false;
	    	}
	    	if (matchesAnyValue(allowExportSecurityEntries, uri)){
	    		return true;
	    	}
			break;
		}
    	return false;
    }
    
    private static boolean matchesAnyValue(Set<SecurityEntry> entries, String value){
    	for(SecurityEntry entry : entries){
    		if (value.matches(entry.getEntryRegex())){
    			return true;
    		}
    	}
    	return false;
    }

	public Set<SecurityEntry> getAllowImportSecurityEntries() {
		return allowImportSecurityEntries;
	}

	public void setAllowImportSecurityEntries(
			Set<SecurityEntry> allowImportSecurityEntries) {
		SecurityManager.allowImportSecurityEntries = allowImportSecurityEntries;
	}

	public Set<SecurityEntry> getDenyImportSecurityEntries() {
		return denyImportSecurityEntries;
	}

	public void setDenyImportSecurityEntries(
			Set<SecurityEntry> denyImportSecurityEntries) {
		SecurityManager.denyImportSecurityEntries = denyImportSecurityEntries;
	}

	public Set<SecurityEntry> getAllowExportSecurityEntries() {
		return allowExportSecurityEntries;
	}

	public void setAllowExportSecurityEntries(
			Set<SecurityEntry> allowExportSecurityEntries) {
		SecurityManager.allowExportSecurityEntries = allowExportSecurityEntries;
	}

	public Set<SecurityEntry> getDenyExportSecurityEntries() {
		return denyExportSecurityEntries;
	}

	public void setDenyExportSecurityEntries(
			Set<SecurityEntry> denyExportSecurityEntries) {
		SecurityManager.denyExportSecurityEntries = denyExportSecurityEntries;
	}
}

enum Type {
	Import, Export;
}

enum SecurityAction {
	Allow, Deny;
}

