package org.universAAL.ri.gateway.eimanager.impl.registry;


public interface IRegistryListener {
    public void registryEntryAdded(RegistryEntry entry);
    public void registryEntryRemoved(RegistryEntry entry);
}
