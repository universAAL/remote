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

package org.universAAL.ri.gateway.communicator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.ri.gateway.communicator.service.impl.CommunicatorStarter;
import org.universAAL.ri.gateway.eimanager.ExportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.ImportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.impl.EIManagerRegistryListener;
import org.universAAL.ri.gateway.eimanager.impl.EIOperationManager;
import org.universAAL.ri.gateway.eimanager.impl.ExportManagerImpl;
import org.universAAL.ri.gateway.eimanager.impl.ImportManagerImpl;
import org.universAAL.ri.gateway.eimanager.impl.security.ExportSecurityOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.impl.security.ImportSecurityOperationInterceptor;

/**
 * Bundle's activator. Starts a default instance of the GatewayCommunicator
 * worker by use of GatewayCommunicatorInstantiator.
 * 
 * @author skallz
 * 
 */
public class Activator implements BundleActivator {

    /**
     * the communicator's starter.
     */
    private CommunicatorStarter inst;
    public static BundleContext bc;
    public static ModuleContext mc;

    private IBusMemberRegistry registry;

    private IBusMemberRegistryListener exportRegistryListener;
    private IBusMemberRegistryListener importRegistryListener;

    private ExportManagerImpl exportManager;
    private ImportManagerImpl importManager;
    /**
     * Starts the communicator.
     * 
     * @param context
     *            bundle context
     */
    public void start(final BundleContext context) throws Exception {
	bc = context;
	
	mc = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	
	inst = new CommunicatorStarter(context);

	exportManager = new ExportManagerImpl(inst.getCommunicator());
	importManager = new ImportManagerImpl(inst.getCommunicator());
	
	inst.setManagers(importManager, exportManager);

	registry = (IBusMemberRegistry) mc.getContainer().fetchSharedObject(mc,
		IBusMemberRegistry.busRegistryShareParams);

	exportRegistryListener = new EIManagerRegistryListener(exportManager);
	
	importRegistryListener = new EIManagerRegistryListener(importManager);
	
	registry.addBusRegistryListener(exportRegistryListener, true);
	registry.addBusRegistryListener(importRegistryListener, true);
	
	EIOperationManager.Instance.init();
	
	Activator.mc.getContainer().shareObject(Activator.mc, new ImportSecurityOperationInterceptor(),
			new Object[] { ImportOperationInterceptor.class.getName() });
	
	Activator.mc.getContainer().shareObject(Activator.mc, new ExportSecurityOperationInterceptor(),
			new Object[] { ExportOperationInterceptor.class.getName() });
    
    }

    /**
     * Stops the communicator and all used resources.
     * 
     * @param context
     *            bundle context
     */
    public void stop(final BundleContext context) {
	inst.stop();
	if (registry!= null){
	    registry.removeBusRegistryListener(exportRegistryListener);
	    registry.removeBusRegistryListener(importRegistryListener);
	}
	
	if (exportManager != null){
	    exportManager.shutdown();
	}
    }

}
