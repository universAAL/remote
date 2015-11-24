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
package org.universAAL.ri.rest.manager;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.ri.rest.manager.resources.Uaal;
import org.universAAL.ri.rest.manager.server.Authenticator;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;

public class Activator implements BundleActivator {
    private static BundleContext osgiContext = null;
    private static ModuleContext uaalContext = null;

    private SerializerListener serializerListener;
    private ServiceReference[] referencesSerializer;
    private static MessageContentSerializerEx parser;

    private TenantListener tenantListener = null;
    private ServiceReference[] tenantRefs;
    private static TenantManager tenantMngr = null;

    public static final String TYPES="application/json, application/xml;charset=UTF-8;version=1, text/xml;charset=UTF-8;version=1, application/octet-stream";
    public static final String TYPES_TXT="text/plain;charset=UTF-8";

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.uaalContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });

	// Find tenant manager
	tenantListener = new TenantListener();
	String filter = "(objectclass=" + TenantManager.class.getName() + ")";
	osgiContext.addServiceListener(tenantListener, filter);
	tenantRefs = osgiContext.getServiceReferences((String) null, filter);
	for (int i = 0; tenantRefs != null && i < tenantRefs.length; i++) {
	    tenantListener.serviceChanged(new ServiceEvent(
		    ServiceEvent.REGISTERED, tenantRefs[i]));
	}
	
	//Find uAAL serializer
	serializerListener=new SerializerListener();
	String filter2 = "(objectclass="+ MessageContentSerializerEx.class.getName() + ")";
	osgiContext.addServiceListener(serializerListener, filter2);
	referencesSerializer = osgiContext.getServiceReferences((String)null,filter2);
	for (int i = 0; referencesSerializer != null && i < referencesSerializer.length; i++) {
	    serializerListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    referencesSerializer[i]));
	}

	// Start REST servlet
	JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
	sf.setResourceClasses(Uaal.class);
	sf.setProvider(new Authenticator());
	sf.setResourceProvider(Uaal.class, new SingletonResourceProvider(
		new Uaal()));
	sf.setAddress("http://localhost:9000/");
	BindingFactoryManager manager = sf.getBus().getExtension(
		BindingFactoryManager.class);
	JAXRSBindingFactory factory = new JAXRSBindingFactory();
	factory.setBus(sf.getBus());
	manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID,
		factory);
	sf.create();
    }

    public void stop(BundleContext arg0) throws Exception {
	for (int i = 0; tenantRefs != null && i < tenantRefs.length; i++) {
	    tenantListener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, tenantRefs[i]));
	}
	UaalWrapper.getInstance().close();
    }

    private class TenantListener implements ServiceListener {
	public void serviceChanged(ServiceEvent event) {
	    switch (event.getType()) {
	    case ServiceEvent.REGISTERED:
	    case ServiceEvent.MODIFIED:
		tenantMngr = (TenantManager) osgiContext.getService(event
			.getServiceReference());
		break;
	    case ServiceEvent.UNREGISTERING:
		tenantMngr = null;
		break;
	    default:
		break;
	    }
	}
    }
    
    private class SerializerListener implements ServiceListener{
	public void serviceChanged(ServiceEvent event) {
	    switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
		    parser=(MessageContentSerializerEx) osgiContext.getService(event.getServiceReference());
		    break;
		case ServiceEvent.UNREGISTERING:
		    parser=null;
		    break;
		default:
		    break;
		}
	    }
    }
    
    public static MessageContentSerializerEx getParser() {
        return parser;
    }

    public static TenantManager getTenantMngr() {
        return tenantMngr;
    }
    
    public static ModuleContext getUaalContext() {
        return uaalContext;
    }
    
    /**
     * Helper method to log DEBUG messages
     * 
     * @param method
     *            The method calling to log
     * @param msg
     *            The messge to log
     */
    public static void logD(String method, String msg){
	LogUtils.logDebug(uaalContext, Activator.class, method, msg);
    }
    
    /**
     * Helper method to log INFO messages
     * 
     * @param method
     *            The method calling to log
     * @param msg
     *            The messge to log
     */
    public static void logI(String method, String msg){
	LogUtils.logInfo(uaalContext, Activator.class, method, msg);
    }
    
    /**
     * Helper method to log WARN messages
     * 
     * @param method
     *            The method calling to log
     * @param msg
     *            The messge to log
     */
    public static void logW(String method, String msg){
	LogUtils.logWarn(uaalContext, Activator.class, method, msg);
    }
    
    /**
     * Helper method to log ERROR messages
     * 
     * @param method
     *            The method calling to log
     * @param msg
     *            The messge to log
     */
    public static void logE(String method, String msg){
	LogUtils.logError(uaalContext, Activator.class, method, msg);
    }
    
}
