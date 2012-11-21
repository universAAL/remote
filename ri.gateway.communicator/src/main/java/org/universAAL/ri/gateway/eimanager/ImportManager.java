package org.universAAL.ri.gateway.eimanager;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.ui.UIResponse;

public interface ImportManager extends RepoEIManager {
    public void sendContextEvent(String sourceId, ContextEvent event);
    public void sendUIResponse(String sourceId, UIResponse response);
    
}
