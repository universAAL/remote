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
package org.universAAL.ri.rest.manager.wrappers;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.universAAL.ri.rest.manager.Activator;

public class UaalWrapper {

	private static UaalWrapper instance = new UaalWrapper();

	private ConcurrentHashMap<String, SpaceWrapper> tenants = new ConcurrentHashMap<String, SpaceWrapper>();

	public static UaalWrapper getInstance() {
		return instance;
	}

	public Enumeration<SpaceWrapper> getTenants() {
		return tenants.elements();
	}

	public SpaceWrapper getTenant(String id) {
		return tenants.get(id);
	}

	public void addTenant(SpaceWrapper t) {
		tenants.put(t.getResource().getId(), t);
	}

	public void removeTenant(String id) {
		SpaceWrapper t = tenants.remove(id);
		if (t != null) {
			t.close();
		}
	}

	public boolean updateTenant(SpaceWrapper t) {
		try {
			SpaceWrapper original = tenants.remove(t.getResource().getId());
			if (original != null) {
				tenants.put(t.getResource().getId(), t);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Activator.logE("UaalWrapper.updateTenant", e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public void close() {
		Enumeration<SpaceWrapper> ts = tenants.elements();
		while (ts.hasMoreElements()) {
			ts.nextElement().close();
		}
	}

}
