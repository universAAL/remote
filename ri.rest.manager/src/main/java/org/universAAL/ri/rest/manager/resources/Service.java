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
@Path("uaal/spaces/{id}/service")
public class Service {

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self;

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link callers;

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link callees;

	public Link getSelf() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	public Link getCallers() {
		return callers;
	}

	public void setCallers(Link callers) {
		this.callers = callers;
	}

	public Link getCallees() {
		return callees;
	}

	public void setCallees(Link callees) {
		this.callees = callees;
	}

	public Service() {

	}

	public Service(String id) {
		setSelf(Link.fromPath("/uaal/spaces/" + id + "/service").rel("self").build());
		setCallers(Link.fromPath("/uaal/spaces/" + id + "/service/callers").rel("callers").build());
		setCallees(Link.fromPath("/uaal/spaces/" + id + "/service/callees").rel("callees").build());
	}

	// ===============REST METHODS===============

	@GET
	@Produces(Activator.TYPES)
	public Service getServiceResource(@PathParam("id") String id) {
		Activator.logI("Service.getServiceResource", "GET host:port/uaal/spaces/X/service ");
		return new Service(id);
	}

	@Path("/callers")
	@Produces(Activator.TYPES)
	public Callers getCallersResource() {
		Activator.logI("Service.getCallersResource", ">>>GET host:port/uaal/spaces/X/service/callers ");
		return new Callers();
	}

	@Path("/callees")
	@Produces(Activator.TYPES)
	public Callees getCalleesResource() {
		Activator.logI("Service.getCalleesResource", ">>>GET host:port/uaal/spaces/X/service/callees ");
		return new Callees();
	}
}
