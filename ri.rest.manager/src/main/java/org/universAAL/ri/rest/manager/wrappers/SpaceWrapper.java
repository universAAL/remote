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
import org.universAAL.ri.rest.manager.resources.Space;

public class SpaceWrapper {

	private Space resource;

	public Space getResource() {
		return resource;
	}

	public void setResource(Space resource) {
		this.resource = resource;
	}

	private ConcurrentHashMap<String, PublisherWrapper> publishers = new ConcurrentHashMap<String, PublisherWrapper>();
	private ConcurrentHashMap<String, SubscriberWrapper> subscribers = new ConcurrentHashMap<String, SubscriberWrapper>();
	private ConcurrentHashMap<String, CalleeWrapper> callees = new ConcurrentHashMap<String, CalleeWrapper>();
	private ConcurrentHashMap<String, CallerWrapper> callers = new ConcurrentHashMap<String, CallerWrapper>();

	public SpaceWrapper(Space r) {
		resource = r;
	}

	public Enumeration<PublisherWrapper> getContextPublishers() {
		return publishers.elements();
	}

	public Enumeration<SubscriberWrapper> getContextSubscribers() {
		return subscribers.elements();
	}

	public Enumeration<CalleeWrapper> getServiceCallees() {
		return callees.elements();
	}

	public Enumeration<CallerWrapper> getServiceCallers() {
		return callers.elements();
	}

	public PublisherWrapper getContextPublisher(String id) {
		return publishers.get(id);
	}

	public SubscriberWrapper getContextSubscriber(String id) {
		return subscribers.get(id);
	}

	public CalleeWrapper getServiceCallee(String id) {
		return callees.get(id);
	}

	public CallerWrapper getServiceCaller(String id) {
		return callers.get(id);
	}

	public void addContextPublisher(PublisherWrapper w) {
		publishers.put(w.getResource().getId(), w);
	}

	public void addContextSubscriber(SubscriberWrapper w) {
		subscribers.put(w.getResource().getId(), w);
	}

	public void addServiceCallee(CalleeWrapper w) {
		callees.put(w.getResource().getId(), w);
	}

	public void addServiceCaller(CallerWrapper w) {
		callers.put(w.getResource().getId(), w);
	}

	public boolean removeContextPublisher(String id) {
		PublisherWrapper w = publishers.remove(id);
		if (w != null) {
			w.close();
			return true;
		} else {
		    return false;
		}
	}

	public boolean removeContextSubscriber(String id) {
		SubscriberWrapper w = subscribers.remove(id);
		if (w != null) {
			w.close();
			return true;
		} else {
		    return false;
		}
	}

	public boolean removeServiceCallee(String id) {
		CalleeWrapper w = callees.remove(id);
		if (w != null) {
			w.close();
			return true;
		} else {
		    return false;
		}
	}

	public boolean removeServiceCaller(String id) {
		CallerWrapper w = callers.remove(id);
		if (w != null) {
			w.close();
			return true;
		} else {
		    return false;
		}
	}

	public boolean updateContextPublisher(PublisherWrapper w) {
		try {
			PublisherWrapper original = publishers.remove(w.getResource().getId());
			if (original != null) {
				publishers.put(w.getResource().getId(), w);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Activator.logE("SpaceWrapper.updateContextPublisher", e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateContextSubscriber(SubscriberWrapper w) {
		try {
			SubscriberWrapper original = subscribers.remove(w.getResource().getId());
			if (original != null) {
				subscribers.put(w.getResource().getId(), w);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Activator.logE("SpaceWrapper.updateContextSubscriber", e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateServiceCallee(CalleeWrapper w) {
		try {
			CalleeWrapper original = callees.remove(w.getResource().getId());
			if (original != null) {
				callees.put(w.getResource().getId(), w);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Activator.logE("SpaceWrapper.updateServiceCallee", e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateServiceCaller(CallerWrapper w) {
		try {
			CallerWrapper original = callers.remove(w.getResource().getId());
			if (original != null) {
				callers.put(w.getResource().getId(), w);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Activator.logE("SpaceWrapper.updateServiceCaller", e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public void close() {
		Enumeration<PublisherWrapper> pubs = publishers.elements();
		while (pubs.hasMoreElements()) {
			pubs.nextElement().close();
		}
		Enumeration<SubscriberWrapper> subs = subscribers.elements();
		while (subs.hasMoreElements()) {
			subs.nextElement().close();
		}
		Enumeration<CalleeWrapper> cees = callees.elements();
		while (cees.hasMoreElements()) {
			cees.nextElement().close();
		}
		Enumeration<CallerWrapper> cers = callers.elements();
		while (cers.hasMoreElements()) {
			cers.nextElement().close();
		}
	}

}
