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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.SubscriberWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@Path("uaal/spaces/{id}/context/subscribers/{subid}")
public class Subscriber {

	@XmlAttribute
	@PathParam("subid")
	private String id;

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self;

	@XmlElement(name = "callback")
	private String callback;

	@XmlElement(name = "pattern") // TODO @XMLElement(nillable=true) for not
									// showing in .../subscribers ?
	private String pattern;

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

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String patetrn) {
		this.pattern = patetrn;
	}

	public Subscriber(String id, String subid, String callback, String pattern) {
		setId(subid);
		setSelf(Link.fromPath("/uaal/spaces/" + id + "/context/subscribers/" + subid).rel("self").build());
		setCallback(callback);
		setPattern(pattern);
	}

	public Subscriber() {

	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES_JSON_XML)
	public Subscriber getSubscriberResource(@PathParam("id") String id, @PathParam("subid") String subid) {
		Activator.logI("Subscriber.getSubscriberResource", "GET host:port/uaal/spaces/X/context/subscribers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			SubscriberWrapper wrapper = tenant.getContextSubscriber(subid);
			if (wrapper != null) {
				return wrapper.getResource();
			}
		}
		return null;
	}

	@DELETE
	public Response deleteSubscriberResource(@PathParam("id") String id, @PathParam("subid") String subid) {
		Activator.logI("Subscriber.deleteSubscriberResource", "DELETE host:port/uaal/spaces/X/context/subscribers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			tenant.removeContextSubscriber(subid);
			Activator.getPersistence().removeSubscriber(id, subid);
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@PUT
	@Consumes(Activator.TYPES_JSON_XML)
	public Response putCalleeResource(@PathParam("id") String id, @PathParam("subid") String subid, Subscriber sub)
			throws URISyntaxException {
		Activator.logI("Subscriber.putCalleeResource", "PUT host:port/uaal/spaces/X/context/subscribers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			if (Activator.hasRegisteredSerializers()) {
				if (sub.getPattern() != null) {
					ContextEventPattern cep = null;
					Object deserialized = Activator.getTurtleParser().deserialize(sub.getPattern());
					if(deserialized ==null )
						deserialized = Activator.getJsonldParser().deserialize(sub.getPattern());
					if (deserialized != null) { // Just check that they are OK
						if(deserialized instanceof ContextEventPattern) {
							cep = (ContextEventPattern)deserialized;
							if (!subid.equals(sub.id)) {// Do not allow changes to id
							    return Response.notModified().build();
							}
							// The sub generated from the PUT body does not contain any "link"
							// elements, but I wouldnt have allowed it anyway
							sub.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callees/" + sub.getId()).rel("self").build());
							SubscriberWrapper original = tenant.getContextSubscriber(subid);
							if (original != null) {// Already exists > change
								original.setResource(sub);
								if (tenant.updateContextSubscriber(original)) {
									Activator.getPersistence().storeSubscriber(id, sub);
									return Response
											.created(new URI("uaal/spaces/" + id + "/service/callees/" + sub.getId()))
											.build();
								} else {
									return Response.notModified().build();
								}
							} else { // New one > create like in POST
							    tenant.addContextSubscriber(new SubscriberWrapper(Activator.getContext(),
								    new ContextEventPattern[] { cep }, sub, id));
							    Activator.getPersistence().storeSubscriber(id, sub);
							    return Response.created(new URI("uaal/spaces/" + id + "/context/subscribers/" + sub.getId()))
								    .build();
							}
						}else {
							Activator.logE("Subscriber.putCalleeResource", "PUT host:port/uaal/spaces/X/context/subscribers/Y Resource type mismatch. Expected ContextEventPattern");
							return Response.status(Status.BAD_REQUEST).build();
						}
						
					} else {
						Activator.logE("Subscriber.putCalleeResource", "Cant serialize Subscriber pattern with the registered parsers");
						return Response.status(Status.BAD_REQUEST).build();
					}
				} else {
					Activator.logE("Subscriber.putCalleeResource", "Null Subscriber pattern");
					return Response.status(Status.BAD_REQUEST).build();
				}
			} else {
				Activator.logE("Subscriber.putCalleeResource", "Not registered serializers");
				return Response.serverError().build();
			}
		} else {
			Activator.logE("Subscriber.putCalleeResource", "SpaceWrapper null");
			return Response.status(Status.NOT_FOUND).build();
		}
	}

}
