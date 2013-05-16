package org.universAAL.ri.gateway.eimanager;

import java.io.IOException;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;

public interface ImportManager extends RepoEIManager {
    public void sendContextEvent(String sourceId, ContextEvent event);
    public void sendUIResponse(String sourceId, UIResponse response);
    public void refreshProxy(ProxyRegistration proxyRegistration) throws IOException, ClassNotFoundException;
}
