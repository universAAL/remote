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
@XmlRootElement(name = "uaal")
@Path("/uaal")
public class Uaal {

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self = Link.fromPath("/uaal").rel("self").build(); // Link to
																	// self

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link spaces = Link.fromPath("/uaal/spaces").rel("spaces").build(); // Link
																				// to
																				// /spaces

	public Link getSelf() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	public Link getSpaces() {
		return spaces;
	}

	public void setSpaces(Link spaces) {
		this.spaces = spaces;
	}

	public Uaal() {

	}

	// ===============REST METHODS===============

	@GET // GET localhost:9000/uaal
	@Produces("application/xml;charset=UTF-8;version=1")
	public Uaal getUaalResource() {
		Activator.logI("Uaal.getUaalResource", "GET host:port/uaal");
		return new Uaal();
	}

	@Path("/spaces") // GET localhost:9000/uaal/spaces (redirects to Spaces class)
	@Produces("application/xml;charset=UTF-8;version=1")
	public Spaces getSpacesResourceLocator() {
		Activator.logI("Uaal.getSpacesResourceLocator", ">>>GET host:port/uaal/spaces");
		return new Spaces();
	}
}
