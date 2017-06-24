/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.dosgi.singlebundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.cxf.dosgi.dsw.Activator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.osgi.service.http.HttpService;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.ModuleContext;

public class AggregatedActivator implements BundleActivator {
	static final String HTTP_PORT_PROPERTY = "org.osgi.service.http.port";
	static final String HTTPS_PORT_PROPERTY = "org.osgi.service.http.port.secure";
	static final String HTTPS_ENABLED_PROPERTY = "org.osgi.service.http.secure.enabled";
	static final String ACTIVATOR_RESOURCE = "activators.list";

	static final String HTTP_SERVICE_ENABLED = "org.osgi.service.http.enabled";

	static String DEFAULT_HTTP_PORT = "8083";

	private List<BundleActivator> activators = new ArrayList<BundleActivator>();

	public void start(BundleContext ctx) throws Exception {
		setHttpServicePort(ctx);
		startEmbeddedActivators(ctx);
		shareHttpServiceObj(ctx);
	}

	public void stop(BundleContext ctx) throws Exception {
		stopEmbeddedActivators(ctx);
	}

	void setHttpServicePort(BundleContext ctx) {

		//enable HttpService support
		System.setProperty(HTTP_SERVICE_ENABLED, "true");

		boolean https = false;
		String port;
		if ("true".equalsIgnoreCase(ctx.getProperty(HTTPS_ENABLED_PROPERTY))) {
			https = true;
			port = ctx.getProperty(HTTPS_PORT_PROPERTY);
		} else {
			port = ctx.getProperty(HTTP_PORT_PROPERTY);
		}

		if (port == null || port.length() == 0) {
			port = tryPortFree(DEFAULT_HTTP_PORT);
			if (port == null) {
				System.out.print("Port " + DEFAULT_HTTP_PORT
						+ " is not available. ");
				port = tryPortFree("0");
			}
			System.out.println("Setting HttpService port to: " + port);

			String prop = https ? HTTPS_PORT_PROPERTY : HTTP_PORT_PROPERTY;
			System.setProperty(prop, port);
		} else {
			if (tryPortFree(port) == null) {
				System.out
						.println("The system is configured to use HttpService port "
								+ port
								+ ". However this port is already in use.");
			} else {
				System.out.println("HttpService using port: " + port);
			}
		}

		// System.setProperty(PAX_WEB_ADDRESSES, "localhost");// listen on only
		// this address, by
		// default - all addresses

	}

	private String tryPortFree(String port) {
		int p = Integer.parseInt(port);

		ServerSocket s = null;
		try {
			s = new ServerSocket(p);
			return "" + s.getLocalPort();
		} catch (IOException e) {
			return null;
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

	void startEmbeddedActivators(BundleContext ctx) throws Exception {
		ClassLoader oldClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					getClass().getClassLoader());
			for (String s : getActivators()) {
				try {
					Class<?> clazz = getClass().getClassLoader().loadClass(s);
					Object o = clazz.newInstance();
					if (o instanceof BundleActivator) {
						BundleActivator ba = (BundleActivator) o;
						activators.add(ba);
						ba.start(ctx);
					}
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}

			SPIActivator sba = new SPIActivator();
			sba.start(ctx);
			activators.add(sba);

			DSWActivator dsw = new DSWActivator();
			dsw.start(ctx);
			activators.add(dsw);
		} finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
	}

	void stopEmbeddedActivators(BundleContext ctx) throws Exception {
		for (BundleActivator ba : activators) {
			ba.stop(ctx);
		}
	}

	static Collection<String> getActivators() throws IOException {
		List<String> bundleActivators = new ArrayList<String>();

		URL url = AggregatedActivator.class.getResource(ACTIVATOR_RESOURCE);
		if (url == null) {
			return Collections.emptyList();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) {
				bundleActivators.add(line);
			}
		}

		return bundleActivators;
	}

	private void shareHttpServiceObj(BundleContext ctx) {
		//gets the HTTPService
		ServiceReference httpServiceRef = ctx
				.getServiceReference(HttpService.class.getName());
		if (httpServiceRef != null) {
			HttpService httpService = (HttpService) ctx
					.getService(httpServiceRef);

			//shares the HTTPService object
			ModuleContext moduleContext = uAALBundleContainer.THE_CONTAINER
					.registerModule(new BundleContext[] { ctx });
			uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
					httpService, new Object[] { HttpService.class.getName() });
		}
	}

}
