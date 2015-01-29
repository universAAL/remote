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

import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;

import org.universAAL.middleware.container.utils.LogUtils;

public class Checker extends TimerTask {

    @Override
    public void run() {
	long now = System.currentTimeMillis();
	HashMap<String, Long> current = (HashMap<String, Long>) CSubscriber
		.getLastknownof().clone();
	if (!current.isEmpty()) {
	    Iterator<String> keys = current.keySet().iterator();
	    while (keys.hasNext()) {
		String key = keys.next();
		Long tst = current.get(key);
		if ((now - tst) > (Activator.multiplier * 36000/*00*/)) {// If lastknown tst is before "multiplier hours" ago
		    LogUtils.logDebug(Activator.context, Checker.class, "run", "Missing keep-alive signal from tenant "+key);
		    System.out.println("Missing keep-alive signal from tenant "+key);
		    // TODO Mail?
		}
	    }
	}
    }

}
