package org.universAAL.ri.gateway.eimanager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.eimanager.ExportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.ImportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

public enum EIOperationManager implements SharedObjectListener{
	Instance;
	
	private ImportInterceptorsComparator importComparator;
	private ExportInterceptorsComparator exportComparator;
	private List<ImportOperationInterceptor> importList;
	private List<ExportOperationInterceptor> exportList;
	
	{
		importComparator = new ImportInterceptorsComparator();
		exportComparator = new ExportInterceptorsComparator();
		
		importList = new ArrayList<ImportOperationInterceptor>();
		exportList = new ArrayList<ExportOperationInterceptor>();
	}
	
	public void init(){
			Activator.mc.getContainer()
					.fetchSharedObject(Activator.mc ,
						new Object[] { ImportOperationInterceptor.class.getName() }, this);

			Activator.mc.getContainer()
					.fetchSharedObject(Activator.mc ,
						new Object[] { ExportOperationInterceptor.class.getName() }, this);
	}
	
	public void executeExportOperationChain(ImportRequest request, Type type) throws InterruptExecutionException{
		List<ExportOperationInterceptor> interceptors = exportList;
		Collections.sort(interceptors, exportComparator);
		for(ExportOperationInterceptor i : interceptors){
			i.process(request);
		}
	}
	
	public void executeImportOperationChain(ImportRequest request, Type type) throws InterruptExecutionException{
		List<ImportOperationInterceptor> interceptors = importList;
		Collections.sort(interceptors, importComparator);
		for(ImportOperationInterceptor i : interceptors){
			i.process(request);
		}
	}
	
	public enum Type {
		Service,Context,UI;
	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if (sharedObj instanceof ImportOperationInterceptor){
			importList.add((ImportOperationInterceptor) sharedObj);
		}else if (sharedObj instanceof ExportOperationInterceptor){
			exportList.add((ExportOperationInterceptor) sharedObj);
		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		ServiceReference ref = (ServiceReference)removeHook;
		Object removedService = Activator.bc.getService(ref);
		if (removedService instanceof ImportOperationInterceptor){
			importList.add((ImportOperationInterceptor) removedService);
		}else if (removedService instanceof ExportOperationInterceptor){
			exportList.add((ExportOperationInterceptor) removedService);
		}
	}
}
