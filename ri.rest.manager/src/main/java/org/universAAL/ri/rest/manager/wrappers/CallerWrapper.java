/*
	Copyright 2015 ITACA-SABIEN, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (SABIEN)
	
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
package org.universAAL.ri.rest.manager.wrappers;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ri.rest.manager.resources.Caller;

public class CallerWrapper extends ServiceCaller{
    
    public Caller resource;

    public CallerWrapper(ModuleContext context, Caller r) {
	super(context);
	resource=r;
    }

    @Override
    public void communicationChannelBroken() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void handleResponse(String reqID, ServiceResponse response) {
	// TODO Auto-generated method stub
	
    }

}
