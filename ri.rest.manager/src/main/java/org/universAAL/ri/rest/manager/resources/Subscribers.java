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
import java.util.ArrayList;
import java.util.Enumeration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.SubscriberWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "subscribers")
@Path("/uaal/spaces/{id}/context/subscribers")
public class Subscribers {
    
    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link self;

    @XmlElement(name = "subscriber") // @XmlElementRef?
    private ArrayList<Subscriber> subscribers;

    public ArrayList<Subscriber> getSubscribers() {
	return subscribers;
    }

    public void setSubscribers(ArrayList<Subscriber> subscribers) {
	this.subscribers = subscribers;
    }
    
    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }
    
    public Subscribers(){
	
    }
    
    //===============REST METHODS===============
    
    @GET
    @Produces(Activator.TYPES)
    public Subscribers getSubscribersResource(@PathParam("id") String id){
	Subscribers allsubs=new Subscribers();
        ArrayList<Subscriber> subs = new ArrayList<Subscriber>();
        
        SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
        if(tenant!=null){
            Enumeration<SubscriberWrapper> subenum = tenant.getContextSubscribers();
            while(subenum.hasMoreElements()){
        	subs.add(subenum.nextElement().getResource());
            }
        }
	
        allsubs.setSubscribers(subs);
        allsubs.setSelf(Link.fromPath("/uaal/spaces/"+id+"/context/subscribers/").rel("self").build());
	return allsubs;
    }
    
    @POST
    @Consumes(Activator.TYPES)
    public Response addSubscriberResource(@PathParam("id") String id, Subscriber sub) throws URISyntaxException{
	//The sub generated from the POST body does not contain any "link" elements, but I wouldnt have allowed it anyway
	//Set the links manually, like in the sub constructor
	sub.setSelf(Link.fromPath("/uaal/spaces/"+id+"/context/subscribers/"+sub.getId()).rel("self").build());
	SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
	if(tenant!=null){
	    if(Activator.getParser()!=null){
		if(sub.getPattern()!=null){
		    ContextEventPattern cep=(ContextEventPattern) Activator.getParser().deserialize(sub.getPattern());
		    if(cep!=null){
			tenant.addContextSubscriber(new SubscriberWrapper(
				Activator.getUaalContext(),
				new ContextEventPattern[] { cep }, sub, id));
			Activator.getPersistence().storeSubscriber(id, sub);
			return Response.created(new URI("uaal/spaces/"+id+"/context/subscribers/"+sub.getId())).build();
		    }else{
			return Response.status(Status.BAD_REQUEST).build();
		    }
		}else{
		    return Response.status(Status.BAD_REQUEST).build();
		}
	    }else{
		return Response.serverError().build();
	    }    
	}else{
	    return Response.status(Status.NOT_FOUND).build();
	}
	
    }
    
    @Path("/{subid}")
    @Produces(Activator.TYPES)
    public Subscriber getSubscriberResourceLocator(){
	return new Subscriber();
    }

}
