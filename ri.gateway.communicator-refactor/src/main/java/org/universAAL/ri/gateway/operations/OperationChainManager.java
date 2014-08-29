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
package org.universAAL.ri.gateway.operations;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;

/**
 * A storage of {@link OperationChain}s.
 * 
 * @author amedrano
 * 
 */
public interface OperationChainManager {
    /**
     * The security checks that a {@link ImportMessage ImportRequest} has to
     * pass in order to be allowed to create a proxy for it.
     * 
     * 
     * @TODO interface to be refined!
     * @return
     */
    OperationChain getImportOperationChain();

    /**
     * The security checks that a {@link BusMember} has to pass in order to be
     * allowed to be exported.
     * 
     * 
     * @TODO interface to be refined!
     * @return
     */
    ExportOpertaionChain getExportOperationChain();

    /**
     * The security checks that an incoming {@link WrappedBusMessage} has to
     * pass in order to be allowed to interpreted by the proxy.
     * 
     * 
     * @TODO interface to be refined!
     * @return
     */
    OperationChain getIncomingMessageOperationChain();

    /**
     * The security checks that an outgoing {@link WrappedBusMessage} has to
     * pass in order to be allowed to transmitted to peer.
     * 
     * 
     * @TODO interface to be refined!
     * @return
     */
    OperationChain getOutgoingMessgaeOperationChain();
}
