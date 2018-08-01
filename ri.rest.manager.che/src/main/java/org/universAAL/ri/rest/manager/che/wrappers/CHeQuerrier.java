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
package org.universAAL.ri.rest.manager.che.wrappers;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.che.ContextHistoryOntology;
import org.universAAL.ontology.che.ContextHistoryService;

/**
 * @author amedrano
 * 
 */
public class CHeQuerrier {

	private static final String UTF_8 = "utf-8";
	private static final String OUTPUT_RESULT_STRING = ContextHistoryOntology.NAMESPACE
			+ "outputfromCHE";

	private ModuleContext owner;

	public CHeQuerrier(ModuleContext mc) {
		this.owner = mc;
	}

	public String unserialisedQuery(String query) {
		ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(
				null), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(
				ContextHistoryService.PROP_PROCESSES, query);

		getQuery.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });
		getQuery.addSimpleOutputBinding(
				new ProcessOutput(OUTPUT_RESULT_STRING), new PropertyPath(null,
						true,
						new String[] { ContextHistoryService.PROP_RETURNS })
						.getThePath());
		ServiceCaller sc = new DefaultServiceCaller(owner);
		ServiceResponse sr = sc.call(getQuery);
		sc.close();
		if (!sr.getCallStatus().equals(CallStatus.succeeded)) {
			throw new RuntimeException(getSerializer().serialize(sr));
		}
		List res = sr.getOutput(OUTPUT_RESULT_STRING);
		if (res != null && res.size() > 0 && res.get(0) instanceof String) {
			return (String) res.get(0);
		}
		throw new RuntimeException("No output in response: \n"
				+ getSerializer().serialize(sr));
	}

	public Object query(String query) {
		try {
			Object res = getSerializer().deserialize(unserialisedQuery(query));
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	private MessageContentSerializer getSerializer() {
		return (MessageContentSerializer) owner
				.getContainer()
				.fetchSharedObject(
						owner,
						new Object[] { MessageContentSerializer.class.getName() });
	}

	public static InputStream getResource(String Rfile) {
		return CHeQuerrier.class.getClassLoader().getResourceAsStream(Rfile);
	}

	public static String getQuery(InputStream file, String[] params) {
		String query = "";
		try {
			query = new Scanner(file, UTF_8).useDelimiter("\\Z").next();
			file.close();
		} catch (Exception e) {
			/*
			 * either: - empty file - non existent file - Scanner failture...
			 * Nothing to do here
			 */
		}
		for (int i = 0; i < params.length; i++) {
			query = query.replace("$" + Integer.toString(i + 1), params[i]);
		}
		return query;
	}
}
