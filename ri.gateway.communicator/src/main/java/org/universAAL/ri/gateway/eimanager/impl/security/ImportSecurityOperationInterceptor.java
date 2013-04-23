package org.universAAL.ri.gateway.eimanager.impl.security;

import org.universAAL.ri.gateway.communicator.service.impl.SecurityEntry;
import org.universAAL.ri.gateway.communicator.service.impl.SecurityManager;
import org.universAAL.ri.gateway.eimanager.ImportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.BusMemberType;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

public class ImportSecurityOperationInterceptor implements ImportOperationInterceptor{

	public void process(ImportRequest importRequest)
			throws InterruptExecutionException {
		String[] uids = null;
		String errorUid = "";
		if (importRequest.getMember().equals(BusMemberType.ServiceCallee.toString()) || 
				importRequest.getMember().equals(BusMemberType.ServiceCaller.toString())){
			uids = new String[1];
			uids[0] = importRequest.getServerNamespace();
		}else if (importRequest.getMember().equals(BusMemberType.ContextPublisher.toString()) ||
				importRequest.getMember().equals(BusMemberType.ContextSubscriber.toString())){
			uids = importRequest.getSubjectURIs();
		}
		
		boolean shouldPass = true;
		for(SecurityEntry entry : SecurityManager.Instance.getDenyImportSecurityEntries()){
			for(String uid : uids){
				if (uid.matches(entry.getEntryRegex())){
					shouldPass = false;
					errorUid = uid;
					break;
				}
			}
		}
		
		if (!shouldPass){
			throw new InterruptExecutionException("UID: " + errorUid + " was matched by one or more IMPORT DENY security entries.");
		}
		
		int passCount = 0;
		for(SecurityEntry entry : SecurityManager.Instance.getAllowImportSecurityEntries()){
			for(String uid : uids){
				if (uid.matches(entry.getEntryRegex())){
					passCount++;
				}
			}
		}
		if (passCount != uids.length){
			throw new InterruptExecutionException("Not all provided uids (only " + passCount +" from " + uids.length +") was matched to IMPORT ALLOW security entries.");
		}
	}

	public int getPriority() {
		return -1;
	}
	
}
