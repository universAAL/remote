/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

See the NOTICE file distributed with this work for additional
information regarding copyright ownership

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.universAAL.ri.gateway.eimanager.impl;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.ri.gateway.eimanager.RepoEIManager;

@Deprecated
public class EIManagerRegistryListener implements IBusMemberRegistryListener {

    private final RepoEIManager manager;

    public EIManagerRegistryListener(final RepoEIManager manager) {
	this.manager = manager;
    }

    public void busMemberAdded(final BusMember member, final BusType type) {
	try {
	    manager.memberAdded(member);
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    public void busMemberRemoved(final BusMember member, final BusType type) {
	try {
	    manager.memberRemoved(member);
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    public void regParamsAdded(final String busMemberID, final Resource[] params) {
	// TODO Auto-generated method stub

    }

    public void regParamsRemoved(final String busMemberID,
	    final Resource[] params) {
	// TODO Auto-generated method stub

    }

}
