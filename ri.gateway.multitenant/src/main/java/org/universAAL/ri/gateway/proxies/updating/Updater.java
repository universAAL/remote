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

/**
 * Interface to manage the operations of Add and Remove RegistrationParameters.
 * Which esentially just differ in the methods contained in this interface.
 *
 * @author amedrano
 *
 */
public interface Updater {
	void update(ProxyBusMember member);

	Resource[] newParameters(Resource[] oldParameters);

	ImportMessage createExportMessage(String busMemberID);
}