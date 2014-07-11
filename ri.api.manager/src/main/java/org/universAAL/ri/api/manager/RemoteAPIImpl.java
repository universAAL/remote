package org.universaal.ri.api.manager;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * Implementation of the RemoteAPI interface.
 * 
 * @author alfiva
 * 
 */
public class RemoteAPIImpl implements RemoteAPI {

    /**
     * The registry of remote nodes that have registered (through the REGISTER
     * method) into the server. Each of these nodes, identified by a unique
     * token, have an associated UAAL instance.
     */
    private static Hashtable<String, RemoteUAAL> nodes = new Hashtable<String, RemoteUAAL>();
    /**
     * The uAAL context.
     */
    private ModuleContext context;

    /**
     * Basic constructor.
     * 
     * @param ctxt
     *            The uAAL context
     */
    public RemoteAPIImpl(ModuleContext ctxt) {
	this.context = ctxt;
    }

    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#register(java.lang.String, java.lang.String)
     */
    public void register(String id, String remote) throws Exception{
	Activator.logI("RemoteAPIImpl.register()", "Received call from remote node > REGISTER, sender: "+id);//TODO Log IDs?
	if(nodes.containsKey(id)){
	    ((RemoteUAAL)nodes.get(id)).setRemoteID(remote);
	}else{
	    nodes.put(id, new RemoteUAAL(context,remote));
	}
    }

    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#sendC(java.lang.String, java.lang.String)
     */
    public void sendC(String id, String cevent) throws Exception {
	Activator.logI("RemoteAPIImpl.sendC()", "Received call from remote node > SENDC, sender: "+id);//TODO Log IDs?
	if(nodes.containsKey(id)){
	    ContextEvent ce=(ContextEvent) Activator.parser.deserialize(cevent);
	    if (ce==null) {
		throw new Exception("Unable to deserialize event");
	    }
	    ((RemoteUAAL)nodes.get(id)).sendC(ce);
	}else{
	    throw new Exception("ID not registered");
	}
    }

    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#subscribeC(java.lang.String, java.lang.String)
     */
    public void subscribeC(String id, String cpattern) throws Exception {
	Activator.logI("RemoteAPIImpl.subscribeC()", "Received call from remote node > SUBSCRIBEC, sender: "+id);//TODO Log IDs?
	if(nodes.containsKey(id)){
	    ContextEventPattern cp=(ContextEventPattern) Activator.parser.deserialize(cpattern);
	    if (cp==null) {
		throw new Exception("Unable to deserialize pattern");
	    }
	    RemoteUAAL node=(RemoteUAAL)nodes.get(id);
	    node.subscribeC(new ContextEventPattern[]{cp},node.createCListener());
	}else{
	    throw new Exception("ID not registered");
	}
    }

    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#callS(java.lang.String, java.lang.String)
     */
    public String callS(String id, String srequest) throws Exception {
	Activator.logI("RemoteAPIImpl.callS()", "Received call from remote node > CALLS, sender: "+id);//TODO Log IDs?
	ServiceResponse res = null;
	if (nodes.containsKey(id)) {
	    ServiceRequest req = (ServiceRequest) Activator.parser.deserialize(srequest);
	    if (req == null) {
		throw new Exception("Unable to deserialize request");
	    }
	    RemoteUAAL node = (RemoteUAAL) nodes.get(id);
	    res = node.callS(req);
	} else {
	    throw new Exception("ID not registered");
	}
	if (res != null) {
	    StringBuilder strb = new StringBuilder();
	    List outputs = res.getOutputs();
	    if (outputs != null && outputs.size() > 0) {
		for (Iterator iter1 = outputs.iterator(); iter1.hasNext();) {
		    Object obj = iter1.next();
		    if (obj instanceof ProcessOutput) {
			ProcessOutput output = (ProcessOutput) obj;
			strb.append(output.getURI()).append("=")
				.append(output.getParameterValue().toString())
				.append("\n");
		    } else if (obj instanceof List) {
			List outputLists = (List) obj;
			for (Iterator iter2 = outputLists.iterator(); iter2
				.hasNext();) {
			    ProcessOutput output = (ProcessOutput) iter2.next();
			    strb.append(output.getURI())
				    .append("=")
				    .append(output.getParameterValue()
					    .toString()).append("\n");
			}
		    }
		}
	    }
	    strb.append(RemoteAPI.KEY_STATUS).append("=").append(res.getCallStatus().toString()).append("\n");
	    strb.append(RemoteAPI.FLAG_TURTLE).append("\n");
	    strb.append(Activator.parser.serialize(res));
	    return strb.toString();
	}else{
	    return null;
	}
	
// For testing://	String turtle = "@prefix ns: http://www.daml.org/services/owl-s/1.1/Process.owl# . "
//		+ "@prefix ns1: http://ontology.universAAL.org/CHE.owl# ."
//		+ "@prefix : http://ontology.universAAL.org/uAAL.owl# . "
//		+ ":BN000000 a :ServiceResponse ; "
//		+ "  :callStatus :call_succeeded ; "
//		+ "  :returns ( "
//		+ "    ns1:sparqlResult "
//		+ "  ) . "
//		+ ":call_succeeded a :CallStatus . "
//		+ "ns1:sparqlResult ns:parameterValue \"\"^^http://www.w3.org/2001/XMLSchema#string ; "
//		+ "  a ns:Output .";
    }

    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#provideS(java.lang.String, java.lang.String)
     */
    public void provideS(String id, String sprofile) throws Exception {
	Activator.logI("RemoteAPIImpl.provideS()", "Received call from remote node > PROVIDES, sender: "+id);//TODO Log IDs?
	if(nodes.containsKey(id)){
	    ServiceProfile sp=(ServiceProfile) Activator.parser.deserialize(sprofile);
	    if (sp==null) {
		throw new Exception("Unable to deserialize profile");
	    }
	    RemoteUAAL node=(RemoteUAAL)nodes.get(id);
	    node.provideS(new ServiceProfile[]{sp},node.createSListener());
	}else{
	    throw new Exception("ID not registered");
	}
    }

    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#unregister(java.lang.String)
     */
    public void unregister(String id) throws Exception {
	Activator.logI("RemoteAPIImpl.unregister()", "Received call from remote node > UNREGISTER, sender: "+id);//TODO Log IDs?
	if(!nodes.containsKey(id)){
	    throw new Exception("ID not registered");
	}
	RemoteUAAL uaal=nodes.remove(id);
	if(uaal!=null){
	    uaal.terminate();
	}else{
	    throw new Exception("No instance for this ID");
	}
    }
    
    /* (non-Javadoc)
     * @see org.universaal.ri.api.manager.RemoteAPI#unregisterAll()
     */
    public void unregisterAll() {
	Enumeration<String> keys = nodes.keys();
	while (keys.hasMoreElements()) {
	    RemoteUAAL uaal = nodes.remove(keys.nextElement());
	    if (uaal != null) {
		uaal.terminate();
	    } else {
		Activator.logE("RemoteAPIImpl.unregisterAll()", "No instance for this ID");
	    }
	}
    }
}
