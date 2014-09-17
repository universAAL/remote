/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

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
package org.universAAL.ri.gateway.osgi;

import java.util.Collection;
import java.util.UUID;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.Server;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.communicator.service.impl.SessionManager;

/**
 * The implementation of the commands for inspecting the status of the Gateway component from an OSGi
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @since 3.2.0
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 *
 */
@Command(scope = "universAAL", name = "gateway", description = "Commands for inspecting the status of the Gateway components")
public class GatewayCommand extends OsgiCommandSupport {

    /*
    private enum Subcommands {
        CONFIG,
    }

    @Option(name = "-c", aliases = "--config", description = "Shows the active configurations", required = false, multiValued = false)
    EnumSet<Subcommands> config = EnumSet.of(Subcommands.CONFIG);
    */

    @Override
    protected Object doExecute() throws Exception {
        final SessionManager sm = SessionManager.getInstance();
        final Gateway gw = Gateway.getInstance();

        Collection<Server> servers = gw.getServers();

        if (!servers.isEmpty()) {
	    System.out.println("List of active Servers:");
	    System.out.printf("%3s - %20s - %20s - %20s - %10s\n", "n#",
		    "name", "interface", "port", "Status");
	    System.out
		    .println("-----------------------------------------------------------------------");
	    int n = 1;
	    for (Server server : servers) {
		System.out.printf("%03d - %20s - %20s - %20s - %10s\n", n++,
			gw.getName(server), server.getInterface(), server.getPort(),
			server.isActive());
	    }
	}
        
	Collection<Session> list = gw.getSessions();
        if (!list.isEmpty()) {
	    System.out.println("List of active sessions:");
	    System.out.printf("%3s - %20s - %20s - %20s - %10s\n", "n#",
		    "name", "session id", "AAL Space", "Status");
	    System.out
		    .println("-----------------------------------------------------------------------");
	    int n = 1;
	    for (Session session : list) {
		final UUID id = UUID.fromString(session.getScope());
		System.out.printf("%03d - %20s - %20s - %20s - %10s\n", n++, id,
			gw.getName(session), sm.getAALSpaceIdFromSession(id),
			sm.isActive(id));
	    }
	}
        if (servers.isEmpty() && list.isEmpty()){
            System.out.println("No Servers, or sessions active.");
        }
	return null;
    }

}
