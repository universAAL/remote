package org.universAAL.ri.gateway.eimanager;

import java.io.IOException;

import org.universAAL.middleware.sodapop.BusMember;

public interface ImportExecutor {
    public void internalImportRemoteService(String uuid, BusMember sourceMember, String targetMemberIdRegex, String serverNamespace) throws IOException, ClassNotFoundException;
    public void removeRemoteBusMember(BusMember sourceMember, String targetMemberIdRegex);
}
