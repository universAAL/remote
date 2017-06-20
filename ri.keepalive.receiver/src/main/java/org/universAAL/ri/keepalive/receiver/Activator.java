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
package org.universAAL.ri.keepalive.receiver;

import java.util.Timer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.OSGiContainer;
import org.universAAL.middleware.container.utils.LogUtils;

public class Activator implements BundleActivator {
	public static BundleContext osgiContext = null;
	public static ModuleContext context = null;
	public static CSubscriber csubscriber = null;
	private Timer t;
	private org.universAAL.ri.keepalive.receiver.Checker checker;
	public static int multiplier;

	public void start(BundleContext bcontext) throws Exception {
		Activator.osgiContext = bcontext;
		Activator.context = OSGiContainer.THE_CONTAINER.registerModule(new Object[] { bcontext });
		csubscriber = new CSubscriber(context);
		t = new Timer();
		checker = new Checker();
		multiplier = 2;
		try {
			multiplier = Integer.parseInt(System.getProperty("org.universAAL.ri.keepalive.receiver.period", "2"));
		} catch (Exception e) {
			LogUtils.logError(context, Activator.class, "start",
					"Invalid period property entered, using default (2x) : " + e);
		}
		t.scheduleAtFixedRate(checker, 10000, multiplier * 3600000);
	}

	public void stop(BundleContext arg0) throws Exception {
		t.cancel();
		t = null;
		checker = null;
		csubscriber.close();
	}

}
