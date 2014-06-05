/*
	Copyright 2008-2010 Vodafone Italy, http://www.vodafone.it
	Vodafone Omnitel N.V.
	
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
package org.universAAL.ri.servicegateway.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.ri.servicegateway.GatewayPort;

/**
 * Main class for HTTP Services Gateway. It handles the registration of servlets
 * which provides different services based on URI. It register a Login servlet
 * and uses a Service Tracker to track for {@link GatewayPort} objects and
 * register them using the <code>url</code> and <code>dataDir</code> methods of
 * GatewayPort.
 * 
 * @author Mario Latronico
 * @author eandgrg
 * 
 */

public class Activator implements BundleActivator {

    public static BundleContext context;
    /**
     * universAAL {@link ModuleContext}
     */
    protected static ModuleContext mcontext;

    GatewayPortTracker gatewayPortTracker;

    public void start(BundleContext context) {

	Activator.context = context;
	BundleContext[] bc = { context };
	Activator.mcontext = uAALBundleContainer.THE_CONTAINER
		.registerModule(bc);

	// start the tracker on the GatewayPort name
	gatewayPortTracker = new GatewayPortTracker(context, mcontext,
		GatewayPort.class.getName(), null); // no service tracker
	// customization
	gatewayPortTracker.open();

    }

    public void stop(BundleContext context) throws Exception {
	gatewayPortTracker.close();
    }
    
    public static ModuleContext getModuleContext() {
	return mcontext;
    }

} // end class

