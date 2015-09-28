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

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.support.utils.context.mid.UtilPublisher;
import org.universAAL.ri.api.manager.push.PushManager;

/**
 * Extension of the Utility API UAAL helper class that adds a few small tweaks
 * to acomodate remote node information.
 * 
 * @author alfiva
 * 
 */
public class RemoteUAAL {
    
    /**
     * These lists contain the URIs of CEPs and SProfiles registered so far.
     * They are used to disallow registration of the same CEP/Profile more than
     * once (which may happen with unreliable clients that dont unregister).
     */ //TODO migrate this to the original UAAL
    private List<String> cepsList = new ArrayList<String>();
    private List<String> sprofilesList = new ArrayList<String>();
    
    private MyServiceCallee scee;
    private ServiceCaller scer;
    private ContextPublisher cpub;
    private MyContextSubscriber csub;
    private ModuleContext context;
    
    public RemoteUAAL(ModuleContext context) {
	this.context=context;
    }

    /**
     * Get the list of context event patterns of subscribers registered so far.
     * 
     * @return The list of URIs of context event patterns of subscribers
     */
    public List<String> getCEPsList() {
	return cepsList;
    }

    /**
     * Get the list of service profiles of callees registered so far.
     * 
     * @return The list of URIs of service profiles of callees
     */
    public List<String> getSProfilesList() {
	return sprofilesList;
    }
    
    public void sendC(ContextEvent e) {
	ContextProvider cp = e.getProvider();
	// Because we are building the provider here, it will not be the same object
	// This would fail the match in bus and not send the event. Remove it so it is set by the bus:
	if(cp!=null) e.changeProperty(ContextEvent.PROP_CONTEXT_PROVIDER,null);
	if (cpub == null) {
	    cpub = new UtilPublisher(
		    context,
		    "http://ontology.universAAL.org/SimpleUAAL.owl#ContextEventsProvider",
		     ContextProviderType.controller, (String) null, null,
		    null);

	}//TODO Get rid of uaal utils and use a normal publisher
	cpub.publish(e);
    }

    public void subscribeC(ContextEventPattern[] p) {
	for (int i = 0; i < p.length; i++) {
	    if (!cepsList.contains(p[i].getURI())) {
		// Only register if not already
		if(csub!=null){
		    csub.addMorePatterns(new ContextEventPattern[]{p[i]});
		}else{
		    csub=new MyContextSubscriber(context, new ContextEventPattern[]{p[i]});
		}
		cepsList.add(p[i].getURI());
	    }// TODO else log
	}
    }
    
    public boolean isPatternAdded(String uri){
	return sprofilesList.contains(uri);
    }


    public void provideS(ServiceProfile[] p) {
	// This is like super. , but handles the list of registered Profiles
	for (int i = 0; i < p.length; i++) {
	    if (!sprofilesList.contains(p[i].getURI())) {
		// Only register if not already
		if(scee!=null){
		    scee.addMoreProfiles(new ServiceProfile[]{p[i]});
		}else{
		    scee=new MyServiceCallee(context, new ServiceProfile[]{p[i]});
		}
		sprofilesList.add(p[i].getURI());
	    }// TODO else log
	}
    }
    
    public ServiceResponse callS(ServiceRequest r) {
	if (scer == null) {
	    scer = new DefaultServiceCaller(context);
	}
	return scer.call(r);
    }
    
    public boolean isProfileAdded(String uri){
	return sprofilesList.contains(uri);
    }

    public void terminate() {
	scer.close();
	scee.close();
	csub.close();
	cpub.close();
	cepsList.clear();
	sprofilesList.clear();
    }
    
    /**
     * Custom ISListener to be used in the callS() method of the Utility API. It
     * has access to the client remote node endpoint information.
     * 
     * This is called from the single ServiceStrategy thread. This method will
     * perform network operations, which will take time. Unfortunately it cannot
     * use a Thread for them because it is a synchronous execution that must
     * return a response, and it would block anyway.
     * 
     * @author alfiva
     * 
     */
    public class MyServiceCallee extends ServiceCallee {
	protected MyServiceCallee(ModuleContext context,
		ServiceProfile[] realizedServices) {
	    super(context, realizedServices);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * This is called everytime a ServiceCall is addressed to a remote node.
	 * It will pack the callback message and send it to the client remote
	 * node endpoint.
	 * 
	 * @param call
	 *            The call to send back to the client
	 * @return The response that the client will have created
	 */
	public ServiceResponse handleCall(ServiceCall call) {
	    try {
		List<String> scopes = call.getScopes();
		ServiceResponse finalresp=new ServiceResponse(CallStatus.succeeded);
		if(scopes.isEmpty()){
		    Activator.logE("CListener.handleCall", "No scopes");
			return new ServiceResponse(CallStatus.denied);
		}
		for(String scope:scopes){
		    if(call.isSerializableTo(scope)){ //MULTITENANT The call is for this scope
			String procuri=call.getProcessURI();
			String spuri=procuri.substring(0, procuri.length()-7);//uri-"Process"
			ServiceResponse partialresp = PushManager.callS(scope, RemoteAPIImpl.scopesToRemotes.get(scope), call, spuri);
			for(ProcessOutput out:partialresp.getOutputs()){
			    finalresp.addOutput(out);
			}
		    }
		}
		return finalresp;
	    } catch (Exception e) {
		e.printStackTrace();
		Activator.logE("CListener.handleCall",
			"Unable to send the proxied Service Call to the remote node. "
				+ e.getMessage());
		return new ServiceResponse(CallStatus.serviceSpecificFailure);
	    }
	}
	
	public void addMoreProfiles(ServiceProfile[] sp){
	    this.addNewServiceProfiles(sp);
	}

	@Override
	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	}
    }
    
    /**
     * Custom ICListener to be used in the sendC() method of the Utility API. It
     * has access to the client remote node endpoint information.
     * 
     * @author alfiva
     * 
     */
    public class MyContextSubscriber extends ContextSubscriber {
	protected MyContextSubscriber(ModuleContext connectingModule,
		ContextEventPattern[] initialSubscriptions) {
	    super(connectingModule, initialSubscriptions);
	    // TODO Auto-generated constructor stub
	}

	/**
	 * This is called everytime a ContextEvent is addressed to a remote
	 * node. It will pack the callback message and send it to the client
	 * remote node endpoint.
	 * 
	 * This is called from the single ContextStrategy thread. This method
	 * will perform network operations, which will take time, so it uses an
	 * inner thread every time is called.
	 * 
	 * @param event
	 *            The event to send back to the client
	 */
	public void handleContextEvent(final ContextEvent event) {
	    Activator.getThreadsPool().execute(
		    new Thread("RemoteUAAL_CListener") {
			public void run() {
			    try {
				List<String> scopes = event.getScopes();
				for(String scope:scopes){
				    if(event.isSerializableTo(scope)){ //MULTITENANT The call is for this scope
				    PushManager.sendC(scope, RemoteAPIImpl.scopesToRemotes.get(scope), event);
				} //MULTITENANT The call is NOT for this scope > ignore
				}
				
			    } catch (Exception e) {
				e.printStackTrace();
				Activator.logE("CListener.handleContextEvent",
					"Unable to send the proxied Context Event to the remote node. "
						+ e.getMessage());
			    }
			}
		    });
	}

	@Override
	public void communicationChannelBroken() {
	    // TODO Auto-generated method stub
	}
	
	public void addMorePatterns(ContextEventPattern[] cp){
	    this.addNewRegParams(cp);
	}
    }
}
