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

import org.universAAL.ioc.dependencies.DependencyProxy;
import org.universAAL.ioc.dependencies.impl.PassiveDependencyProxy;
import org.universAAL.ioc.dependencies.impl.WaitingDependencyProxy;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.configuration.Configuration.ConnectionMode;
import org.universAAL.ri.gateway.configuration.ConfigurationFile;
import org.universAAL.ri.gateway.proxies.ProxyPool;

/**
 * Main Class for the AALSpace Gateway. It is incharge of managing
 * {@link Session Sessions}, and boot them from the configuration folder.
 * 
 * @author amedrano
 * 
 */
public class Gateway implements ModuleActivator {

    private static WaitingDependencyProxy<Gateway> singleton;

    public DependencyProxy<AALSpaceManager> spaceManager;

    public DependencyProxy<TenantManager> tenantManager;

    public DependencyProxy<MessageContentSerializer> serializer;

    public static Gateway getInstance() {
	return singleton.getObject();
    }

    public ModuleContext context;

    /**
     * Set for all sessions, and a name per session.
     */
    private Map<Session, String> sessions;

    /**
     * Set for all servers, and a name per session.
     */
    private Map<Server, String> servers;

    /**
     * All proxies are holded here.
     */
    private ProxyPool proxypool;

    private Exporter exporter;

    public Collection<Session> getSessions() {
	return sessions.keySet();
    }

    public void start(final ModuleContext mc) throws Exception {
	context = mc;
	singleton = new WaitingDependencyProxy<Gateway>(new Object[] {});
	singleton.setObject(this);

	proxypool = new ProxyPool();

	exporter = new Exporter(proxypool);

	sessions = new HashMap<Session, String>();

	spaceManager = new PassiveDependencyProxy<AALSpaceManager>(context,
		new Object[] { AALSpaceManager.class.getName() });

	serializer = new PassiveDependencyProxy<MessageContentSerializer>(
		context,
		new Object[] { MessageContentSerializer.class.getName() });

	tenantManager = new PassiveDependencyProxy<TenantManager>(context,
		new Object[] { TenantManager.class.getName() });

	final File dir = context.getConfigHome();
	final File[] props = dir.listFiles(new FileFilter() {

	    public boolean accept(final File pathname) {
		return pathname.getName().endsWith(".properties");
	    }
	});

	for (int i = 0; i < props.length; i++) {
	    try {
		// create a new session for each proerties file
		final Configuration fc = new ConfigurationFile(props[i]);
		if (fc.getConnectionMode().equals(ConnectionMode.CLIENT)) {
		    final Session s = new Session(fc, proxypool);
		    newSession(props[i].getAbsolutePath(), s);
		} else {
		    // TODO class of servers may change
		    final Server s = new Server(fc);
		    newServer(props[i].getAbsolutePath(), s);
		}
	    } catch (final Exception e) {
		LogUtils.logError(
			context,
			getClass(),
			"start",
			new String[] { "unable to start session: "
				+ props[i].getAbsolutePath() }, e);
	    }
	}
	/*
	 * XXX implement a monitoring mechanism that tracks new files, creating
	 * new sessions, and stops sessions when their respective file is
	 * removed
	 */

    }

    public synchronized void newServer(final String name, final Server s) {
	servers.put(s, name);
    }

    public synchronized void endServer(final Session s) {
	// TODO stop server
	servers.remove(s);
    }

    public synchronized void newSession(final String name, final Session s) {
	sessions.put(s, name);
	exporter.newSession(s);
    }

    public String getName(final Session s) {
	return sessions.get(s);
    }

    public synchronized void endSession(final Session s) {
	proxypool.sessionEnding(s);
	sessions.remove(s);
    }

    public void stop(final ModuleContext mc) throws Exception {
	// TODO stop all servers
	// TODO end all sessions
	for (final Session s : sessions.keySet()) {
	    endSession(s);
	}
    }
}