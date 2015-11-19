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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.ri.rest.manager.Activator;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@Path("uaal/spaces/{id}/context")
public class Context {

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link self;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link publishers;

    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link subscribers;

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public Link getPublishers() {
        return publishers;
    }

    public void setPublishers(Link publishers) {
        this.publishers = publishers;
    }

    public Link getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Link subscribers) {
        this.subscribers = subscribers;
    }
    
    public Context(){
	
    }
    
    public Context(String id){
	setSelf(Link.fromPath("/uaal/spaces/"+id+"/context").rel("self").build());
	setPublishers(Link.fromPath("/uaal/spaces/"+id+"/context/publishers").rel("publishers").build());
	setSubscribers(Link.fromPath("/uaal/spaces/"+id+"/context/subscribers").rel("subscribers").build());
    }
    
    //===============REST METHODS===============
    
    @GET
    @Produces(Activator.TYPES)
    public Context getContextResource(@PathParam("id") String id){
	return new Context(id);
    }
    
    @Path("/publishers")
    @Produces(Activator.TYPES)
    public Publishers getPublishersResource(){
	return new Publishers();
    }
    
    @Path("/subscribers")
    @Produces(Activator.TYPES)
    public Subscribers getSubscribersResource(){
	return new Subscribers();
    }
}
