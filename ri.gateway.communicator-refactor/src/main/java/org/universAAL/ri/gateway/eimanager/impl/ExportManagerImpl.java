/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.ri.gateway.eimanager.impl;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.eimanager.ExportExecutor;
import org.universAAL.ri.gateway.eimanager.ExportManager;
import org.universAAL.ri.gateway.eimanager.ExportPremise;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ExportProcessExecutor;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ExportedProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.exporting.InternalExportOperation;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

@Deprecated
public class ExportManagerImpl implements ExportManager, ExportExecutor {

    private final ExportProcessExecutor exportExecutor;
    private final Thread exportThread;

    private final ExportedProxyManager manager;

    private Set<ExportPremise> exportPremises;

    private final BlockingQueue<InternalExportOperation> busMembersToExport;

    private final GatewayCommunicator communicator;

    public ExportManagerImpl(final GatewayCommunicator communicator) {
	this.communicator = communicator;

	busMembersToExport = new ArrayBlockingQueue<InternalExportOperation>(5);

	exportExecutor = new ExportProcessExecutor(busMembersToExport);
	exportThread = new Thread(exportExecutor);
	exportThread.start();

	manager = new ExportedProxyManager(communicator);
    }

    public void shutdown() {
	exportThread.interrupt();
    }

    /*
     * Methods for tracing registered BusMembers
     */
    public void memberAdded(final BusMember member) {

    }

    public void memberRemoved(final BusMember member) {

    }

    public void addExportPremise(final ExportPremise premise) {
	exportPremises.add(premise);
    }

    public void removeExportPremise(final ExportPremise premise) {
	exportPremises.remove(premise);
    }

    public void exportBusMemberForRemote(final BusMember sourceMember) {
	// TODO implement in next release
    }

    public void removeExportedBusMember(final BusMember sourceMember) {
	// TODO implement in next release

    }

    public ServiceResponse sendServiceRequest(final String sourceId,
	    final ServiceCall call, final String memberId) {
	final ServiceResponse response = manager.sendServiceRequest(sourceId,
		call, memberId);
	if (response == null) {
	    throw new RuntimeException("response == null");
	}
	return response;
    }

    public void sendUIRequest(final String sourceId, final UIRequest request) {
	manager.sendUIRequest(sourceId, request);
    }

    public ProxyRegistration registerProxies(final ImportRequest request)
	    throws IOException, ClassNotFoundException {
	return manager.registerProxies(request);
    }

    public void unregisterProxies(final ImportRequest request) {
	manager.unregisterProxies(request);
    }
}
