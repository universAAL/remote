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

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;
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
    
    @XmlElement(name = "callback")
    private String callback;

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
    
    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Space() {

    }

    public Space(String id, String callback) {
	this.id = id;
	this.callback = callback;
	setSelf(Link.fromPath("/uaal/spaces/"+id).rel("self").build());
	setContext(Link.fromPath("/uaal/spaces/"+id+"/context").rel("context").build());
	setService(Link.fromPath("/uaal/spaces/"+id+"/service").rel("service").build());
    }
    
    //===============REST METHODS===============
    
    @GET		// GET localhost:9000/uaal/spaces/123      (Redirected from Spaces class)
    @Produces(Activator.TYPES)
    public Space getSpaceResource(@PathParam("id") String id){
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    return tenant.getResource();
	}else{
	    return null;
	}
    }
    
    @DELETE		// DEL localhost:9000/uaal/spaces/123
    public Response deleteSpaceResource(){
	if(Activator.getTenantMngr()!=null){
	    Activator.getTenantMngr().unregisterTenant(this.id);
	}
	UaalWrapper.getInstance().removeTenant(id);
	return Response.ok().build();//.nocontent?
    }
    
    @PUT	// PUT localhost:9000/uaal/spaces/123      <Body: Space>
    @Consumes(Activator.TYPES)
    public Response putSpaceResource(@PathParam("id") String id, Space space) throws URISyntaxException {
	if (id.equals(space.id)) {//Do not allow changes to id
	    SpaceWrapper original = UaalWrapper.getInstance().getTenant(id);
	    if (original != null) {//Can only change existing ones
		// The space generated from the PUT body does not contain any "link"
		space.setSelf(Link.fromPath("/uaal/spaces/" + space.getId()).rel("self").build());
		space.setContext(Link.fromPath("/uaal/spaces/" + space.getId() + "/context").rel("context").build());
		space.setService(Link.fromPath("/uaal/spaces/" + space.getId() + "/service").rel("service").build());
		// Reuse the original to keep the wrappers it already has
		original.setResource(space);
		if(UaalWrapper.getInstance().updateTenant(original)){
		    return Response.created(new URI("uaal/spaces/" + space.getId())).build();
		}else{
		    return Response.notModified().build();
		}
	    } else {
		return Response.status(Status.NOT_FOUND).build();
	    }
	} else {
	    return Response.notModified().build();
	}
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
