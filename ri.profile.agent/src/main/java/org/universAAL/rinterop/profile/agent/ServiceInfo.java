package org.universAAL.rinterop.profile.agent;

import org.universAAL.middleware.interfaces.aalspace.AALSpaceType;

//import org.universAAL.middleware.interfaces.aalspace.AALSpaceType;

public class ServiceInfo {

	//AALSpaceCard properties
    private String serviceName;
	private String serviceID;
    private String status;
    private String version;
	private AALSpaceType type;//static or mobile
	// private String description;
	// private String peerCoordinatorID;
	// private String peeringChannel;
	// private String peeringChannelName;
	// // TODO: Profile of the Space. Currentyl this is a string but a more
	// // constrained type should be used
	// private String profile;
	// private int retry;
	// private int aalSpaceLifeTime;

    public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceID() {
		return serviceID;
	}
	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public AALSpaceType getType() {
		return type;
	}
	public void setType(AALSpaceType type) {
		this.type = type;
	}
}
