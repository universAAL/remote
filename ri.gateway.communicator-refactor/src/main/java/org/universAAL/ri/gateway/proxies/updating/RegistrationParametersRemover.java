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
package org.universAAL.ri.gateway.proxies.updating;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.utils.ArraySet;

/**
 * @author amedrano
 * 
 */
public class RegistrationParametersRemover implements Updater {

    private final Resource[] nParams;

    /**
     * @param nParams
     */
    public RegistrationParametersRemover(final Resource[] nParams) {
	super();
	this.nParams = nParams;
    }

    public void update(final ProxyBusMember member) {
	member.removeSubscriptionParameters(nParams);
    }

    public Resource[] newParameters(final Resource[] oldParameters) {
	return new ArraySet.Difference<Resource>().combine(oldParameters,
		nParams);
    }

    public ImportMessage createExportMessage(final String busMemberID) {
	return ImportMessage.importRemoveSubscription(busMemberID, nParams);
    }

}
