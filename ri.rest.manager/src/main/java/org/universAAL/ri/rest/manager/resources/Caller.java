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

import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.CallerWrapper;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@Path("uaal/spaces/{id}/service/callers/{subid}")
public class Caller {

    @XmlAttribute
    @PathParam("subid")
    private String id;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link self;

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

    public Caller(String id, String subid) {
	this.id = subid;
	setSelf(Link.fromPath("/uaal/spaces/"+id+"/service/callers/"+subid).rel("self").build());
    }

    public Caller() {
	
    }
    
    //===============REST METHODS===============
    
    @GET
    @Produces(Activator.TYPES)
    public Caller getCallerResource(@PathParam("id") String id, @PathParam("subid") String subid){
	Activator.logI("Caller.getCallerResource", "GET host:port/uaal/spaces/X/service/callers/Y");
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    CallerWrapper wrapper = tenant.getServiceCaller(subid);
	    if(wrapper!=null){
		return wrapper.getResource();
	    }
	}
	return null;
    }
    
    @DELETE
    public Response deleteCallerResource(@PathParam("id") String id, @PathParam("subid") String subid){
	Activator.logI("Caller.deleteCallerResource", "DELETE host:port/uaal/spaces/X/service/callers/Y");
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    tenant.removeServiceCaller(subid);
	    Activator.getPersistence().removeCaller(id, subid);
	    return Response.ok().build();//.nocontent?
	}
	return Response.status(Status.NOT_FOUND).build();
    }
    
    @POST
    @Consumes(Activator.TYPES_TXT)
    @Produces(Activator.TYPES_TXT)
    public Response executeCallerCall(@PathParam("id") String id, @PathParam("subid") String subid, String call){
	Activator.logI("Caller.executeCallerCall", "POST host:port/uaal/spaces/X/service/callers/Y");
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    CallerWrapper cerwrap = tenant.getServiceCaller(subid);
	    if(cerwrap!=null){
		ServiceRequest sreq=(ServiceRequest) Activator.getParser().deserialize(call);
		if(sreq!=null){
		    ServiceResponse sr = cerwrap.call(sreq);
		    return Response.ok(Activator.getParser().serialize(sr)).build();
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
    public Response putCallerResource(@PathParam("id") String id, @PathParam("subid") String subid, Caller cer) throws URISyntaxException{
	Activator.logI("Caller.putCallerResource", "PUT host:port/uaal/spaces/X/service/callers/Y");
	//The cer generated from the PUT body does not contain any "link" elements, but I wouldnt have allowed it anyway
	if (subid.equals(cer.id)) {// Do not allow changes to id
	    SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	    if (tenant != null) {
		if(Activator.getParser()!=null){
		    CallerWrapper original = tenant.getServiceCaller(subid);
		    if (original != null) {//Can only change existing ones
			cer.setSelf(Link.fromPath("/uaal/spaces/"+id+"/service/callees/"+cer.getId()).rel("self").build());
			original.setResource(cer);
			if(tenant.updateServiceCaller(original)){
			    Activator.getPersistence().storeCaller(id, cer);
			    return Response.created(new URI("uaal/spaces/"+id+"/service/callees/"+cer.getId())).build();
			}else{
			    return Response.notModified().build();
			}
		    } else {
			return Response.status(Status.NOT_FOUND).build();
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
