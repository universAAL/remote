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

public class ExportManagerImpl implements ExportManager, ExportExecutor{
    
    private ExportProcessExecutor exportExecutor;
    private Thread exportThread;
    
    private ExportedProxyManager manager;
    
    private Set<ExportPremise> exportPremises;
    
    private BlockingQueue<InternalExportOperation> busMembersToExport;
    
    private GatewayCommunicator communicator;
    
    public ExportManagerImpl(GatewayCommunicator communicator){
	this.communicator = communicator;
	
	busMembersToExport = new ArrayBlockingQueue<InternalExportOperation>(5);
	
	exportExecutor = new ExportProcessExecutor(busMembersToExport);
	exportThread = new Thread(exportExecutor);
	exportThread.start();
	
	manager = new ExportedProxyManager(communicator);
    }
   
    public void shutdown(){
	exportThread.interrupt();
    }
    /*
     * Methods for tracing registered BusMembers
     * */
    public void memberAdded(BusMember member) {
	
    }
    
    public void memberRemoved(BusMember member){
	
    }

    public void addExportPremise(ExportPremise premise){
	exportPremises.add(premise);
    }
    
    public void removeExportPremise(ExportPremise premise){
	exportPremises.remove(premise);
    }

    public void exportBusMemberForRemote(BusMember sourceMember) {
	// TODO implement in next release
    }

    public void removeExportedBusMember(BusMember sourceMember) {
	// TODO implement in next release
	
    }

    public ServiceResponse sendServiceRequest(String sourceId, ServiceCall call, String memberId) {
	ServiceResponse response = manager.sendServiceRequest(sourceId, call, memberId);
	if (response == null){
	    throw new RuntimeException("response == null");
	}
	return response;
    }

    public void sendUIRequest(String sourceId, UIRequest request) {
	manager.sendUIRequest(sourceId, request);
    }

    public ProxyRegistration registerProxies(ImportRequest request) throws IOException, ClassNotFoundException {
	return manager.registerProxies(request);
    }

    public void unregisterProxies(ImportRequest request) {
	manager.unregisterProxies(request);
    }
}
