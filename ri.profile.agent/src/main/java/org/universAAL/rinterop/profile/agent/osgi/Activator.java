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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.universAAL.commerce.ustore.tools.OnlineStoreManager;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializerEx;
import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.UserProfile;
import org.universAAL.rinterop.profile.agent.ProfileCHEProvider;
import org.universAAL.rinterop.profile.agent.impl.ContextHistoryProfileAgent;
import org.universAAL.rinterop.profile.ws.SoapExportServlet;
import org.universAAL.ucc.deploymanagerservice.DeployManagerService;

/**
 * @author
 */
public class Activator implements BundleActivator, ServiceListener {

  public static BundleContext context = null;

  private static final String PROFILE_PROVIDER_ALIAS = "/ustoreagent";

  private static final String DEPLOY_MANAGER_ALIAS = "/deploymanager";

  private static final String STORE_MANAGER_SERVICE_ADDRESS = "http://srv-ustore.haifa.il.ibm.com:9060/universAAL/OnlineStoreManagerService/OnlineStoreManagerService.wsdl";

  private ModuleContext moduleContext = null;

  private HttpService httpService;
  private boolean profileProviderRegistered = false;
  private boolean deployManagerRegistered = false;

  private ProfileCHEProvider profileCHEProvider;

  private DeployManagerService deployManager;

  /**
   * Turtle parser
   */
  public static MessageContentSerializerEx parser = null;

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
   */
  public void start(BundleContext context) throws Exception {
    Activator.context = context;

    moduleContext = uAALBundleContainer.THE_CONTAINER.registerModule(new BundleContext[] {context});

    createOnlineStoreManagerClient();

    // System.out
    // .println("Is  ContextProvider registered:"
    // + OntologyManagement
    // .getInstance()
    // .isRegisteredClass(
    // "http://ontology.universAAL.org/Context.owl#ContextProvider",
    // true));

    profileCHEProvider = new ContextHistoryProfileAgent(moduleContext);

    // String userURI = "http://ontology.universAAL.org/ContextHistoryHTLImpl.owl#dummyUser";
    // UserProfile userProfile = new UserProfile(userURI);
    // ProfileOntology userProfileOnt = new ProfileOntology();
    // OntologyManagement.getInstance().register(userProfileOnt);
    /* test */
    // profileCHEProvider.getAllUserProfiles(userURI);

    Object httpServiceObj = moduleContext.getContainer().fetchSharedObject(moduleContext, new Object[] {HttpService.class.getName()});
    httpService = (HttpService)httpServiceObj;
    System.out.println("[ProfileAgent]Fetch HttpServiceObj: " + httpService);

    deployManager = (DeployManagerService)moduleContext.getContainer().fetchSharedObject(moduleContext, new Object[] {DeployManagerService.class.getName()});

    if (httpService != null) {
      httpService.createDefaultHttpContext();

      if (!profileProviderRegistered) {
        registerProfileCHEProvider(httpService, profileCHEProvider);
        profileProviderRegistered = true;
      }

      if (!deployManagerRegistered) {
        registerDeployManagerService(httpService, deployManager);
        deployManagerRegistered = true;
      }
    }

    String filter = "(objectclass=" + MessageContentSerializerEx.class.getName() + ")";
    context.addServiceListener(this, filter);
    ServiceReference[] references = context.getServiceReferences(null, filter);
    for (int i = 0; references != null && i < references.length; i++) {
      this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, references[i]));
    }

    test();
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
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    if (httpService != null) {
      unregisterProfileProvider(httpService);
      unregisterDeployManager(httpService);
    }

    httpService = null;
    context = null;
    ((ContextHistoryProfileAgent)profileCHEProvider).close();
    profileCHEProvider = null;
    deployManager = null;
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

    OnlineStoreManager client = (OnlineStoreManager)factory.create();
    uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext, client, new Object[] {OnlineStoreManager.class.getName()});
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

  private void registerProfileCHEProvider(HttpService httpService, ProfileCHEProvider provider) {
    try {
      HttpServlet servlet = new SoapExportServlet(ProfileCHEProvider.class, provider);
      httpService.registerServlet(PROFILE_PROVIDER_ALIAS, servlet, null, null);
      debug("Registered ProfileCHEProvider web service at alias '" + PROFILE_PROVIDER_ALIAS + "'.");
    } catch (Throwable ex) {
      log("Failed to register ProfileCHEProvider web service at alias '" + PROFILE_PROVIDER_ALIAS + "'!", ex);
    }
  }

  private void registerDeployManagerService(HttpService httpService, DeployManagerService deployManager) {
    try {
      HttpServlet servlet = new SoapExportServlet(DeployManagerService.class, deployManager);
      httpService.registerServlet(DEPLOY_MANAGER_ALIAS, servlet, null, null);
      debug("Registered DeployManagerService web service at alias '" + DEPLOY_MANAGER_ALIAS + "'.");
    } catch (Throwable ex) {
      log("Failed to register DeployManagerService web service at alias '" + DEPLOY_MANAGER_ALIAS + "'!", ex);
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
    Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, "[ProfileAgent][ERROR]" + msg, ex);
    System.out.println("[ProfileAgent][ERROR]" + msg);
    ex.printStackTrace();
  }

  public static void debug(String msg) {
    Logger.getLogger(Activator.class.getName()).log(Level.FINE, "[ProfileAgent][DEBUG]" + msg);
    System.out.println("[ProfileAgent][DEBUG]" + msg);
  }

  public static void warning(String msg) {
    Logger.getLogger(Activator.class.getName()).log(Level.WARNING, "[ProfileAgent][WARNING]" + msg);
    System.out.println("[ProfileAgent][WARNING]" + msg);
  }

  public static BundleContext getBundleContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework. ServiceEvent)
   */
  public void serviceChanged(ServiceEvent event) {
    // Update the MessageContentSerializer
    switch (event.getType()) {
      case ServiceEvent.REGISTERED:
      case ServiceEvent.MODIFIED:
        parser = (MessageContentSerializerEx)context.getService(event.getServiceReference());
        break;
      case ServiceEvent.UNREGISTERING:
        parser = null;
        break;
      default:
        break;
    }
  }

  public void test() {
    ContextHistoryProfileAgent agent = new ContextHistoryProfileAgent(moduleContext);

    String userID = "user444";
    String userProfileURI = "urn:org.universAAL.aal_space:test_env#user_profile444";
    String aalSpaceProfileURI = "urn:org.universAAL.aal_space:test_env#aal_space_profile444";
    UserProfile userProfile = new UserProfile(userProfileURI);
    AALSpaceProfile aalSpaceProfile = new AALSpaceProfile(aalSpaceProfileURI);

    /*test add/get user profile*/
    System.out.println("[TEST] adding user profile...");
    agent.addUserProfile(userID, userProfile);

    System.out.println("[TEST] getting user profile...");
    UserProfile gottenUserProfile = agent.getUserProfile(userID);
    System.out.println("[TEST] gotten user profile:" + gottenUserProfile);

    /*test add/get space profile*/
    System.out.println("[TEST] adding aalspace profile...");
    agent.addAALSpaceProfile(userID, aalSpaceProfile);

    System.out.println("[TEST] getting aalspace profile...");
    ArrayList<AALSpaceProfile> gottenAALSpaceProfiles = agent.getAALSpaceProfiles(userID);
    for (AALSpaceProfile pr : gottenAALSpaceProfiles) {
      System.out.println("[TEST] gotten aalspace profile:" + pr);
    }
  }
}
