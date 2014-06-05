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
package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.io.Serializable;
import java.util.Arrays;

import org.universAAL.ri.gateway.eimanager.impl.BusMemberType;

public class ImportRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -5069608110720753063L;
    /**
     * 
     */
    private String member;
    private String id;

    /**
     * Service import fields
     */
    private String serviceType;
    private String serverNamespace;

    /**
     * Context import fields
     */
    private String[] subjectURI;
    private String[] cpe;
    
    private String modalityRegex;

    public ImportRequest(final BusMemberType member, final String id) {
	super();
	this.member = member.toString();
	this.id = id;
    }

    public String getMember() {
	return member;
    }

    public void setMember(final String member) {
	this.member = member;
    }

    public String getId() {
	return id;
    }

    public void setId(final String id) {
	this.id = id;
    }

    public String getServiceType() {
	return serviceType;
    }

    public void setServiceType(final String serviceType) {
	this.serviceType = serviceType;
    }

    public String getServerNamespace() {
	return serverNamespace;
    }

    public void setServerNamespace(final String serverNamespace) {
	this.serverNamespace = serverNamespace;
    }

    public String[] getCpe() {
	return cpe;
    }

    public void setCpe(final String[] cpe) {
	this.cpe = cpe;
    }

    @Override
    public String toString() {
	return "ImportRequest ["
		+ (cpe != null ? "cpe=" + Arrays.toString(cpe) + ", " : "")
		+ (id != null ? "id=" + id + ", " : "")
		+ (member != null ? "member=" + member + ", " : "")
		+ (serverNamespace != null ? "serverNamespace="
			+ serverNamespace + ", " : "")
		+ (serviceType != null ? "serviceType=" + serviceType : "")
		+ (modalityRegex != null ? "modalityRegex=" + modalityRegex: "")
		+ "]";
    }

	public String[] getSubjectURIs() {
		return subjectURI;
	}

	public void setSubjectURIs(String[] subjectURI) {
		this.subjectURI = subjectURI;
	}

	public String getModalityRegex() {
		return modalityRegex;
	}

	public void setModalityRegex(String modalityRegex) {
		this.modalityRegex = modalityRegex;
	}

}
