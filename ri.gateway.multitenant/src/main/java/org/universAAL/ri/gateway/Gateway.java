/*******************************************************************************
 * Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
 * Institute of Information Science and Technologies
 * of the Italian National Research Council
 *
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.universAAL.ioc.dependencies.DependencyProxy;
import org.universAAL.ioc.dependencies.impl.PassiveDependencyProxy;
import org.universAAL.ioc.dependencies.impl.WaitingDependencyProxy;
import org.universAAL.log.LoggerFactory;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.ri.gateway.SessionEvent.SessionStatus;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.configuration.Configuration.ConnectionMode;
import org.universAAL.ri.gateway.configuration.ConfigurationFile;
import org.universAAL.ri.gateway.proxies.ProxyPool;

/**
 * Main Class for the AALSpace Gateway. It is in charge of managing
 * {@link Session Sessions}, and boot them from the configuration folder.
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author amedrano
 * 
 */
public class Gateway implements ModuleActivator, SessionEventListener {

    private static WaitingDependencyProxy<Gateway> singleton;

    public DependencyProxy<AALSpaceManager> spaceManager;

    public DependencyProxy<TenantManager> tenantManager;

    public DependencyProxy<MessageContentSerializer> serializer;

    public DependencyProxy<IBusMemberRegistry> busTracker;

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

    public Collection<Server> getServers() {
	return servers.keySet();
    }

    public Collection<Session> getSessions() {
	return sessions.keySet();
    }

    public void start(final ModuleContext mc) throws Exception {
	context = mc;
	LoggerFactory.updateModuleContext(context);
	try {
	    actualStart(mc);
	} catch (final Exception ex) {
	    LoggerFactory.setModuleContextAsStopped(context);
	    throw ex;
	}
    }

    private void actualStart(final ModuleContext mc) throws Exception {
	singleton = new WaitingDependencyProxy<Gateway>(new Object[] {});
	singleton.setObject(this);

	proxypool = new ProxyPool();

	exporter = new Exporter(proxypool);

	sessions = new HashMap<Session, String>();
	servers = new HashMap<Server, String>();

	spaceManager = new PassiveDependencyProxy<AALSpaceManager>(context,
		new Object[] { AALSpaceManager.class.getName() });

	serializer = new PassiveDependencyProxy<MessageContentSerializer>(
		context,
		new Object[] { MessageContentSerializer.class.getName() });

	tenantManager = new PassiveDependencyProxy<TenantManager>(context,
		new Object[] { TenantManager.class.getName() });

	busTracker = new PassiveDependencyProxy<IBusMemberRegistry>(context,
		IBusMemberRegistry.busRegistryShareParams);

	busTracker.getObject().addListener(exporter, true);

	final File dir = context.getConfigHome();
	if (!dir.exists()) {
	    dir.mkdirs();
	    return;
	}
	final File[] props = dir.listFiles(new FileFilter() {

	    public boolean accept(final File pathname) {
		return pathname.getName().endsWith(".properties");
	    }
	});

	if (props != null) {
	    for (int i = 0; i < props.length; i++) {
		final File p = props[i];
		try {
		    final Runnable task = new Runnable() {
			public void run() {
			    // create a new session for each properties file
			    final Configuration fc = new ConfigurationFile(p);
			    if (fc.getConnectionMode().equals(
				    ConnectionMode.CLIENT)) {
				final Session s = new Session(fc, proxypool);
				newSession(p.getAbsolutePath(), s);
			    } else {
				final Server s = new Server(fc);
				newServer(p.getAbsolutePath(), s);
			    }
			}
		    };
		    new Thread(task, "initialisation of "
			    + props[i].getAbsolutePath()).start();
		} catch (final Exception e) {
		    LogUtils.logError(context, getClass(), "start",
			    new String[] { "unable to start instance from : "
				    + props[i].getAbsolutePath() }, e);
		}
	    }
	    /*
	     * XXX implement a monitoring mechanism that tracks new files,
	     * creating new sessions, and stops sessions when their respective
	     * file is removed
	     */
	}

    }

    public synchronized void newServer(final String name, final Server s) {
	servers.put(s, name);
    }

    public synchronized void endServer(final Server s) {
	// stop server
	s.stop();
	servers.remove(s);
    }

    public synchronized void newSession(final String name, final Session s) {
	sessions.put(s, name);
	s.addSessionEventListener(this);
    }

    public String getName(final Session s) {
	return sessions.get(s);
    }

    public String getName(final Server s) {
	return servers.get(s);
    }

    public synchronized void endSession(final Session s) {
	// Remove exports
	exporter.stopedSession(s);
	// Remove imports
	s.removeImports();
	// Remove Reference
	sessions.remove(s);
	// Stop the session (and it's resources)
	s.stop();
    }

    public void stop(final ModuleContext mc) throws Exception {
	try {
	    actualStop(mc);
	    LoggerFactory.setModuleContextAsStopped(context);
	} catch (final Exception ex) {
	    LoggerFactory.setModuleContextAsStopped(context);
	    throw ex;
	}
    }

    private void actualStop(final ModuleContext mc) throws Exception {
	busTracker.getObject().removeListener(exporter);
	final Set<Server> srvs = new HashSet<Server>(servers.keySet());
	// stop all servers
	for (final Server server : srvs) {
	    server.stop();
	}
	final Set<Session> ssns = new HashSet<Session>(sessions.keySet());
	// end all sessions
	for (final Session s : ssns) {
	    endSession(s);
	}
	exporter.stop();
    }

    public Exporter getExporter() {
	return exporter;
    }

    public ProxyPool getPool() {
	return proxypool;
    }

    /** {@ inheritDoc} */
    public void statusChange(final SessionEvent se) {
	if (se.getCurrentStatus() == SessionStatus.CONNECTED) {
	    // session is activated, check if there is anything to export.
	    exporter.activatedSession(se.getSession());
	} else if (se.getOldStatus() == SessionStatus.CONNECTED) {
	    // it has disconnected have to purge proxies without deleting the
	    // session
	    exporter.stopedSession(se.getSession());
	    se.getSession().removeImports();
	}
    }

    /** {@ inheritDoc} */
    public String getName() {
	return "Gateway Singleton";
    }
}
