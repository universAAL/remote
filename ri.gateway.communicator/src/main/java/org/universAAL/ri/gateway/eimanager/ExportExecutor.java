package org.universAAL.ri.gateway.eimanager;

import org.universAAL.middleware.sodapop.BusMember;

public interface ExportExecutor {
    public void exportBusMemberForRemote(BusMember sourceMember);
    public void removeExportedBusMember(BusMember sourceMember);
}
