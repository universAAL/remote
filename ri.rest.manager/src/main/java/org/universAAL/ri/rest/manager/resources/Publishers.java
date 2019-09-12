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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.PublisherWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "publishers")
@Path("/uaal/spaces/{id}/context/publishers")
public class Publishers {

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self;

	@XmlElement(name = "publisher") // @XmlElementRef?
	private ArrayList<Publisher> publishers;

	public ArrayList<Publisher> getPublishers() {
		return publishers;
	}

	public void setPublishers(ArrayList<Publisher> publishers) {
		this.publishers = publishers;
	}

	public Link getSelf() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	public Publishers() {

	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES)
	public Publishers getPublishersResource(@PathParam("id") String id) {
		Activator.logI("Publishers.getPublishersResource", "GET host:port/uaal/spaces/X/context/publishers");
		Publishers allpubs = new Publishers();
		ArrayList<Publisher> pubs = new ArrayList<Publisher>();

		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			Enumeration<PublisherWrapper> pubenum = tenant.getContextPublishers();
			while (pubenum.hasMoreElements()) {
				pubs.add(pubenum.nextElement().getResource());
			}
		}

		allpubs.setPublishers(pubs);
		allpubs.setSelf(Link.fromPath("/uaal/spaces/" + id + "/context/publishers/").rel("self").build());
		return allpubs;
	}

	@POST
	@Consumes(Activator.TYPES)
	public Response addPublisherResource(@PathParam("id") String id, Publisher pub) throws URISyntaxException {
		Activator.logI("Publishers.addPublisherResource", "POST host:port/uaal/spaces/X/context/publishers");
		if(pub.getId().isEmpty()) return Response.status(Status.BAD_REQUEST).build();
		// The pub generated from the POST body does not contain any "link"
		// elements, but I wouldnt have allowed it anyway
		// Set the links manually, like in the pub constructor
		pub.setSelf(Link.fromPath("/uaal/spaces/" + id + "/context/publishers/" + pub.getId()).rel("self").build());
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			if (Activator.getParser() != null) {
				if (pub.getProviderinfo() != null) {
					ContextProvider cp = (ContextProvider) Activator.getParser().deserialize(pub.getProviderinfo());
					if(tenant.getContextPublisher(pub.getId())!=null){ //Already exists 409
					    return Response.status(Status.CONFLICT).build();
					}
					if (cp != null) {
						tenant.addContextPublisher(new PublisherWrapper(Activator.getContext(), cp, pub));
						Activator.getPersistence().storePublisher(id, pub);
						return Response.created(new URI("uaal/spaces/" + id + "/context/publishers/" + pub.getId()))
								.build();
					} else {
						return Response.status(Status.BAD_REQUEST).build();
					}
				} else {
					return Response.status(Status.BAD_REQUEST).build();
				}
			} else {
				return Response.serverError().build();
			}
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}

	}

	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPublisherResourceJsonLD(@PathParam("id") String id, Publisher pub) throws URISyntaxException {
		Activator.logI("Publishers.addPublisherResource", "POST host:port/uaal/spaces/X/context/publishers");
		if(pub.getId().isEmpty()) return Response.status(Status.BAD_REQUEST).build();
		// The pub generated from the POST body does not contain any "link"
		// elements, but I wouldnt have allowed it anyway
		// Set the links manually, like in the pub constructor
		pub.setSelf(Link.fromPath("/uaal/spaces/" + id + "/context/publishers/" + pub.getId()).rel("self").build());
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			if (Activator.getParser() != null) {
				if (pub.getProviderinfo() != null) {
					ContextProvider cp = (ContextProvider) Activator.getParserLD().deserialize(pub.getProviderinfo());
					if(tenant.getContextPublisher(pub.getId())!=null){ //Already exists 409
					    return Response.status(Status.CONFLICT).build();
					}
					if (cp != null) {
						tenant.addContextPublisher(new PublisherWrapper(Activator.getContext(), cp, pub));
						Activator.getPersistence().storePublisher(id, pub);
						return Response.created(new URI("uaal/spaces/" + id + "/context/publishers/" + pub.getId()))
								.build();
					} else {
						return Response.status(Status.BAD_REQUEST).build();
					}
				} else {
					return Response.status(Status.BAD_REQUEST).build();
				}
			} else {
				return Response.serverError().build();
			}
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}

	}
	@Path("/{subid}")
	@Produces(Activator.TYPES)
	public Publisher getPublisherResourceLocator() {
		Activator.logI("Publishers.getPublisherResourceLocator", ">>>GET host:port/uaal/spaces/X/context/publishers/Y");
		return new Publisher();
	}

}
