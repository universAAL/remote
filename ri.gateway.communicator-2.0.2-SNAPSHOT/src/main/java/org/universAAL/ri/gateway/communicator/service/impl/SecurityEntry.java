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

public class SecurityEntry {
	private String entryRegex;
	private SecurityAction action;
	private Type type;
	
	public SecurityEntry(SecurityAction action, Type type, String entryRegex) {
		this.action = action;
		this.type = type;
		this.entryRegex = entryRegex;
	}

	public String getEntryRegex() {
		return entryRegex;
	}

	public void setEntryRegex(String entryRegex) {
		this.entryRegex = entryRegex;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SecurityAction getAction() {
		return action;
	}

	public void setAction(SecurityAction action) {
		this.action = action;
	}
}
