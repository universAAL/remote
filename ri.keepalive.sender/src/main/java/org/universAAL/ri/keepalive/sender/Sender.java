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

import java.util.TimerTask;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.util.Constants;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class Sender extends TimerTask {

	@Override
	public void run() {// TODO Set right URI, but now it doesnt matter because
						// what is used in receiver is tenant id.
		SystemInfo sys = new SystemInfo(Constants.MIDDLEWARE_LOCAL_ID_PREFIX + "localsysteminfo");
		sys.setAlive(true);
		ContextEvent event = new ContextEvent(sys, SystemInfo.PROP_ALIVE);
		Activator.sendEvent(event);
	}

}
