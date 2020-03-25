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
import org.universAAL.middleware.serialization.MessageContentSerializer;
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
		this.providerinfo = providerinfo;
		setSelf(Link.fromPath("/uaal/spaces/" + id + "/context/publishers/" + subid).rel("self").build());
	}

	public Publisher() {

	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES_JSON_XML)
	public Publisher getPublisherResource(@PathParam("id") String id, @PathParam("subid") String subid) {
		Activator.logI("Publisher.getPublisherResource", "GET host:port/uaal/spaces/X/context/publishers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			PublisherWrapper wrapper = tenant.getContextPublisher(subid);
			if (wrapper != null) {
				return wrapper.getResource();
			}
		}
		return null;
	}

	@DELETE
	public Response deletePublisherResource(@PathParam("id") String id, @PathParam("subid") String subid) {
		Activator.logI("Publisher.deletePublisherResource", "DELETE host:port/uaal/spaces/X/context/publishers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			tenant.removeContextPublisher(subid);
			Activator.getPersistence().removePublisher(id, subid);
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	@Consumes(Activator.TYPES_TURTLE)
	public Response executePublisherPublishTurtle(@PathParam("id") String id, @PathParam("subid") String subid,
			String event) {
		Activator.logI("Publisher.executePublisherPublish", "POST host:port/uaal/spaces/X/context/publishers/Y TURTLE");
		return executePublisherPublish(id, subid, event, Activator.getTurtleParser());
	}
	
	@POST
	@Consumes(Activator.TYPES_JSONLD)
	public Response executePublisherPublishJsonld(@PathParam("id") String id, @PathParam("subid") String subid,
			String event) {
		Activator.logI("Publisher.executePublisherPublish", "POST host:port/uaal/spaces/X/context/publishers/Y JSONLD");
		return executePublisherPublish(id, subid, event, Activator.getJsonldParser());
	}
	
	public Response executePublisherPublish(String id, String subid, String event, MessageContentSerializer parser) {
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			PublisherWrapper pubwrap = tenant.getContextPublisher(subid);
			if (pubwrap != null) {
				Object o =parser.deserialize(event);
				ContextEvent ev=null;
				if(o != null) {
					if(o instanceof ContextEvent) {
						ev = (ContextEvent) parser.deserialize(event);
						pubwrap.publish(ev);
						return Response.ok().build();
					}else {
						Activator.logE("Publisher.executePublisherPublish", "POST host:port/uaal/spaces/X/context/publishers/Y Resource type mismatch. Expected ContextEvent");
						return Response.status(Status.BAD_REQUEST).build();
					}
				}else {
					Activator.logE("Publisher.executePublisherPublish", "POST host:port/uaal/spaces/X/context/publishers/Y cant deserialize given ContextEvent with selected parser");
					return Response.status(Status.BAD_REQUEST).build();
				}
				
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
	
	@POST
	@Consumes(Activator.TYPES_JSON_XML)
	public Response executePublisherPublishMany(@PathParam("id") String id, @PathParam("subid") String subid,
			EventBundle bundle) {
		Activator.logI("Publisher.executePublisherPublishMany", "POST host:port/uaal/spaces/X/context/publishers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			PublisherWrapper pubwrap = tenant.getContextPublisher(subid);
			if (pubwrap != null) {
			    if(bundle!=null && bundle.getEvent()!=null){
				String[] events = bundle.getEvent();
				int sent=0;
				for(String event:events){
        				ContextEvent ev = (ContextEvent) Activator.getTurtleParser().deserialize(event);
        				if(ev == null)
        					ev = (ContextEvent) Activator.getJsonldParser().deserialize(event);
        				if (ev != null) {
        					pubwrap.publish(ev);
        					sent++;
        				}
				}
				if(sent==events.length){ // Sent all events OK
				    return Response.ok().build();
				}else if(sent==0){  // Could not send any event
				    return Response.status(Status.BAD_REQUEST).build();
				}else{ // Sent only some events OK
				    return Response.status(Status.ACCEPTED).build();
				}
			    } else {
				return Response.status(Status.BAD_REQUEST).build();
			    }
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@PUT
	@Consumes(Activator.TYPES_JSON_XML)
	public Response putPublisherResource(@PathParam("id") String id, @PathParam("subid") String subid, Publisher pub)
			throws URISyntaxException {
		Activator.logI("Publisher.putPublisherResource", "PUT host:port/uaal/spaces/X/context/publishers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			if (Activator.hasRegisteredSerializers()) {
				if (pub.getProviderinfo() != null) {
					ContextProvider cp = (ContextProvider) Activator.getTurtleParser().deserialize(pub.getProviderinfo());
					if(cp == null)
						cp = (ContextProvider) Activator.getJsonldParser().deserialize(pub.getProviderinfo());
					if (cp != null) { // Just check that it is OK
					    if (!subid.equals(pub.id)) {// Do not allow id different than URI
						return Response.notModified().build();
					    }
						// The pub generated from the PUT body does not contain any "link"
						// elements, but I wouldnt have allowed it anyway
						pub.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callees/" + pub.getId()).rel("self").build());
						PublisherWrapper original = tenant.getContextPublisher(subid);
						if (original != null) {// Already exists > change
							
							original.setResource(pub);
							if (tenant.updateContextPublisher(original)) {
								Activator.getPersistence().storePublisher(id, pub);
								return Response
										.created(new URI("uaal/spaces/" + id + "/service/callees/" + pub.getId()))
										.build();
							} else {
								return Response.notModified().build();
							}
						} else { // New one > create like in POST
						    tenant.addContextPublisher(new PublisherWrapper(Activator.getContext(), cp, pub));
						    Activator.getPersistence().storePublisher(id, pub);
						    return Response.created(new URI("uaal/spaces/" + id + "/context/publishers/" + pub.getId()))
							    .build();
						}
					} else {
						Activator.logE("Publisher.putPublisherResource", "Cant serialize with registered serializers");
						return Response.status(Status.BAD_REQUEST).build();
					}
				} else {
					Activator.logE("Publisher.putPublisherResource", "Null Publisher provider info");
					return Response.status(Status.BAD_REQUEST).build();
				}
			} else {
				Activator.logE("Publisher.putPublisherResource", "Not registered serializers");
				return Response.serverError().build();
			}
		} else {
			Activator.logE("Publisher.putPublisherResource", "SpaceWrapper null");
			return Response.status(Status.NOT_FOUND).build();
		}
	}

}
