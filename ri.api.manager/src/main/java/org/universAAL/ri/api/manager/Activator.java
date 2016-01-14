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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.ri.api.manager.push.CryptUtil;
import org.universAAL.ri.api.manager.server.Authenticator;
import org.universAAL.ri.api.manager.server.RemoteServlet;
import org.universAAL.ri.api.manager.server.persistence.Persistence;

/**
 * OSGi Activator to start everything.
 * 
 * @author alfiva
 * 
 */
public class Activator implements BundleActivator {
    /**
     * OSGi Context
     */
    private BundleContext osgiContext;
    /**
     * uAAL Context
     */
    private static ModuleContext uaalContext;
    /**
     * Identifies if configured to use hardcoded servlet registration with own
     * authenticator as opposed to using a web container.
     */
    private static boolean hard = Configuration.getHardcoded();
    /**
     * Identifies if an encryption key has been setup and therefore encryption
     * is enabled for communication with GCM.
     */
    private static boolean crypt = Configuration.getGCMCrypt() != null;
    /**
     * Singleton instance of the actual RemoteAPI
     */
    private static RemoteAPIImpl remoteAPI;
    /**
     * Singleton instance of uAAL serializer
     */
    private static MessageContentSerializerEx parser;
    /**
     * Singleton instance of the persistence DB engine
     */
    private static Persistence persistence;
    /**
     * Context path for the server URL (used only if hard=true)
     */
    private static final String URL = Configuration.getContext();
    /**
     * Instance of authentication-enabled HttpContext for the servlet (used only
     * if hard=true)
     */
    private Authenticator auth;
    /**
     * Instance of the servlet (used only if hard=true)
     */
    private HttpServlet remoteServlet;
    /**
     * OSGi service reference holders
     */
    private ServiceReference[] referencesHttp, referencesSerializer;
    /**
     * OSGi service listener for HTTP (used only if hard=true)
     */
    private HttpListener httpListener;
    /**
     * OSGi service listener for uAAL serializer
     */
    private SerializerListener serializerListener;
    /**
     * Creates a Thread Pool to be used throughout the manager (currently only
     * in CListeners)
     */
    private static ExecutorService threadsPool;
    // For CXF
    // private ServiceRegistration registration = null;

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bcontext) throws Exception {
	osgiContext = bcontext;
	uaalContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });
	
	//Find uAAL serializer
	serializerListener=new SerializerListener();
	String filter = "(objectclass="+ MessageContentSerializerEx.class.getName() + ")";
	osgiContext.addServiceListener(serializerListener, filter);
	referencesSerializer = osgiContext.getServiceReferences((String)null,filter);
	for (int i = 0; referencesSerializer != null && i < referencesSerializer.length; i++) {
	    serializerListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    referencesSerializer[i]));
	}
	
	// Initialize encryption if enabled
	if (crypt) CryptUtil.init(Configuration.getGCMCrypt());
	
	//Instance the API impl before DB is ready (it wont be called yet) then the servlet, then the DB
	remoteAPI=new RemoteAPIImpl(uaalContext);
	remoteServlet=new RemoteServlet(remoteAPI);
	threadsPool=Executors.newFixedThreadPool(100);//TODO check what amount is best
	
	//Instance persistence DB once API impl is ready and before it is public in servlet
	try{
	persistence = (Persistence) Class.forName(Configuration.getDBClass())
		.getConstructor(new Class[] {}).newInstance(new Object[] {});
	persistence.init(remoteAPI);
	persistence.restore();
	} catch (RuntimeException ex){
	    ex.printStackTrace();
	} catch (Exception e) {
	    // If we cannot get the Backend, abort.
	    String cause = "The store implementation passed as configuration"
		    + " parameter could not be used. Make sure it is a "
		    + "class that implements "
		    + "org.universAAL.context.che.database.Backend or "
		    + "remove that configuration parameter to use the "
		    + "default engine.";
	    LogUtils.logError(uaalContext, this.getClass(), "start",
		new Object[] { cause }, null);
	}
	
	// For CXF
//	Dictionary<String, Object> props = new Hashtable<String, Object>();
//	props.put("service.exported.interfaces", "*");
//	props.put("service.exported.configs", "org.apache.cxf.rs");
////	props.put("service.exported.intents", "HTTP");
//	props.put("org.apache.cxf.rs.httpservice.context", "http://localhost:9000/remote");
////	props.put("org.apache.cxf.rs.address", "/remote");
//	registration = osgiContext.registerService(RemoteAPI.class.getName(),
//                new RemoteAPIImpl(uaalContext), props);
	
	// Custom servlet with own authenticator. If disabled the servlet and
	// its authentication will have to be setup by Pax Web (I couldnt make
	// it work not even as a .war)
	if (hard) {
	    auth = new Authenticator();
	    httpListener=new HttpListener();
	    filter = "(objectclass=" + HttpService.class.getName() + ")";
	    osgiContext.addServiceListener(httpListener, filter);
	    referencesHttp = osgiContext.getServiceReferences((String)null, filter);
	    //TODO If there are more than 1 HttpService, it gets registered in all !
	    for (int i = 0; referencesHttp != null && i < referencesHttp.length; i++) {
		httpListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, referencesHttp[i]));
	    }
	}
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bcontext) throws Exception {
	// For CXF
//	if (registration != null) {
//	    registration.unregister();
//	}
	for (int i = 0; referencesHttp != null && i < referencesHttp.length; i++) {
	    httpListener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, referencesHttp[i]));
	}
	for (int i = 0; referencesSerializer != null && i < referencesSerializer.length; i++) {
	    serializerListener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, referencesSerializer[i]));
	}
	//Stop all wrappers, without affecting the backup DB.
	threadsPool.shutdownNow();
	remoteAPI.unregisterAll();
	remoteServlet=null;
	remoteAPI=null;
	auth=null;
    }

    /**
     * Registers the Servlet into the OSGi HTTP service.
     * 
     * @param http
     *            The referenced OSGi HTTP service
     * @return true if it managed to register
     */
    public boolean register(HttpService http) {
	try {
	    http.registerServlet(URL, remoteServlet, null, auth);
	} catch (ServletException e) {
	    LogUtils.logError( uaalContext, this.getClass(), "register",
		    new Object[] { "Exception while registering Servlet." }, e);
	    return false;
	} catch (NamespaceException e) {
	    LogUtils.logError(uaalContext, this.getClass(), "register",
		    new Object[] { "Servlet Namespace exception; URL is already in use." }, e);
	    return false;
	}
	LogUtils.logInfo(uaalContext, this.getClass(), "register",
		new Object[] { "Servlet started." }, null);
	return true;
    }

    /**
     * Unregisters the Servlet from the OSGi HTTP service.
     * 
     * @param http
     *            The referenced OSGi HTTP service
     * @return true if it managed to unregister
     */
    public boolean unregister(HttpService http) {
	try {
	    http.unregister(URL);
	} catch (IllegalArgumentException e) {
	    LogUtils.logError(uaalContext, this.getClass(), "unregister",
		    new Object[] { "Servlet cannot be unregistered: illegal argument." }, e);
	    return false;
	}
	LogUtils.logInfo(uaalContext, this.getClass(), "unregister",
		new Object[] { "Servlet stopped." }, null);
	return true;
    }
    
    /**
     * Listener for reacting to the presence of OSGi HTTP services
     * 
     * @author alfiva
     * 
     */
    private class HttpListener implements ServiceListener{
	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
		    register((HttpService) osgiContext.getService(event.getServiceReference()));
		    break;
		case ServiceEvent.UNREGISTERING:
		    unregister((HttpService) osgiContext.getService(event.getServiceReference()));
		    break;
		default:
		    break;
		}
	    }
    }
    
    /**
     * Listener for reacting to the presence of uAAL Serializers
     * 
     * @author alfiva
     * 
     */
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
    
    /**
     * Get the instance of the Persistence engine
     * 
     * @return the Persistence engine
     */
    public static Persistence getPersistence() {
	return persistence;
    }

    /**
     * Get the instance of the Remote API impl
     * 
     * @return the Remote API impl
     */
    public static RemoteAPIImpl getRemoteAPI() {
	return remoteAPI;
    }

    /**
     * Get the instance of the uAAL serializer
     * 
     * @return the uAAL serializer
     */
    public static MessageContentSerializerEx getParser() {
	return parser;
    }

    /**
     * Get the value of the "hard" variable identifying if a hardcoded
     * authenticated servlet is being registered programmatically
     * 
     * @return true if it is using the hardcoded servlet registration with
     *         authentication.
     */
    public static boolean isHardcoded() {
	return hard;
    }
    
    /**
     * Identifies if an encryption key has been setup and therefore encryption
     * is enabled for communication with GCM.
     * 
     * @return true if encryption is enabled.
     */
    public static boolean isGCMEncrypted() {
	return crypt;
    }
    
    /**
     * Get an instance of ExecutorService that provides access to a Fixed size
     * Thread Pool
     * 
     * @return ExecutorService instance
     */
    public static ExecutorService getThreadsPool() {
        return threadsPool;
    }
	
}
