/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
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
package org.universAAL.ri.gateway.proxies;

import java.util.Collection;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.protocol.MessageSender;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;

/**
 * Generic interface for any proxy representing any imported or exported
 * {@link BusMember}, independently of the bus they associate to.
 * 
 * @author amedrano
 * 
 */
public interface ProxyBusMember {

    /**
     * Get the busmemberId. <br>
     * For imported proxies this ID of the internal busMember of the proxy. <br>
     * For exported proxies this ID is of the represented {@link BusMember}.
     * 
     * @return
     */
    String getBusMemberId();

    void addRemoteProxyReference(BusMemberIdentifier remoteReference);

    void removeRemoteProxyReference(BusMemberIdentifier remoteReference);

    void removeRemoteProxyReferences(MessageSender session);

    // remember to avoid concurrent deletion (while iterating)

    Collection<BusMemberIdentifier> getRemoteProxiesReferences();

    Resource[] getSubscriptionParameters();

    void handleMessage(WrappedBusMessage busMessage);

    boolean isCompatible(Resource[] newParameters);

    void close();

    void update(Resource[] newParams);

}
