package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyServiceCaller extends ProxyBusMember {

    private DefaultServiceCaller caller;
    private String serverNamespace;

    public ProxyServiceCaller(ExportedProxyManager manager, String targetId,
	    ModuleContext mc, String serverNamespace) {
	super(manager, targetId, mc);
	caller = new DefaultServiceCaller(mc);
	this.serverNamespace = serverNamespace;
    }

    public ServiceResponse invoke(ServiceRequest req) {
	ServiceResponse response = caller.call(req);
	prepareRequestedOutput(response.getOutputs());
	return response;
    }

    private void prepareRequestedOutput(List outputs) {
	if (outputs != null && !outputs.isEmpty()) {
	    for (int i = outputs.size() - 1; i > -1; i--) {
		ProcessOutput po = (ProcessOutput) outputs.remove(i);

		Object val = po.getParameterValue();
		if (val == null)
		    continue;

		String poSuffixUri = po.getURI().substring(
			po.getURI().indexOf("#") + 1);

		ProcessOutput substitutedWithServerURI = new ProcessOutput(
			serverNamespace + poSuffixUri);
		substitutedWithServerURI.setParameterValue(val);
		outputs.add(substitutedWithServerURI);
	    }
	}
    }

    public void removeProxy() {
	caller.close();
    }

    public String getId() {
	return caller.getMyID();
    }
}
