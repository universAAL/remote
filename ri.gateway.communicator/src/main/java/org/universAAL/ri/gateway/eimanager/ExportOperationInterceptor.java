package org.universAAL.ri.gateway.eimanager;

import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

public interface ExportOperationInterceptor {
	
	public void process(ImportRequest importRequest) throws InterruptExecutionException;
	
	public int getPriority();
}
