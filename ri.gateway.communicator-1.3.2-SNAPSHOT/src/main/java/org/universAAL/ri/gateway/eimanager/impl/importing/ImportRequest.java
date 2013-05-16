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
