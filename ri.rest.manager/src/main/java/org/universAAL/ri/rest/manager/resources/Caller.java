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

import org.universAAL.middleware.serialization.MessageContentSerializer;
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
		setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callers/" + subid).rel("self").build());
	}

	public Caller() {

	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES_JSON_XML)
	public Caller getCallerResource(@PathParam("id") String id, @PathParam("subid") String subid) {
		Activator.logI("Caller.getCallerResource", "GET host:port/uaal/spaces/X/service/callers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			CallerWrapper wrapper = tenant.getServiceCaller(subid);
			if (wrapper != null) {
				return wrapper.getResource();
			}
		}
		return null;
	}

	@DELETE
	public Response deleteCallerResource(@PathParam("id") String id, @PathParam("subid") String subid) {
		Activator.logI("Caller.deleteCallerResource", "DELETE host:port/uaal/spaces/X/service/callers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			tenant.removeServiceCaller(subid);
			Activator.getPersistence().removeCaller(id, subid);
			return Response.status(Status.NO_CONTENT).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	@Consumes(Activator.TYPES_TURTLE)
	@Produces(Activator.TYPES_TURTLE)
	public Response executeCallerCallTurtle(@PathParam("id") String id, @PathParam("subid") String subid, String call) {
		Activator.logI("Caller.executeCallerCall", "POST host:port/uaal/spaces/X/service/callers/Y TURTLE");
		return executeCallerCall(id, subid, call, Activator.getTurtleParser());
	}
	
	@POST
	@Consumes(Activator.TYPES_JSONLD)
	@Produces(Activator.TYPES_JSONLD)
	public Response executeCallerCallJsonld(@PathParam("id") String id, @PathParam("subid") String subid, String call) {
		Activator.logI("Caller.executeCallerCall", "POST host:port/uaal/spaces/X/service/callers/Y JSONLD");
		return executeCallerCall(id, subid, call, Activator.getJsonldParser());
	}
	
	public Response executeCallerCall(String id, String subid, String call, MessageContentSerializer parser) {
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			CallerWrapper cerwrap = tenant.getServiceCaller(subid);
			if (cerwrap != null) {
				ServiceRequest sreq = null;
				Object deserialized = parser.deserialize(call);

				if (deserialized != null) {
					if(deserialized instanceof ServiceRequest) {
						sreq = (ServiceRequest)deserialized;
						ServiceResponse sr = cerwrap.call(sreq);
						return Response.ok(parser.serialize(sr)).build();	
					}else {
						Activator.logE("Caller.executeCallerCallJsonld", "POST host:port/uaal/spaces/X/context/publishers/Y Resource type mismatch. Expected ServiceRequest");
						return Response.status(Status.BAD_REQUEST).build();
					}
					
				} else {
					Activator.logE("Caller.executeCallerCall", "POST POST host:port/uaal/spaces/X/service/callers/Y cant parse Caller with selected parser");
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
	public Response putCallerResource(@PathParam("id") String id, @PathParam("subid") String subid, Caller cer)
			throws URISyntaxException {
		Activator.logI("Caller.putCallerResource", "PUT host:port/uaal/spaces/X/service/callers/Y");
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			if (Activator.hasRegisteredSerializers()) {
				if (!subid.equals(cer.id)) {// Do not allow id different than URI
				    return Response.notModified().build();
				}
				// The cer generated from the PUT body does not contain any "link"
				// elements, but I wouldnt have allowed it anyway
				cer.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callees/" + cer.getId()).rel("self").build());
				CallerWrapper original = tenant.getServiceCaller(subid);
				if (original != null) {// Already exists > change
					original.setResource(cer);
					if (tenant.updateServiceCaller(original)) {
						Activator.getPersistence().storeCaller(id, cer);
						return Response.created(new URI("uaal/spaces/" + id + "/service/callees/" + cer.getId()))
								.build();
					} else {
						return Response.notModified().build();
					}
				} else { // New one > create like in POST
				    tenant.addServiceCaller(new CallerWrapper(Activator.getContext(), cer));
				    Activator.getPersistence().storeCaller(id, cer);
				    return Response.created(new URI("uaal/spaces/" + id + "/service/callers/" + cer.getId())).build();
				}
			} else {
				Activator.logE("Caller.putCallerResource", "Not registered serializers");
				return Response.serverError().build();
			}
		} else {
			Activator.logE("Calees.addCalleeResource", "SpaceWrapper null");
			return Response.status(Status.NOT_FOUND).build();
		}
	}

}
