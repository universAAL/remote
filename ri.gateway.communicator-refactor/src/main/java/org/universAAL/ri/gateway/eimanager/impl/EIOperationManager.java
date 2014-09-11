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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.universAAL.ri.gateway.eimanager.ExportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.ImportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

@Deprecated
public enum EIOperationManager // implements SharedObjectListener
{
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

    // public void init(){
    // Activator.mc.getContainer()
    // .fetchSharedObject(Activator.mc ,
    // new Object[] { ImportOperationInterceptor.class.getName() }, this);
    //
    // Activator.mc.getContainer()
    // .fetchSharedObject(Activator.mc ,
    // new Object[] { ExportOperationInterceptor.class.getName() }, this);
    // }

    public void executeExportOperationChain(final ImportRequest request,
	    final Type type) throws InterruptExecutionException {
	final List<ExportOperationInterceptor> interceptors = exportList;
	Collections.sort(interceptors, exportComparator);
	for (final ExportOperationInterceptor i : interceptors) {
	    i.process(request);
	}
    }

    public void executeImportOperationChain(final ImportRequest request,
	    final Type type) throws InterruptExecutionException {
	final List<ImportOperationInterceptor> interceptors = importList;
	Collections.sort(interceptors, importComparator);
	for (final ImportOperationInterceptor i : interceptors) {
	    i.process(request);
	}
    }

    public enum Type {
	Service, Context, UI;
    }

    public void sharedObjectAdded(final Object sharedObj,
	    final Object removeHook) {
	if (sharedObj instanceof ImportOperationInterceptor) {
	    importList.add((ImportOperationInterceptor) sharedObj);
	} else if (sharedObj instanceof ExportOperationInterceptor) {
	    exportList.add((ExportOperationInterceptor) sharedObj);
	}
    }

    // public void sharedObjectRemoved(Object removeHook) {
    // if (removeHook instanceof ServiceReference){
    // ServiceReference ref = (ServiceReference)removeHook;
    // Object removedService = Activator.bc.getService(ref);
    // if (removedService instanceof ImportOperationInterceptor){
    // importList.add((ImportOperationInterceptor) removedService);
    // }else if (removedService instanceof ExportOperationInterceptor){
    // exportList.add((ExportOperationInterceptor) removedService);
    // }
    // }
    // }
}
