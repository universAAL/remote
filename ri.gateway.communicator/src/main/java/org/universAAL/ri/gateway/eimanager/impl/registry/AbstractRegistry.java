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
