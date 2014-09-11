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
package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.IOException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

@Deprecated
public class ProxyContextSubscriber extends ProxyBusMember {

    private final ContextSubscriber subscriber;

    public ProxyContextSubscriber(final ExportedProxyManager manager,
	    final ModuleContext mc, final ContextEventPattern[] subscriptions) {
	super(manager, "", "", mc);
	subscriber = new ProxiedContextSubscriber(mc, subscriptions);
    }

    class ProxiedContextSubscriber extends ContextSubscriber {

	protected ProxiedContextSubscriber(final ModuleContext context,
		final ContextEventPattern[] initialSubscriptions) {
	    super(context, initialSubscriptions);
	}

	@Override
	public void communicationChannelBroken() {
	}

	@Override
	public void handleContextEvent(final ContextEvent event) {
	    try {
		((ExportedProxyManager) getManager()).handleContextEvent(
			subscriber.getMyID(), event);
	    } catch (final IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    @Override
    public void removeProxy() {
	subscriber.close();
    }

    @Override
    public String getId() {
	return subscriber.getMyID();
    }
}
