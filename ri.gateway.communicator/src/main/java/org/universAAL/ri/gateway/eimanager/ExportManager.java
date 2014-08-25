/*
Copyright 2014 Universidad Politécnica de Madrid, http://www.upm.es/
Life Supporting Technologies

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
package org.universAAL.ri.gateway.eimanager;

import java.io.IOException;

import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

public interface ExportManager extends IBusMemberRegistryListener {
	public ServiceResponse sendServiceRequest(String sourceId,
			ServiceCall call, String memberId);

	public void sendUIRequest(String sourceId, UIRequest request);

	public ProxyRegistration registerProxies(ImportRequest request)
			throws IOException, ClassNotFoundException;

	public void unregisterProxies(ImportRequest request);
}
