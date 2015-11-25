/*
	Copyright 2015 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.ri.rest.manager.server.persistence;

import org.universAAL.ri.rest.manager.resources.Callee;
import org.universAAL.ri.rest.manager.resources.Caller;
import org.universAAL.ri.rest.manager.resources.Publisher;
import org.universAAL.ri.rest.manager.resources.Space;
import org.universAAL.ri.rest.manager.resources.Subscriber;

/**
 * Database interface for persisting the calls from clients. This allows clients
 * not having to re-send their subscriptions or registers when the server goes
 * down.
 * 
 * The persistence operates only at the level of the requests by the clients,
 * not the actual nodes and wrappers in MW. These will go up and down together
 * with the manager bundle. It is better to see this as a persistence for when
 * the bundle is down, rather than the whole server.
 * 
 * Only information about registration, context subscribers and service callees
 * is persisted for the remote nodes. Context events and service calls are not
 * persisted since they are "consumed" as they come.
 * 
 * @author alfiva
 * 
 */
public interface Persistence {

    public void init();

    public void storeSpace(Space s, String... v);

    public void removeSpace(String id);

    public void storeSubscriber(String id, Subscriber s);
    
    public void removeSubscriber(String id, String subid);

    public void storeCallee(String id, Callee c);
    
    public void removeCallee(String id, String subid);
    
    public void storePublisher(String id, Publisher p);
    
    public void removePublisher(String id, String subid);

    public void storeCaller(String id, Caller c);
    
    public void removeCaller(String id, String subid);

    /**
     * Restore in the MW all the
     * persisted information (registration, subscribers and callees)
     */
    public void restore();
    
    /**
     * Store a new user-password pair for later authentication. There is no
     * "change" method for this, so any change will require manual modification
     * of the persistence.
     * 
     * @param user
     *            User
     * @param pwd
     *            Password
     */
    public void storeUserPWD(String user, String pwd);
    
    /**
     * Method that checks the proper authentication of a persisted user-password
     * pair.
     * 
     * @param user
     *            User
     * @param pwd
     *            Password
     * @return true if pwd matches the initially stored value and no errors
     *         occured
     */
    public boolean checkUserPWD(String user, String pwd);
    
    /**
     * Check if a user is already persisted with an associated password pair.
     * 
     * @param user
     *            to check
     * @return true if there is already a user-password pair persisted.
     */
    public boolean checkUser(String user);

}
