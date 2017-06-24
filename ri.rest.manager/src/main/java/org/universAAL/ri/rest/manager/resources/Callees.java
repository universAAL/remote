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

import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.CalleeWrapper;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "callees")
@Path("/uaal/spaces/{id}/service/callees")
public class Callees {

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self;

	@XmlElement(name = "callee") // @XmlElementRef?
	private ArrayList<Callee> callees;

	public ArrayList<Callee> getCallees() {
		return callees;
	}

	public void setCallees(ArrayList<Callee> callees) {
		this.callees = callees;
	}

	public Link getSelf() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	public Callees() {

	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES)
	public Callees getCalleesResource(@PathParam("id") String id) {
		Activator.logI("Callees.getCalleesResource", "GET host:port/uaal/spaces/X/service/callees");
		Callees allcees = new Callees();
		ArrayList<Callee> cees = new ArrayList<Callee>();

		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			Enumeration<CalleeWrapper> calleeenum = tenant.getServiceCallees();
			while (calleeenum.hasMoreElements()) {
				cees.add(calleeenum.nextElement().getResource());
			}
		}

		allcees.setCallees(cees);
		allcees.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callees/").rel("self").build());
		return allcees;
	}

	@POST
	@Consumes(Activator.TYPES)
	public Response addCalleeResource(@PathParam("id") String id, Callee cee) throws URISyntaxException {
		Activator.logI("Callees.addCalleeResource", "POST host:port/uaal/spaces/X/service/callees");
		// The cee generated from the POST body does not contain any "link"
		// elements, but I wouldnt have allowed it anyway
		// Set the links manually, like in the cee constructor
		cee.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callees/" + cee.getId()).rel("self").build());
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			if (Activator.getParser() != null) {
				if (cee.getProfile() != null) {
					ServiceProfile sp = (ServiceProfile) Activator.getParser().deserialize(cee.getProfile());
					if (sp != null) {
						tenant.addServiceCallee(
								new CalleeWrapper(Activator.getUaalContext(), new ServiceProfile[] { sp }, cee, id));
						Activator.getPersistence().storeCallee(id, cee);
						return Response.created(new URI("uaal/spaces/" + id + "/service/callees/" + cee.getId()))
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
	public Callee getCalleeResourceLocator() {
		Activator.logI("Callees.getCalleeResourceLocator", ">>>GET host:port/uaal/spaces/X/service/callees/Y");
		return new Callee();
	}

}
