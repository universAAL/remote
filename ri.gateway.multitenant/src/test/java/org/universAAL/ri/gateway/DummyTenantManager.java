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

import java.util.Dictionary;
import java.util.Map;

import org.universAAL.middleware.managers.api.TenantListener;
import org.universAAL.middleware.managers.api.TenantManager;

/**
 * @author amedrano
 *
 */
public class DummyTenantManager implements TenantManager {

    /** {@inheritDoc} */
    public void loadConfigurations(Dictionary configurations) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public boolean init() {
	// TODO Auto-generated method stub
	return false;
    }

    /** {@inheritDoc} */
    public void dispose() {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void registerTenant(String tenantID, String tenantDescription) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void unregisterTenant(String tenantID) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public Map<String, String> getTenants() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public void addTenantListener(TenantListener tenantListener) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void removeTenantListener(TenantListener tenantListener) {
	// TODO Auto-generated method stub

    }

}
