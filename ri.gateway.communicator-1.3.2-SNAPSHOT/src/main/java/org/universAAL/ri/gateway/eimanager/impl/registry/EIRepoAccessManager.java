package org.universAAL.ri.gateway.eimanager.impl.registry;

import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ExportEntry;


public enum EIRepoAccessManager {
    Instance;
    
    static{
	registry = new NonPersistentExportImportRegistry();
    }
    
    private static NonPersistentExportImportRegistry registry;
    
    public void publishExportToRepo(ExportEntry ee){
	registry.addExportInfo(ee);
    }
    
    public void removeExportFromRepo(ExportEntry ee){
	registry.removeExportInfo(ee);
    }
    
    public void publishImportToRepo(ImportEntry ie) {
	registry.addImportInfo(ie);
    }
    
    public void removeImportFromRepo(ImportEntry ie) {
	registry.removeImportInfo(ie);
    }
    
    public void addListener(IRegistryListener listener) {
	registry.addListener(listener);
    }

    public void removeListener(IRegistryListener listener) {
	registry.removeListener(listener);
    }
}
