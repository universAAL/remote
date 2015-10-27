/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.ri.api.manager;

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
import org.universAAL.ri.api.manager.exceptions.APIImplException;

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
     * @see org.universAAL.ri.api.manager.RemoteAPI#register(java.lang.String, java.lang.String)
     */
    public void register(String id, String remote) throws APIImplException{
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.register()", "Received call from remote node > REGISTER, sender: "+id);
	}
	if (Configuration.determineEndpoint(remote) == RemoteAPI.REMOTE_UNKNOWN){ // No POST nor GCM
	    throw new APIImplException("Unable to determine protocol of remote endpoint");
	}
	if(nodes.containsKey(id)){
	    ((RemoteUAAL)nodes.get(id)).setRemoteID(remote);
	}else{
	    nodes.put(id, new RemoteUAAL(context,id,remote));
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.RemoteAPI#sendC(java.lang.String, java.lang.String)
     */
    public void sendC(String id, String cevent) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.sendC()", "Received call from remote node > SENDC, sender: "+id);
	}
	if(nodes.containsKey(id)){
	    ContextEvent ce=(ContextEvent) Activator.getParser().deserialize(cevent);
	    if (ce==null) {
		throw new APIImplException("Unable to deserialize event");
	    }
	    ce.addScope(id); // MULTITENANT Add my scope //TODO Really?
	    ce.setProperty(ContextEvent.PROP_ORIG_SCOPE, id); // MULTITENANT Add my scope as origin, so I dont get it back 
	    ((RemoteUAAL)nodes.get(id)).sendC(ce);
	}else{
	    throw new APIImplException("ID not registered");
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.RemoteAPI#subscribeC(java.lang.String, java.lang.String)
     */
    public void subscribeC(String id, String cpattern) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.subscribeC()", "Received call from remote node > SUBSCRIBEC, sender: "+id);
	}
	if(nodes.containsKey(id)){
	    ContextEventPattern cp=(ContextEventPattern) Activator.getParser().deserialize(cpattern);
	    if (cp==null) {
		throw new APIImplException("Unable to deserialize pattern");
	    }
	    RemoteUAAL node=(RemoteUAAL)nodes.get(id);
	    node.subscribeC(new ContextEventPattern[]{cp},node.createCListener(cp.getURI()));
	}else{
	    throw new APIImplException("ID not registered");
	}
    }
    
    public boolean isPatternAdded(String id, String cpattern) {
	if(nodes.containsKey(id)){
	    ContextEventPattern cp=(ContextEventPattern) Activator.getParser().deserialize(cpattern);
	    if (cp==null) {
		return false;
	    }
	    RemoteUAAL node=(RemoteUAAL)nodes.get(id);
	    return node.isPatternAdded(cp.getURI());
	}else{
	    return false;
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.RemoteAPI#callS(java.lang.String, java.lang.String)
     */
    public String callS(String id, String srequest) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.callS()", "Received call from remote node > CALLS, sender: "+id);
	}
	ServiceResponse res = null;
	if (nodes.containsKey(id)) {
	    ServiceRequest req = (ServiceRequest) Activator.getParser().deserialize(srequest);
	    if (req == null) {
		throw new APIImplException("Unable to deserialize request");
	    }
	    RemoteUAAL node = (RemoteUAAL) nodes.get(id);
	    req.addScope(id); // MULTITENANT Add my scope //TODO Really?
	    req.setProperty(ContextEvent.PROP_ORIG_SCOPE, id); // MULTITENANT Add my scope as origin, so I dont get it back 
	    res = node.callS(req);
	} else {
	    throw new APIImplException("ID not registered");
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
	    strb.append(Activator.getParser().serialize(res));
	    return strb.toString();
	}else{
	    return null;
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.RemoteAPI#provideS(java.lang.String, java.lang.String)
     */
    public void provideS(String id, String sprofile) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.provideS()", "Received call from remote node > PROVIDES, sender: "+id);
	}
	if(nodes.containsKey(id)){
	    ServiceProfile sp=(ServiceProfile) Activator.getParser().deserialize(sprofile);
	    if (sp==null) {
		throw new APIImplException("Unable to deserialize profile");
	    }
	    RemoteUAAL node=(RemoteUAAL)nodes.get(id);
	    node.provideS(new ServiceProfile[]{sp},node.createSListener(sp.getURI()));
	}else{
	    throw new APIImplException("ID not registered");
	}
    }
    
    public boolean isProfileAdded(String id, String sprofile) {
	if(nodes.containsKey(id)){
	    ServiceProfile sp=(ServiceProfile) Activator.getParser().deserialize(sprofile);
	    if (sp==null) {
		return false;
	    }
	    RemoteUAAL node=(RemoteUAAL)nodes.get(id);
	    return node.isProfileAdded(sp.getURI());
	}else{
	    return false;
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.RemoteAPI#unregister(java.lang.String)
     */
    public void unregister(String id) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.unregister()", "Received call from remote node > UNREGISTER, sender: "+id);
	}
	if(!nodes.containsKey(id)){
	    throw new APIImplException("ID not registered");
	}
	RemoteUAAL uaal=nodes.remove(id);
	if(uaal!=null){
	    uaal.terminate();
	}else{
	    throw new APIImplException("No instance for this ID");
	}
    }
    
    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.RemoteAPI#unregisterAll()
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
