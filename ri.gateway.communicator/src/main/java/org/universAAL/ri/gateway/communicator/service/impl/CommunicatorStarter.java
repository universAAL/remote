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
package org.universAAL.ri.gateway.communicator.service.impl;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//import org.osgi.framework.BundleContext;
//import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
//import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.eimanager.ExportManager;
import org.universAAL.ri.gateway.eimanager.ImportManager;

/**
 * Objects of this class are responsible for starting and stopping running
 * GatewayCommunicatorImpl workers.
 * 
 * @author skallz
 * 
 */
public class CommunicatorStarter {

	/**
	 * Registry for active aliases.
	 */
	private static Set<String> aliases;
	static {
		aliases = Collections.synchronizedSet(new HashSet<String>());
	}

	/**
	 * context of a related bundle.
	 */
//	public static BundleContext context;

	private GatewayCommunicatorImpl communicator;

	/**
	 * unique ID = ALIAS_PREFIX + custom name.
	 */
	private final String id;
	/**
	 * HTTP servlet's alias.
	 */
	private final String alias;

	public static ModuleContext mc;

	public static Properties properties;

	/**
	 * Starts the worker with given ImportExportManager and ID.
	 * 
	 * @param id
	 *            ID
	 * @param fileProperties 
	 * @throws Exception
	 * @throws RuntimeException
	 */
	public CommunicatorStarter(/*BundleContext ctxt,*/ ModuleContext mctxt, final String id, Properties fileProperties)
			throws Exception {

//		loadConfiguration();
	    properties=fileProperties;
		loadSecurityConfiguration();

		final List<GatewayAddress> remoteAddresses = extractRemoteGateways();

//		context = ctxt;
		mc=mctxt;
		this.id = id;
		this.alias = createAlias(id);

		if (!aliases.add(alias)) {
			throw new IllegalArgumentException("Duplicated IDs");
		}

		this.communicator = new GatewayCommunicatorImpl();
		this.communicator.addRemoteGateways(remoteAddresses);
	}

	/**
	 * Starts the worker with default ID: "".
	 * @param fileProperties 
	 * 
	 * @param manager
	 *            a ImportExportManager reference
	 * @throws Exception
	 */
	public CommunicatorStarter(/*final BundleContext context,  */ModuleContext mcontext, Properties fileProperties) throws Exception {
		this(/*context, */mcontext, null, fileProperties);
	}

	/**
	 * Util logging function.
	 * 
	 * @param format
	 *            format
	 * @param args
	 *            args
	 */
	static void logInfo(final String format, final Object... args) {
		String callingMethod = Thread.currentThread().getStackTrace()[2]
				.getMethodName();
		System.out.format("[%s] %s%n", callingMethod,
				String.format(format, args));
		// LogUtils.logInfo(Activator.mc, Activator.class.getClass(),
		// callingMethod.getMethodName(),
		// new Object[] { String.format(format, args) }, null);
	}

	public synchronized GatewayCommunicator getCommunicator() {
		while (communicator == null) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return communicator;
	}

	/**
	 * Sets import and export managers for the communicator
	 * 
	 * @param importManager
	 *            import manager
	 * @param exportManager
	 *            export manager
	 * @throws Exception
	 */
	public synchronized void setManagers(final ImportManager importManager,
			final ExportManager exportManager) throws Exception {
		// wait for the communicator
		getCommunicator();
		this.communicator.setManagers(importManager, exportManager);
		this.communicator.start();
	}

//	private void loadConfiguration() {
//		try {
//			properties = new Properties();
//			File confHome = new File(new BundleConfigHome(Activator.bc
//					.getBundle().getSymbolicName()).getAbsolutePath());
//			String dataDir = confHome.getPath();
//			String separator = System.getProperty("file.separator");
//
//			File dataDirFile = new File(dataDir);
//			if (!dataDirFile.exists()) {
//				dataDirFile.mkdirs();
//			}
//
//			properties
//					.load(new FileInputStream(new File(confHome + separator,
//							Activator.bc.getBundle().getSymbolicName()
//									+ ".properties")));
//
//		} catch (FileNotFoundException e) {
//			System.out.println(e.toString() + "\t" + e.getMessage());
//			throw new RuntimeException("Configuration file not found");
//		} catch (IOException e) {
//			throw new RuntimeException("Error reading configuration file");
//		}
//	}

	private void loadSecurityConfiguration() {
		try {
			
			SecurityManager.Instance.setAllowExportSecurityEntries(convertSecurityPropertiesToObjectNotation(Type.Export, SecurityAction.Allow));
			SecurityManager.Instance.setAllowImportSecurityEntries(convertSecurityPropertiesToObjectNotation(Type.Import, SecurityAction.Allow));
			SecurityManager.Instance.setDenyExportSecurityEntries(convertSecurityPropertiesToObjectNotation(Type.Export, SecurityAction.Deny));
			SecurityManager.Instance.setDenyImportSecurityEntries(convertSecurityPropertiesToObjectNotation(Type.Import, SecurityAction.Deny));
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private Set<SecurityEntry> convertSecurityPropertiesToObjectNotation(
			Type type, SecurityAction action) {

		String propertyname = null;
		switch (type) {
		case Export:
			switch (action) {
			case Allow:
				propertyname = GatewayCommunicator.EXPORT_SECURITY_CONSTRAINT_ALLOW;
				break;
			case Deny:
				propertyname = GatewayCommunicator.EXPORT_SECURITY_CONSTRAINT_DENY;
				break;
			}
			break;
		case Import:
			switch (action) {
			case Allow:
				propertyname = GatewayCommunicator.IMPORT_SECURITY_CONSTRAINT_ALLOW;
				break;
			case Deny:
				propertyname = GatewayCommunicator.IMPORT_SECURITY_CONSTRAINT_DENY;
				break;
			}
			break;
		}

		final String constraints = properties.getProperty(propertyname);
		if (constraints == null) {
			throw new RuntimeException(action.toString() + " " +type.toString()
					+ " security constraints were not "
					+ "specified during middleware startup in '" + propertyname
					+ "' property.");
		}
		final Set<SecurityEntry> ret = new HashSet<SecurityEntry>();
		String[] splitted = constraints.split(",");
		if (splitted.length == 0) {
			throw new RuntimeException("At last one entry of "
					+ action.toString() + " " + type.toString()
					+ " security constraints has to be specified"
					+ "specified during middleware startup in '" + propertyname
					+ "' property.");
		}
		SecurityEntry entry = null;
		for(String s : splitted){
			entry = new SecurityEntry(action, type, s);
			ret.add(entry);
		}
		return ret;
	}

	public static List<GatewayAddress> extractRemoteGateways() {
		try {
			final String remoteGateways = properties
					.getProperty(GatewayCommunicator.REMOTE_GATEWAYS_PROP);
			if (remoteGateways == null) {
				throw new RuntimeException("Remote gateway addresses were not "
						+ "specified during middleware startup in '"
						+ GatewayCommunicator.REMOTE_GATEWAYS_PROP
						+ "' property.");
			}
			final List<GatewayAddress> ret = new ArrayList<GatewayAddress>();
			String[] splitted = remoteGateways.split(",");
			for (String hostAddress : splitted) {
				String[] splittedHost = hostAddress.trim().split(":");
				if (splittedHost.length < 2 || splittedHost.length > 3) {
					throw new RuntimeException(
							"Provided remote gateway address has bad format '"
									+ hostAddress
									+ "'. Should be colon separated!!!");
				}
				GatewayAddress addr = new GatewayAddress(splittedHost[0],
						Integer.valueOf(splittedHost[1]),
						splittedHost.length == 3 ? splittedHost[2] : null);
				ret.add(addr);
			}
			return ret;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	static String createAlias(final String id) {
		if (id != null && !"".equals(id)) {
			return GatewayCommunicator.ALIAS_PREFIX + "-" + id;
		} else {
			return GatewayCommunicator.ALIAS_PREFIX;
		}
	}

	public void stop() {

	}

}
