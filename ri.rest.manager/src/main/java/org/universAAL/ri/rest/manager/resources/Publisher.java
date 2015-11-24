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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.PublisherWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@Path("uaal/spaces/{id}/context/publishers/{subid}")
public class Publisher {

    @XmlAttribute
    @PathParam("subid")
    private String id;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link self;
    
    @XmlElement(name = "providerinfo")
    private String providerinfo;

    public Link getSelf() {
	return self;
    }

    public void setSelf(Link self) {
	this.self = self;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }
    
    public String getProviderinfo() {
        return providerinfo;
    }

    public void setProviderinfo(String providerinfo) {
        this.providerinfo = providerinfo;
    }

    public Publisher(String id, String subid, String providerinfo) {
	this.id = subid;
	this.providerinfo=providerinfo;
	setSelf(Link.fromPath("/uaal/spaces/"+id+"/context/publishers/"+subid).rel("self").build());
    }

    public Publisher() {
	
    }
    
    //===============REST METHODS===============
    
    @GET
    @Produces(Activator.TYPES)
    public Publisher getPublisherResource(@PathParam("id") String id, @PathParam("subid") String subid){
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    PublisherWrapper wrapper = tenant.getContextPublisher(subid);
	    if(wrapper!=null){
		return wrapper.getResource();
	    }
	}
	return null;
    }
    
    @DELETE
    public Response deletePublisherResource(@PathParam("id") String id, @PathParam("subid") String subid){
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    tenant.removeContextPublisher(subid);
	    return Response.ok().build();//.nocontent?
	}
	return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @Consumes(Activator.TYPES_TXT)
    public Response executePublisherPublish(@PathParam("id") String id, @PathParam("subid") String subid, String event){
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    PublisherWrapper pubwrap = tenant.getContextPublisher(subid);
	    if(pubwrap!=null){
		ContextEvent ev = (ContextEvent) Activator.getParser().deserialize(event);
		if(ev!=null){
		    pubwrap.publish(ev);
		    return Response.ok().build();
		}else{
		    return Response.status(Status.BAD_REQUEST).build();
		}
	    }else{
		return Response.status(Status.NOT_FOUND).build();
	    }
	}else{
	    return Response.status(Status.NOT_FOUND).build();
	}
    }
    
    @PUT
    @Consumes(Activator.TYPES)
    public Response putPublisherResource(@PathParam("id") String id, @PathParam("subid") String subid, Publisher pub) throws URISyntaxException{
	//The pub generated from the PUT body does not contain any "link" elements, but I wouldnt have allowed it anyway
	if (subid.equals(pub.id)) {// Do not allow changes to id
	    SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	    if (tenant != null) {
		if(Activator.getParser()!=null){
		    if (pub.getProviderinfo() != null) {
			ContextProvider cp = (ContextProvider) Activator
				.getParser().deserialize(pub.getProviderinfo());
			if (cp != null) { //Just check that it is OK
			    PublisherWrapper original = tenant.getContextPublisher(subid);
			    if (original != null) {//Can only change existing ones
				pub.setSelf(Link.fromPath("/uaal/spaces/"+id+"/service/callees/"+pub.getId()).rel("self").build());
				original.setResource(pub);
				if(tenant.updateContextPublisher(original)){
				    return Response.created(new URI("uaal/spaces/"+id+"/service/callees/"+pub.getId())).build();
				}else{
				    return Response.notModified().build();
				}
			    } else {
				return Response.status(Status.NOT_FOUND).build();
			    }
			} else {
			    return Response.status(Status.BAD_REQUEST).build();
			}
		    } else {
			return Response.status(Status.BAD_REQUEST).build();
		    }
		}else{
		    return Response.serverError().build();
		}
	    } else {
		return Response.status(Status.NOT_FOUND).build();
	    }
	} else {
	    return Response.notModified().build();
	}
    }
    
}
