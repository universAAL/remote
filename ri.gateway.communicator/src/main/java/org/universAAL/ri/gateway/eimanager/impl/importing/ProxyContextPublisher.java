/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.ri.gateway.eimanager.impl.importing;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyContextPublisher extends ProxyBusMember {
    
    private DefaultContextPublisher publisher;
    
    private ContextSubscriber subscriber;
    
    public ProxyContextPublisher(ContextSubscriber subscriber, ContextProvider info, ImportedProxyManager manager, String targetId,
	    ModuleContext mc) {
	super(manager, targetId,"", mc);
	//TODO modify
	//this.publisher = new DefaultContextPublisher(mc, info);
	this.subscriber = subscriber;
    }
    
    public void publishContextEvent(ContextEvent event){
	subscriber.handleContextEvent(event);
	//publisher.publish(event);
    }
    
    public void removeProxy() {
	publisher.close();
    }
    
    public String getId() {
	return (publisher != null)? publisher.getMyID() : "SHOULD NOT BE SEEN";
    }
}
