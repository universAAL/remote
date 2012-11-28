package org.universAAL.ri.gateway.communicator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.ri.gateway.communicator.service.impl.CommunicatorStarter;
import org.universAAL.ri.gateway.eimanager.impl.EIManagerRegistryListener;
import org.universAAL.ri.gateway.eimanager.impl.ExportManagerImpl;
import org.universAAL.ri.gateway.eimanager.impl.ImportManagerImpl;

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
