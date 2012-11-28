package org.universAAL.ri.gateway.eimanager;

import java.io.IOException;

import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

public interface ExportManager extends RepoEIManager{
    public ServiceResponse sendServiceRequest(String sourceId, ServiceRequest request);
    public void sendUIRequest(String sourceId, UIRequest request);
    
    public ProxyRegistration registerProxies(ImportRequest request) throws IOException, ClassNotFoundException;
    public void unregisterProxies(ImportRequest request);
}
