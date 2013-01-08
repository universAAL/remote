/*	
	Copyright 2010-2014 UPM http://www.upm.es
	Universidad Polit�cnica de Madrdid
	
	OCO Source Materials
	� Copyright IBM Corp. 2011
	
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
package org.universAAL.rinterop.profile.agent.osgi;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.universAAL.commerce.ustore.tools.OnlineStoreManager;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.service.ServiceResponse;
//import org.universAAL.rinterop.profile.agent.IDeployManager;
import org.universAAL.ucc.deploymanagerservice.DeployManagerService;
import org.universAAL.rinterop.profile.agent.ProfileProvider;
import org.universAAL.rinterop.profile.ws.SoapExportServlet;

/**
 * @author
 */
public class Activator implements BundleActivator {

	public static BundleContext context = null;

	private static final String PROFILE_PROVIDER_ALIAS = "/ustoreagent";

	private static final String DEPLOY_MANAGER_ALIAS = "/deploymanager";

	private static final String STORE_MANAGER_SERVICE_ADDRESS = "http://srv-ustore.haifa.il.ibm.com:9060/universAAL/OnlineStoreManagerService/OnlineStoreManagerService.wsdl";

	private ModuleContext moduleContext = null;

	// private ServiceTracker httpServiceTracker;

	private HttpService httpService;
	private boolean profileProviderRegistered = false;
	private boolean deployManagerRegistered = false;

	private ProfileProvider provider;

	private DeployManagerService deployManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;

		moduleContext = uAALBundleContainer.THE_CONTAINER
				.registerModule(new BundleContext[] { context });

		createOnlineStoreManagerClient();
		// createDynamicDeploymentManagerClient();

//		System.out
//				.println("Is  ContextProvider registered:"
//						+ OntologyManagement
//								.getInstance()
//								.isRegisteredClass(
//										"http://ontology.universAAL.org/Context.owl#ContextProvider",
//										true));
		// context.addServiceListener(this);
		// // context.addBundleListener(new uAALBundleExtender(context));

		// count_activator++;
		// System.out.println("count_activator: " + count_activator);

		provider = new ProfileProvider(moduleContext);

		String userURI = "http://ontology.universAAL.org/ContextHistoryHTLImpl.owl#dummyUser";

		// UserProfile userProfile = new UserProfile(userURI);
		// ProfileOntology userProfileOnt = new ProfileOntology();
		// OntologyManagement.getInstance().register(userProfileOnt);

		/*test*/
		ServiceResponse sr = provider.getAllUserProfiles(userURI);
		
		Object httpServiceObj = moduleContext.getContainer().fetchSharedObject(
				moduleContext, new Object[] { HttpService.class.getName() });
		httpService = (HttpService) httpServiceObj;
		System.out
				.println("[ProfileAgent]Fetch HttpServiceObj: " + httpService);

		deployManager = (DeployManagerService) moduleContext.getContainer()
				.fetchSharedObject(moduleContext,
						new Object[] { DeployManagerService.class.getName() });

		// callback = new
		// ContextHistoryProfileAgent(uAALBundleContainer.THE_CONTAINER
		// .registerModule(new BundleContext[] { context }));
		// TaskEndpointServlet.checkSessions = true;
		// new Thread(TaskEndpointServlet.runableObj).start();

		// httpServiceTracker = new ServiceTracker(context,
		// HttpService.class.getName(), this);
		// httpServiceTracker.open();

		// transformerTracker = new ServiceTracker(context,
		// UITransformer.class.getName(), null);
		// transformerTracker.open();

		if (httpService != null) {
			httpService.createDefaultHttpContext();

			if (!profileProviderRegistered) {
				registerProfileProvider(httpService, provider);
				profileProviderRegistered = true;
			}

			if (!deployManagerRegistered) {
				registerDeployManagerService(httpService, deployManager);
				deployManagerRegistered = true;
			}

			// create AALuisTask customizer class and tracker
			// AALuisTaskTrackerCustomizer taskCustomizer = new
			// AALuisTaskTrackerCustomizer(
			// this, context, httpService, httpContext);
			// taskCustomizers.put(httpService, taskCustomizer);
			// ServiceTracker taskServiceTracker = new ServiceTracker(context,
			// AALuisTask.class.getName(), taskCustomizer);
			// taskServiceTracker.open();
			// taskTrackers.put(httpService, taskServiceTracker);
		}

		// callbackReg =
		// context.registerService(ProfileProvider.class.getName(),
		// provider, null);// ProfileCHEProvider -> ProfileProvider,
		// // callback -> provider
	}

	// class JQuerySevlet extends HttpServlet {
	//
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	// throws ServletException, IOException {
	// StreamSource ss = new StreamSource(
	// Activator.class
	// .getResourceAsStream("/resources/jquery-1.7.1.js"));
	// InputStream is = ss.getInputStream();
	// BufferedReader bufferedReader = new BufferedReader(
	// new InputStreamReader(is));
	// String line = null;
	// while ((line = bufferedReader.readLine()) != null) {
	// resp.getOutputStream().println(line);
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		// TaskEndpointServlet.checkSessions = false;

		// callbackReg.unregister();
		// callbackReg = null;
		if (httpService != null) {
			unregisterProfileProvider(httpService);
			unregisterDeployManager(httpService);
		}

		// unregister all task endpoints servlets
		// if (taskCustomizers != null) {
		// Set<HttpService> customizers = taskCustomizers.keySet();
		// for (HttpService hs : customizers) {
		// taskCustomizers.get(hs).unregisterAllTaskEndpoints();
		// }
		// taskCustomizers.clear();
		// }

		// close all task trackers
		// if (taskTrackers != null) {
		// Set<HttpService> trackers = taskTrackers.keySet();
		// for (HttpService hs : trackers) {
		// taskTrackers.get(hs).close();
		// }
		// taskTrackers.clear();
		// }

		// httpServiceTracker.close();
		// httpServiceTracker = null;

		httpService = null;
		// callback = null;
		context = null;

		provider = null;
	}

	private void createOnlineStoreManagerClient() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.getInInterceptors().add(new LoggingInInterceptor());
		factory.getOutInterceptors().add(new LoggingOutInterceptor());
		factory.setServiceClass(OnlineStoreManager.class);
		factory.setAddress(STORE_MANAGER_SERVICE_ADDRESS);

		// DeployManager deployManagerClient = (DeployManager)
		// factory.create();//trqbva da e interface

		// ModuleContext moduleContext = uAALBundleContainer.THE_CONTAINER
		// .registerModule(new BundleContext[] { getBundleContext() });
		// uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		// deployManagerClient, new Object[] { DeployManager.class.getName() });

		OnlineStoreManager client = (OnlineStoreManager) factory.create();
		uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext, client,
				new Object[] { OnlineStoreManager.class.getName() });
	}

	// private void createDynamicDeploymentManagerClient(){
	// JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
	// Client client = dcf.createClient(DEPLOYMENT_MANAGER_SERVICE_ADDRESS);
	// }

	// @Override
	// public void serviceChanged(ServiceEvent se) {
	// Object service = context.getService(se.getServiceReference());
	// System.out.println("SERVICE_CHANGED:"+service.getClass());
	// if (service instanceof LogListener) {
	// if (se.getType() == ServiceEvent.REGISTERED)
	// logListeners.add(service);
	// else if (se.getType() == ServiceEvent.UNREGISTERING)
	// logListeners.remove(service);
	// }
	// }

	// ******* ServiceTrackerCustomizer methods *******//

	// public Object addingService(ServiceReference reference) {
	//
	// HttpService service = (HttpService) context.getService(reference);
	//
	// if (service != null) {
	// httpContext = service.createDefaultHttpContext();
	//
	// if (!registered) {
	// registerCallback(service, provider);// callback -> provider
	// // HttpServlet jqueryServlet = new JQuerySevlet();
	// // try {
	// // service.registerServlet("/jquery-1.7.1.js", jqueryServlet,
	// // null, httpContext);
	// // } catch (ServletException e) {
	// // e.printStackTrace();
	// // } catch (NamespaceException e) {
	// // e.printStackTrace();
	// // }
	// registered = true;
	// }
	//
	// // Set<HttpService> httpServices = taskTrackers.keySet();
	// // boolean containHttpService = false;
	// // if (httpServices != null) {
	// // for (HttpService hs : httpServices) {
	// // if (service.equals(hs)) {
	// // containHttpService = true;
	// // break;
	// // }
	// // }
	// // }
	// // if (!containHttpService) {
	// // AALuisTaskTrackerCustomizer taskCustomizer = new
	// // AALuisTaskTrackerCustomizer(this, context, service, httpContext);
	// // taskCustomizers.put(service, taskCustomizer);
	// // debug("HttpCustomizer" + taskCustomizers.get(service));
	// // ServiceTracker taskServiceTracker = new ServiceTracker(context,
	// // ProfileCHEProvider.class.getName(), taskCustomizer);
	// // taskServiceTracker.open();
	// // taskTrackers.put(service, taskServiceTracker);
	// // }
	// }
	// return service;
	// }
	//
	// public void modifiedService(ServiceReference reference, Object service) {
	// }
	//
	// public void removedService(ServiceReference reference, Object o) {
	// HttpService service = (HttpService) context.getService(reference);
	//
	// if (service != null) {
	// debug("Removed HttpService method!");
	// // if (taskCustomizers.get(service) != null) {
	// // taskCustomizers.get(service).unregisterAllTaskEndpoints();
	// // taskCustomizers.remove(service);
	// // }
	// // if (taskTrackers.get(service) != null) {
	// // taskTrackers.get(service).close();
	// // taskTrackers.remove(service);
	// // }
	//
	// unregisterCallback(service);
	// context.ungetService(reference);
	// }
	// }

	private void registerProfileProvider(HttpService httpService,
			ProfileProvider provider) { // ProfileCHEProvider callback ->
										// ProfileProvider provider
		try {
			HttpServlet servlet = new SoapExportServlet(ProfileProvider.class,
					provider); // ProfileCHEProvider -> ProfileProvider,
								// callback -> provider
								// httpService.registerServlet(PROFILE_PROVIDER_ALIAS,
								// servlet, null,
			// httpContext); // smenq se konstantata
			httpService.registerServlet(PROFILE_PROVIDER_ALIAS, servlet, null,
					null);
			debug("Registered ProfileProvider web service at alias '"
					+ PROFILE_PROVIDER_ALIAS + "'.");
		} catch (Throwable ex) {
			log("Failed to register ProfileProvider web service at alias '"
					+ PROFILE_PROVIDER_ALIAS + "'!", ex);
		}
	}

	private void registerDeployManagerService(HttpService httpService,
			DeployManagerService deployManager) {
		try {
			HttpServlet servlet = new SoapExportServlet(DeployManagerService.class,
					deployManager);
			httpService.registerServlet(DEPLOY_MANAGER_ALIAS, servlet, null,
					null);
			debug("Registered DeployManagerService web service at alias '"
					+ DEPLOY_MANAGER_ALIAS + "'.");
		} catch (Throwable ex) {
			log("Failed to register DeployManagerService web service at alias '"
					+ DEPLOY_MANAGER_ALIAS + "'!", ex);
		}
	}

	private void unregisterProfileProvider(HttpService httpService) {
		httpService.unregister(PROFILE_PROVIDER_ALIAS);
		debug("Unregister ProfileProvider servlet!");
	}

	private void unregisterDeployManager(HttpService httpService) {
		httpService.unregister(DEPLOY_MANAGER_ALIAS);
		debug("Unregister DeployManager servlet!");
	}

	public static void log(String msg, Throwable ex) {
		Logger.getLogger(Activator.class.getName()).log(Level.SEVERE,
				"[ProfileAgent][ERROR]" + msg, ex);
		System.out.println("[ProfileAgent][ERROR]" + msg);
		ex.printStackTrace();
	}

	public static void debug(String msg) {
		Logger.getLogger(Activator.class.getName()).log(Level.FINE,
				"[ProfileAgent][DEBUG]" + msg);
		System.out.println("[ProfileAgent][DEBUG]" + msg);
	}

	public static void warning(String msg) {
		Logger.getLogger(Activator.class.getName()).log(Level.WARNING,
				"[ProfileAgent][WARNING]" + msg);
		System.out.println("[ProfileAgent][WARNING]" + msg);
	}

	// public UITransformer getUITransformer() {
	// return (UITransformer)transformerTracker.getService();
	// }

	public static BundleContext getBundleContext() {
		return context;
	}

}
