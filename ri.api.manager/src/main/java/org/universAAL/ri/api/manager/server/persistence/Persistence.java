/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.ri.api.manager.server.persistence;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.ri.api.manager.RemoteAPI;

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

    /**
     * Sets up the underlying database, creating any tables if not present
     * before, and performing any mainenance operations.
     * 
     * @param remoteAPI
     *            The Remote API instance being used in the manager
     * @param context
     *            Module Context of universAAL
     */
    public void init(RemoteAPI remoteAPI, ModuleContext context);

    /**
     * Persist a request to have a registration of a remote node
     * 
     * @param id
     *            The id used to uniquely identify the remote node
     * @param remote
     *            The remote access information for a remote node
     * @param v
     *            The version of client invoking the API. Descriptive only. Can be null.
     */
    public void storeRegister(String id, String remote, String v);

    /**
     * Eliminate all persisted info (node registration, subscribers and callees)
     * of a remote node
     * 
     * @param id
     *            The id used to uniquely identify the remote node
     */
    public void removeRegister(String id);

    /**
     * Persist a request to have a context subscriber for a node
     * 
     * @param id
     *            The id used to uniquely identify the remote node
     * @param pattern
     *            The serialized ContextEventPattern sent by the remote node in
     *            the request
     */
    public void storeSubscriber(String id, String pattern);

    /**
     * Persist a request to have a service callee for a node
     * 
     * @param id
     *            The id used to uniquely identify the remote node
     * @param profile
     *            The serialized ServiceProfile sent by the remote node in the
     *            request
     */
    public void storeCallee(String id, String profile);

    /**
     * Restore in the R-API implementation (and consequently in the MW) all the
     * persisted information (registration, subscribers and callees) for all
     * nodes.
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
