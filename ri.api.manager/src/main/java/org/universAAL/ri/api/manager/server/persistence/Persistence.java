package org.universAAL.ri.api.manager.server.persistence;

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
     */
    public void init(RemoteAPI remoteAPI);

    /**
     * Persist a request to have a registration of a remote node
     * 
     * @param id
     *            The id used to uniquely identify the remote node
     * @param remote
     *            The remote access information for a remote node
     */
    public void storeRegister(String id, String remote);

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

}
