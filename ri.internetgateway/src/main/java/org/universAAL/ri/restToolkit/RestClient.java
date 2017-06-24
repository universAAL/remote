/*
	Copyright 2007-2014 CERTH-ITI, http://www.iti.gr
	Centre of Research and Technology Hellas
	Information Technologies Institute

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
package org.universAAL.ri.restToolkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RestClient {

	static public String getRequest(String endpoint, HashMap<String, String> parameters) {
		String response = "";
		// Send a GET request to the servlet
		try {
			// Construct data
			StringBuffer data = new StringBuffer();
			// Send data
			String urlStr = endpoint;
			String parms = "";
			if (parameters != null && !parameters.isEmpty()) {
				parms = "?";
				Iterator it = parameters.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next().toString();
					String value = parameters.get(key).toLowerCase();
					parms = parms + key + "=" + value + "&";
				}
				if (parms.endsWith("&")) {
					parms = parms.substring(0, parms.lastIndexOf("&") - 1);
				}
			}
			if (!parms.equals("") && !parms.equals("?")) {
				urlStr = urlStr + parms;
			}
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setReadTimeout(10000);
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				response = response + line + "\n";
			}
			rd.close();
			return response;
		} catch (Exception e) {

			return response;
		}

	}

	static public String postRequest(String endpoint, HashMap<String, String> parameters) {
		String response = "";
		try {
			ClientHttpRequest method = new ClientHttpRequest(endpoint);
			Iterator it = parameters.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				String value = parameters.get(key).toLowerCase();
				method.setParameter(key, value);
				InputStream in = method.post();
				int oneChar;
				while ((oneChar = in.read()) != -1) {
					response = response + oneChar;

				}
			}
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			return response;
		}

	}

	// public static void main(String[] args) {
	//
	// System.out.println(getRequest("http://www.thomas-bayer.com/sqlrest/",
	// null));
	// }
}
