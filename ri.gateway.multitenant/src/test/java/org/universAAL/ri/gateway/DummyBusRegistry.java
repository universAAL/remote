/*******************************************************************************
 * Copyright 2015 2011 Universidad Politécnica de Madrid
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
package org.universAAL.ri.gateway;

import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;

/**
 * @author amedrano
 *
 */
public class DummyBusRegistry implements IBusMemberRegistry {

    /** {@inheritDoc} */
    public void addListener(IBusMemberRegistryListener listener,
	    boolean notifyAboutPreviouslyRegisteredMembers) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void removeListener(IBusMemberRegistryListener listener) {
	// TODO Auto-generated method stub

    }

}
