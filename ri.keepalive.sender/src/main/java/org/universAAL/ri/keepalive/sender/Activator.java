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
package org.universAAL.ri.keepalive.sender;

import java.util.Timer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class Activator implements BundleActivator {
	public static BundleContext osgiContext = null;
	public static ModuleContext context = null;
	public static ContextPublisher cpublisher = null;
	private Timer t;
	private Sender sender;

	public void start(BundleContext bcontext) throws Exception {
		Activator.osgiContext = bcontext;
		Activator.context = uAALBundleContainer.THE_CONTAINER.registerModule(new Object[] { bcontext });
		ContextProvider info = new ContextProvider();
		info.setType(ContextProviderType.gauge);
		ContextEventPattern cep = new ContextEventPattern();
		cep.addRestriction(MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT, SystemInfo.MY_URI));
		cep.addRestriction(
				MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_PREDICATE, SystemInfo.PROP_ALIVE));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_OBJECT, new Boolean(true)));
		info.setProvidedEvents(new ContextEventPattern[] { cep });
		t = new Timer();
		sender = new Sender();
		cpublisher = new DefaultContextPublisher(context, info);
		int multiplier = 1;
		try {
			multiplier = Integer.parseInt(System.getProperty("org.universAAL.ri.keepalive.sender.period", "1"));
		} catch (Exception e) {
			LogUtils.logError(context, Activator.class, "start",
					"Invalid period property entered, using default (1x) : " + e);
		}
		t.scheduleAtFixedRate(sender, 10000, multiplier * 3600000);
	}

	public void stop(BundleContext arg0) throws Exception {
		t.cancel();
		t = null;
		sender = null;
		cpublisher.close();
	}

	public static void sendEvent(ContextEvent cev) {
		if (cpublisher != null) {
			cpublisher.publish(cev);
		}
	}

}
