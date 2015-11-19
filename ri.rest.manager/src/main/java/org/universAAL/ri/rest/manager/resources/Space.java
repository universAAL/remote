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
package org.universAAL.ri.rest.manager.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@Path("uaal/spaces/{id}")
public class Space {

    @XmlAttribute
    @PathParam("id")
    private String id;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link self;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link context;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link service;

    public Link getSelf() {
	return self;
    }

    public void setSelf(Link self) {
	this.self = self;
    }

    public Link getContext() {
	return context;
    }

    public void setContext(Link context) {
	this.context = context;
    }

    public Link getService() {
	return service;
    }

    public void setService(Link service) {
	this.service = service;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Space() {

    }

    public Space(String id) {
	this.id = id;
	setSelf(Link.fromPath("/uaal/spaces/"+id).rel("self").build());
	setContext(Link.fromPath("/uaal/spaces/"+id+"/context").rel("context").build());
	setService(Link.fromPath("/uaal/spaces/"+id+"/service").rel("service").build());
    }
    
    //===============REST METHODS===============
    
    @GET		// GET localhost:9000/uaal/spaces/123      (Redirected from Spaces class)
    @Produces(Activator.TYPES)
    public Space getSpaceResource(@PathParam("id") String id){
	Space space=new Space(id);
	return space;
    }
    
    @DELETE		// DEL localhost:9000/uaal/spaces/123
    public Response deleteSpaceResource(){
	if(Activator.tenantMngr!=null){
	    Activator.tenantMngr.unregisterTenant(this.id);
	}
	UaalWrapper.getInstance().removeTenant(id);
	return Response.ok().build();//.nocontent?
    }
    
    @Path("/context")	// GET localhost:9000/uaal/spaces/123/context     (Redirects to Context class)
    @Produces(Activator.TYPES)
    public Context getContextResource(){
	return new Context();
    }
    
    @Path("/service")	// GET localhost:9000/uaal/spaces/123/service     (Redirects to Service class)
    @Produces(Activator.TYPES)
    public Service getServiceResource(){
	return new Service();
    }
    
}
