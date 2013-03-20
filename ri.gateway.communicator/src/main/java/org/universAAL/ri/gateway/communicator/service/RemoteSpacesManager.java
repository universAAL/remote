package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;

import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.bus.member.BusMember;

public interface RemoteSpacesManager {
    public boolean importRemoteService(BusMember sourceMember, String serviceType, String serverNamespace) throws IOException, ClassNotFoundException;
    public boolean importRemoteContextEvents(BusMember sourceMember, ContextEventPattern[] cpe) throws IOException, ClassNotFoundException;
}
