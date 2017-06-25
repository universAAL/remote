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

import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.wrappers.CallerWrapper;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "callers")
@Path("/uaal/spaces/{id}/service/callers")
public class Callers {

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self;

	@XmlElement(name = "caller") // @XmlElementRef?
	private ArrayList<Caller> callers;

	public ArrayList<Caller> getCallers() {
		return callers;
	}

	public void setCallers(ArrayList<Caller> callers) {
		this.callers = callers;
	}

	public Link getSelf() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	public Callers() {

	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES)
	public Callers getCallersResource(@PathParam("id") String id) {
		Activator.logI("Callers.getCallersResource", "GET host:port/uaal/spaces/X/service/callers");
		Callers allcers = new Callers();
		ArrayList<Caller> cers = new ArrayList<Caller>();

		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			Enumeration<CallerWrapper> callerenum = tenant.getServiceCallers();
			while (callerenum.hasMoreElements()) {
				cers.add(callerenum.nextElement().getResource());
			}
		}

		allcers.setCallers(cers);
		allcers.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callers/").rel("self").build());
		return allcers;
	}

	@POST
	@Consumes(Activator.TYPES)
	public Response addCallerResource(@PathParam("id") String id, Caller cer) throws URISyntaxException {
		Activator.logI("Callers.addCallerResource", "POST host:port/uaal/spaces/X/service/callers");
		// The cer generated from the POST body does not contain any "link"
		// elements, but I wouldnt have allowed it anyway
		// Set the links manually, like in the cer constructor
		cer.setSelf(Link.fromPath("/uaal/spaces/" + id + "/service/callers/" + cer.getId()).rel("self").build());
		SpaceWrapper tenant = UaalWrapper.getInstance().getTenant(id);
		if (tenant != null) {
			tenant.addServiceCaller(new CallerWrapper(Activator.getContext(), cer));
			Activator.getPersistence().storeCaller(id, cer);
			return Response.created(new URI("uaal/spaces/" + id + "/service/callers/" + cer.getId())).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Path("/{subid}")
	@Produces(Activator.TYPES)
	public Caller getCallerResourceLocator() {
		Activator.logI("Callers.getCallerResourceLocator", ">>>GET host:port/uaal/spaces/X/service/callers/Y");
		return new Caller();
	}

}
