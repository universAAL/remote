/*
	Copyright 2013 CERTH, http://www.certh.gr

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
package org.universAAL.ri.soap.cxf.server;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.ri.soap.cxf.server.osgi.Activator;
import org.universAAL.ri.soap.cxf.service.ServiceInterface;

public class ServiceImpl implements ServiceInterface {

	private static ServiceCaller caller;
	public static ServiceResponse sr;
	MessageContentSerializer m;
	ContextEvent event;

	public String getTurtleServiceReq(String turtleStr) {

		caller = new DefaultServiceCaller(Activator.mc);

		sr = caller.call(turtleStr);
		Object[] contentSerializerParams = new Object[] { MessageContentSerializer.class.getName() };
		MessageContentSerializer s = (org.universAAL.middleware.serialization.MessageContentSerializer) Activator.mc
				.getContainer().fetchSharedObject(Activator.mc, contentSerializerParams);
		String serializedStr = s.serialize(sr);

		return serializedStr;
	}

}
