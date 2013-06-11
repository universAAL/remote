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
package org.universAAL.ri.gateway.eimanager.impl.registry;

import java.util.HashMap;
import java.util.Map;

import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ExportEntry;


public class NonPersistentExportImportRegistry extends AbstractRegistry{
    private Map<String, ExportEntry> exportMap;
    private Map<String, ImportEntry> importMap;
    
    public NonPersistentExportImportRegistry(){
	this.exportMap = new HashMap<String, ExportEntry>();
	this.importMap = new HashMap<String, ImportEntry>();
    }
    
    public void addExportInfo(ExportEntry ee){
	exportMap.put(ee.getId(), ee);
	for(IRegistryListener listener : listeners){
	    listener.registryEntryAdded(ee);
	}
    }
    
    public void removeExportInfo(ExportEntry ee){
	exportMap.remove(ee.getId());
	for(IRegistryListener listener : listeners){
	    listener.registryEntryRemoved(ee);
	}
    }
    
    public void addImportInfo(ImportEntry ie){
	importMap.put(ie.getId(), ie);
	for(IRegistryListener listener : listeners){
	    listener.registryEntryAdded(ie);
	}
    }
    
    public void removeImportInfo(ImportEntry ie){
	importMap.remove(ie.getId());
	for(IRegistryListener listener : listeners){
	    listener.registryEntryRemoved(ie);
	}
    }
}
