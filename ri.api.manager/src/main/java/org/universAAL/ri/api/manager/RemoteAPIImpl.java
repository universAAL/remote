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

import java.util.HashMap;
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
    public static HashMap<String, String> scopesToRemotes = new HashMap<String, String>();
    
    private static RemoteUAAL uAALAPI ;
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
	uAALAPI = new RemoteUAAL(context);
    }

    public void register(String idx, String remote) throws APIImplException{
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.register()", "Received call from remote node > REGISTER, sender: "+idx);
	}
	if (Configuration.determineEndpoint(remote) == RemoteAPI.REMOTE_UNKNOWN){ // No POST nor GCM
	    throw new APIImplException("Unable to determine protocol of remote endpoint");
	}
	scopesToRemotes.put(idx, remote);
    }

    public void sendC(String idx, String cevent) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.sendC()", "Received call from remote node > SENDC, sender: "+idx);
	}
	if(uAALAPI!=null && scopesToRemotes.containsKey(idx)){//Just to pretend we need register
	    ContextEvent ce=(ContextEvent) Activator.getParser().deserialize(cevent);
	    if (ce==null) {
		throw new APIImplException("Unable to deserialize event");
	    }
	    ce.addScope(idx); // MULTITENANT Add my scope
	    ce.setProperty(ContextEvent.PROP_ORIG_SCOPE, idx); //? MULTITENANT Add my scope as origin, so I dont get it back 
	    uAALAPI.sendC(ce);
	}else{
	    throw new APIImplException("ID not registered");
	}
    }

    public void subscribeC(String idx, String cpattern) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.subscribeC()", "Received call from remote node > SUBSCRIBEC, sender: "+idx);
	}
	if(uAALAPI!=null && scopesToRemotes.containsKey(idx)){//Just to pretend we need register
	    ContextEventPattern cp=(ContextEventPattern) Activator.getParser().deserialize(cpattern);
	    if (cp==null) {
		throw new APIImplException("Unable to deserialize pattern");
	    }
	    uAALAPI.subscribeC(new ContextEventPattern[]{cp});
	}else{
	    throw new APIImplException("ID not registered");
	}
    }
    
    public boolean isPatternAdded(String idx, String cpattern) {
	if(uAALAPI!=null){
	    ContextEventPattern cp=(ContextEventPattern) Activator.getParser().deserialize(cpattern);
	    if (cp==null) {
		return false;
	    }
	    return uAALAPI.isPatternAdded(cp.getURI());
	}else{
	    return false;
	}
    }

    public String callS(String idx, String srequest) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.callS()", "Received call from remote node > CALLS, sender: "+idx);
	}
	ServiceResponse res = null;
	if (uAALAPI!=null && scopesToRemotes.containsKey(idx)){//Just to pretend we need register
	    ServiceRequest req = (ServiceRequest) Activator.getParser().deserialize(srequest);
	    if (req == null) {
		throw new APIImplException("Unable to deserialize request");
	    }
	    req.addScope(idx); // MULTITENANT Add my scope
	    req.setProperty(ContextEvent.PROP_ORIG_SCOPE, idx); // MULTITENANT Add my scope as origin, so I dont get it back 
	    res = uAALAPI.callS(req);
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

    public void provideS(String id, String sprofile) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.provideS()", "Received call from remote node > PROVIDES, sender: "+id);
	}
	if(uAALAPI!=null && scopesToRemotes.containsKey(id)){//Just to pretend we need register
	    ServiceProfile sp=(ServiceProfile) Activator.getParser().deserialize(sprofile);
	    if (sp==null) {
		throw new APIImplException("Unable to deserialize profile");
	    }
	    uAALAPI.provideS(new ServiceProfile[]{sp});
	}else{
	    throw new APIImplException("ID not registered");
	}
    }
    
    public boolean isProfileAdded(String id, String sprofile) {
	if(uAALAPI!=null){
	    ServiceProfile sp=(ServiceProfile) Activator.getParser().deserialize(sprofile);
	    if (sp==null) {
		return false;
	    }
	    return uAALAPI.isProfileAdded(sp.getURI());
	}else{
	    return false;
	}
    }

    public void unregister(String id) throws APIImplException {
	if(Configuration.getLogDebug()){
	    Activator.logI("RemoteAPIImpl.unregister()", "Received call from remote node > UNREGISTER, sender: "+id);
	}
	if(!scopesToRemotes.containsKey(id)){
	    throw new APIImplException("ID not registered");
	}
	scopesToRemotes.remove(id);
	//TODO remove registrations of this scope
	//But now if not removed R API will try to send them to the remoteID and fail or timeout
    }
    
    public void unregisterAll() {
	scopesToRemotes.clear();
	uAALAPI.terminate();
    }
    
}
