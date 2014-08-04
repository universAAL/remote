/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

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
import org.universAAL.ioc.dependencies.DependencyProxy;
import org.universAAL.ioc.dependencies.impl.NPEDependencyProxy;
import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
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
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class Activator implements BundleActivator {

    /**
     * the communicator's starter.
     */
    private CommunicatorStarter inst;
    public static BundleContext bc;
    public static ModuleContext mc;

    public static Logger log;


    private IBusMemberRegistryListener exportRegistryListener;
    private IBusMemberRegistryListener importRegistryListener;

    private ExportManagerImpl exportManager;
    private ImportManagerImpl importManager;

    public static DependencyProxy<IBusMemberRegistry> registry;

    public static DependencyProxy<AALSpaceManager> spaceManager;

    public static DependencyProxy<TenantManager> tenantManager;

    public static DependencyProxy<MessageContentSerializerEx> serializer;

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

        log = LoggerFactory.createLoggerFactory(
                Activator.mc).getLogger(Activator.class);

        registry = new NPEDependencyProxy<IBusMemberRegistry>(mc,
                IBusMemberRegistry.busRegistryShareParams);

        spaceManager = new NPEDependencyProxy<AALSpaceManager>(mc,
                new Object[] { AALSpaceManager.class.getName() });

        serializer = new NPEDependencyProxy<MessageContentSerializerEx>(
                new Object[] { MessageContentSerializerEx.class.getName() } );


        tenantManager = new NPEDependencyProxy<TenantManager>(
                new Object[] { TenantManager.class.getName() } );

        checkDependencies();

        inst = new CommunicatorStarter(context);

        exportManager = new ExportManagerImpl(inst.getCommunicator());
        importManager = new ImportManagerImpl(inst.getCommunicator());

        inst.setManagers(importManager, exportManager);

        exportRegistryListener = new EIManagerRegistryListener(exportManager);

        importRegistryListener = new EIManagerRegistryListener(importManager);

        registry.getObject().addListener(exportRegistryListener, true);
        registry.getObject().addListener(importRegistryListener, true);

        EIOperationManager.Instance.init();

        Activator.mc.getContainer().shareObject(Activator.mc,
                new ImportSecurityOperationInterceptor(),
                new Object[] { ImportOperationInterceptor.class.getName() });

        Activator.mc.getContainer().shareObject(Activator.mc,
                new ExportSecurityOperationInterceptor(),
                new Object[] { ExportOperationInterceptor.class.getName() });

    }

    private void checkDependencies() {
        if ( registry == null || registry.getObject() == null ) {
            log.warning("Missing required shared "
                    + IBusMemberRegistry.class.getName()
                    + " object in the current run-time ");
        }
        if ( spaceManager == null || spaceManager.getObject() == null ) {
            log.warning("Missing required shared "
                    + AALSpaceManager.class.getName()
                    + " object in the current run-time ");
        }

        if ( serializer == null || serializer.getObject() == null ) {
            log.warning("Missing required shared "
                    + MessageContentSerializerEx.class.getName()
                    + " object in the current run-time ");
        }
    }

    /**
     * Stops the communicator and all used resources.
     *
     * @param context
     *            bundle context
     */
    public void stop(final BundleContext context) {
        inst.stop();
        if (registry.getObject() != null) {
            registry.getObject().removeListener(exportRegistryListener);
            registry.getObject().removeListener(importRegistryListener);
        }

        if (exportManager != null) {
            exportManager.shutdown();
        }
    }

}
