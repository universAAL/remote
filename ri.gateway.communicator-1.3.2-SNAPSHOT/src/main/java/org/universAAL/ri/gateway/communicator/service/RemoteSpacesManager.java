package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.ri.gateway.eimanager.ImportEntry;

public interface RemoteSpacesManager {
	public ImportEntry importRemoteUI(BusMember sourceMember, String modailtyRegex) throws IOException,
    ClassNotFoundException;
	
    public ImportEntry importRemoteService(BusMember sourceMember,
	    String serviceType, String serverNamespace) throws IOException,
	    ClassNotFoundException;

    public ImportEntry importRemoteContextEvents(BusMember sourceMember,
	    ContextEventPattern[] cpe) throws IOException,
	    ClassNotFoundException;

    public boolean unimportRemoteService(ImportEntry entry) throws IOException,
	    ClassNotFoundException;
}
