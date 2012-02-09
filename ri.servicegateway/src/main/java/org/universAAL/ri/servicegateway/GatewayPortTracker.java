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
package org.universAAL.ri.servicegateway;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

public class GatewayPortTracker extends ServiceTracker implements Runnable {

    private static int SLEEP_TIME = 3000;
    private HttpService httpService;
    private BundleContext bc;
    private ModuleContext moduleContext;
    private Thread thread;
    private Vector<ServiceReference> servletToBeRegistered;
    private ServiceTracker httpTracker;
    private Set<String> urlSet, dataDirSet;

    private class GatewayHttpContext implements HttpContext {

	private ServiceReference reference;

	public GatewayHttpContext(ServiceReference reference) {
	    this.reference = reference;
	}

	public String getMimeType(String name) {
	    if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
		return "image/jpeg";
	    } else if (name.endsWith(".gif")) {
		return "image/gif";
	    } else if (name.endsWith(".html") || name.endsWith(".htm")) {
		return "text/html";
	    } else if (name.endsWith(".png")) {
		return "image/png";
	    } else if (name.endsWith(".css")) {
		return "text/css";
	    } else if (name.endsWith(".xml")) {
		return "text/xml";
	    } else if (name.endsWith(".js")) {
		return "application/javascript";
	    } else {
		return null;
	    }

	}

	public URL getResource(String res) {
	    return reference.getBundle().getResource(res);
	}

	public boolean handleSecurity(HttpServletRequest arg0,
		HttpServletResponse arg1) throws IOException {

	    return true;
	}

    }

    public GatewayPortTracker(BundleContext bc, ModuleContext mcontext,
	    String clazz, ServiceTrackerCustomizer stc) {
	super(bc, clazz, stc);
	moduleContext = mcontext;
	servletToBeRegistered = new Vector<ServiceReference>();
	urlSet = new HashSet<String>();
	dataDirSet = new HashSet<String>();

	this.bc = bc;
	httpTracker = new ServiceTracker(bc, HttpService.class.getName(), null); // no
	httpTracker.open();

	thread = new Thread(this, "GatewayPortTracker");
	thread.start();
    }

    public synchronized void registerService(ServiceReference reference)
	    throws ServletException, NamespaceException {
	GatewayPort gp = (GatewayPort) bc.getService(reference);
	gp.setContext(moduleContext);
	HttpContext servletContext = new GatewayHttpContext(reference);
	String url = gp.url();
	String dataDir = gp.dataDir();

	LogUtils.logInfo(Activator.mcontext, this.getClass(),
		"registerService", new Object[] { "dataDir->  {}", dataDir },
		null);

	// register the servlet to the url, for example /whereis, if it is
	// already not registered
	if (urlSet.add(url))
	    httpService.registerServlet(url, gp, null, servletContext);

	// register also the path for the resources, for example
	// /helpwhenoutside, if not registered
	if (dataDir != null && dataDirSet.add(dataDir))
	    httpService.registerResources(dataDir, dataDir, servletContext);

    }

    public synchronized Object addingService(ServiceReference reference) {
	// if the http service is still not available, store the services
	// to be registered when it will be available
	if (httpService == null) {
	    servletToBeRegistered.add(reference);
	    return null;
	}

	GatewayPort gp = null;
	try {
	    // if there is some previously stored service first register it
	    if (!servletToBeRegistered.isEmpty()) {
		registerPreviousServices();
	    }

	    registerService(reference);

	} catch (NamespaceException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (ServletException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return gp;
    }

    public synchronized void removedService(ServiceReference reference,
	    Object service) {

	System.out.println("Removed: " + reference.toString());
	GatewayPort gp = (GatewayPort) service;
	httpService.unregister(gp.url());

	super.removedService(reference, service);

    }

    public void run() {
	Thread currentThread = Thread.currentThread();
	while (thread == currentThread) {
	    try {
		HttpService httpService = (HttpService) httpTracker
			.getService();
		if (httpService != null) {
		    this.httpService = httpService;
		    registerPreviousServices();
		    thread = null;
		} else {
		    Thread.sleep(SLEEP_TIME);
		}
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (ServletException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (NamespaceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    private synchronized void registerPreviousServices()
	    throws ServletException, NamespaceException {

	Iterator<ServiceReference> i = servletToBeRegistered.iterator();
	while (i.hasNext()) {

	    registerService(i.next());
	}
	servletToBeRegistered.clear();

    }

}
