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
package org.universAAL.ri.gateway;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.configuration.ConfigurationFile;

/**
 * Main Class for the AALSpace Gateway. It is incharge of managing
 * {@link Session Sessions}, and boot them from the configuration folder.
 * 
 * @author amedrano
 * 
 */
public class Gateway {

    private static Gateway singleton;

    public static void init(final ModuleContext mc) {
	singleton = new Gateway(mc);
    }

    public static Gateway getInstance() {
	// TODO synchronize until singleton != null
	return singleton;
    }

    public ModuleContext context;

    private Map<String, Session> sessions;

    private Gateway(final ModuleContext mc) {
	context = mc;
	sessions = new HashMap<String, Session>();

	final File dir = context.getConfigHome();
	final File[] props = dir.listFiles(new FileFilter() {

	    public boolean accept(final File pathname) {
		return pathname.getName().endsWith(".properties");
	    }
	});

	for (int i = 0; i < props.length; i++) {
	    // create a new session for each proerties file
	    final Configuration fc = new ConfigurationFile(props[i]);
	    final Session s = new Session(fc);
	    sessions.put(props[i].getAbsolutePath(), s);
	}
	/*
	 * XXX implement a monitoring mechanism that tracks new files, creating
	 * new sessions, and stops sessions when their respective file is
	 * removed
	 */
    }

    public Collection<Session> getSessions() {
	return sessions.values();
    }
}
