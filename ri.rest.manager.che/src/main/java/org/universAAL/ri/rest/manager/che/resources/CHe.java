/*******************************************************************************
 * Copyright 2018 2011 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.rest.manager.che.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.che.wrappers.CHeQuerrier;

/**
 * @author amedrano
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@Path("/uaal/CHe")
public class CHe {

	/**
	 * 
	 */
	public CHe() {
		// TODO Auto-generated constructor stub
	}

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link self;

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link context;

	@XmlElement(name = "link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	private Link service;

	public Link getSelf() {
		return self;
	}

	public void setSelf(Link self) {
		this.self = self;
	}

	public Link getContext() {
		return context;
	}

	public void setContext(Link context) {
		this.context = context;
	}

	public Link getService() {
		return service;
	}

	public void setService(Link service) {
		this.service = service;
	}

	/*
	 * =============== REST Services =================
	 */

	@GET
	// GET localhost:9000/uaal/CHe
	@Produces(Activator.TYPES_TXT)
	public String querryRaw(@QueryParam("querry") String querry) {
		CHeQuerrier cq = new CHeQuerrier(Activator.getContext());
		try {
			return cq.unserialisedQuery(querry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
