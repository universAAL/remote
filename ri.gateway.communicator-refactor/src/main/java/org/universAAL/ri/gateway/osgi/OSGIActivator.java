/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.ri.gateway.Gateway;

/**
 * OSGI Container activator. no logic at all.
 * 
 * @author amedrano
 * 
 */
public class OSGIActivator implements BundleActivator {

    /** {@inheritDoc} */
    public void start(final BundleContext context) throws Exception {
	final ModuleContext mc = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	new Gateway().start(mc);
    }

    /** {@inheritDoc} */
    public void stop(final BundleContext context) throws Exception {
	final ModuleContext mc = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	Gateway.getInstance().stop(mc);
    }

}
