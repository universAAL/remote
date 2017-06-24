/*
	Copyright 2007-2014 CERTH-ITI, http://www.iti.gr
	Centre of Research and Technology Hellas
	Information Technologies Institute

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
package org.universAAL.ri.internetgateway;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Main class for Internet Gateway.
 *
 * @author Konstantinos Giannoutakis
 *
 */

public class Activator implements BundleActivator {

	public static BundleContext context;

	/**
	 * universAAL {@link ModuleContext}
	 */

	public void start(BundleContext context) {

		Activator.context = context;
		org.apache.axiom.om.util.StAXUtils.setFactoryPerClassLoader(false);

	}

	public void stop(BundleContext context) throws Exception {

	}

}
