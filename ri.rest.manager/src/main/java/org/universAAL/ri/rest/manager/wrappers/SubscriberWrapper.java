/*
	Copyright 2015 ITACA-SABIEN, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (SABIEN)
	
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
package org.universAAL.ri.rest.manager.wrappers;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.push.PushException;
import org.universAAL.ri.rest.manager.push.PushManager;
import org.universAAL.ri.rest.manager.resources.Subscriber;

public class SubscriberWrapper extends ContextSubscriber{
    
    private Subscriber resource;
    private String tenant;

    public Subscriber getResource() {
        return resource;
    }

    public void setResource(Subscriber resource) {
        this.resource = resource;
    }

    public SubscriberWrapper(ModuleContext connectingModule,
	    ContextEventPattern[] initialSubscriptions, Subscriber r, String t) {
	super(connectingModule, initialSubscriptions);
	resource=r;
	tenant=t;
    }

    @Override
    public void communicationChannelBroken() {
	Activator.logW("SubscriberWrapper.communicationChannelBroken", "communication Channel Broken");
    }

    @Override
    public void handleContextEvent(ContextEvent event) {
	Activator.logI("SubscriberWrapper.handleContextEvent",
		"Received Context Event "+event.getURI()+" for tenant " + tenant
			+ ". Sending to callback");
	try {
	    String callback=resource.getCallback();
	    if(callback==null || callback.isEmpty()){
		//Use generic callback of the tenant
		SpaceWrapper t = UaalWrapper.getInstance().getTenant(tenant);
		if(t!=null){
		    callback=t.getResource().getCallback();
		    if(callback==null){
			return;
		    }
		}
	    }
	    PushManager.pushContextEvent(callback, resource.getId(), event);
	} catch (PushException e) {
	    Activator.logE("SubscriberWrapper.handleContextEvent", e.toString());
	    e.printStackTrace();
	}
    }

}
