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
