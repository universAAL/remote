package org.universAAL.ri.gateway.eimanager.impl.registry;

import java.util.ArrayList;
import java.util.List;


public class AbstractRegistry {
    protected List<IRegistryListener> listeners; 
    
    public AbstractRegistry(){
	listeners = new ArrayList<IRegistryListener>();
    }
    
    public void addListener(IRegistryListener listener){
	listeners.add(listener);
    }
    
    public void removeListener(IRegistryListener listener){
	listeners.remove(listener);
    }
}
