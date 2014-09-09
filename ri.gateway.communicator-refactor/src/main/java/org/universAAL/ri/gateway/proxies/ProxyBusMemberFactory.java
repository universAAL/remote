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
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.proxies.exporting.ProxyContextPublisher;
import org.universAAL.ri.gateway.proxies.exporting.ProxySCaller;
import org.universAAL.ri.gateway.proxies.importing.ProxyContextSubscriber;
import org.universAAL.ri.gateway.proxies.importing.ProxySCallee;

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
     * @return may be null if regParams is not matched to any proxy.
     */
    public static ProxyBusMember createImport(final Resource[] regParams) {
	if (regParams.length > 0) {
	    if (regParams[0] instanceof ContextEventPattern) {
		return new ProxyContextSubscriber(
			Gateway.getInstance().context,
			(ContextEventPattern[]) regParams);
	    }
	    if (regParams[0] instanceof ServiceProfile) {
		return new ProxySCallee(Gateway.getInstance().context,
			(ServiceProfile[]) regParams);
	    }
	}
	return null;
    }

    /**
     * Create a proxy for exporting with the given paramenters.
     * 
     * @param regParams
     * @return may be null if regParams is not matched to any proxy.
     */
    public static ProxyBusMember createExport(final Resource[] regParams,
	    final String busMemberId) {
	if (regParams.length > 0) {
	    if (regParams[0] instanceof ContextEventPattern) {
		return new ProxyContextPublisher(Gateway.getInstance().context);
	    }
	    if (regParams[0] instanceof ServiceProfile) {
		return new ProxySCaller(Gateway.getInstance().context,
			(ServiceProfile[]) regParams, busMemberId);
	    }
	}
	return null;
    }

    public static boolean isForExport(final BusMember member) {
	return member != null
		&& (member instanceof ServiceCallee || member instanceof ContextSubscriber);
    }
}
