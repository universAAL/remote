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

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCallee;

/**
 * Factory to create Proxies.
 * 
 * @author amedrano
 * 
 */
public class ProxyBusMemberFactory {

    /**
     * Create a proxy given parameters sent by remote peer.
     * 
     * @param regParams
     * @return
     */
    public static ProxyBusMember createImport(final Resource[] regParams) {
	// TODO complete creation from params
	return null;
    }

    /**
     * Create a proxy for exporting with the given paramenters.
     * 
     * @param params
     * @return
     */
    public static ProxyBusMember createExport(final Resource[] params) {
	// TODO complete creation from params
	return null;
    }

    public static boolean isForExport(final BusMember member) {
        return member != null
        	&& (member instanceof ServiceCallee || member instanceof ContextSubscriber);
    }
}
