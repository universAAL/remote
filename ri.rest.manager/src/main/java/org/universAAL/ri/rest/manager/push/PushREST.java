/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion
	Avanzadas - Grupo Tecnologias para la Salud y el
	Bienestar (TSB)

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
package org.universAAL.ri.rest.manager.push;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.server.Base64;
import org.universAAL.ri.rest.manager.server.Configuration;

/**
 * Class that manages the push of callbacks to client remote node endpoints
 * using HTTP.
 *
 * @author alfiva
 *
 */
public class PushREST {

	public static void pushContextEvent(String callback, ContextEvent event) throws PushException {
		try {
			String serial = Activator.getTurtleParser().serialize(event);
			if(serial == null)
				serial = Activator.getJsonParser().serialize(event);
			Activator.logI("PushREST.pushContextEvent",
					"Attempting to send event " + event.getURI() + " to callback " + callback);
			send(callback, serial);
		} catch (MalformedURLException e) {
			throw new PushException("Unable to send message to malformed URL: " + e.getMessage());
		} catch (IOException e) {
			throw new PushException("Unable to send message through communication channel: " + e.getMessage());
		}
	}

	public static void pushServiceCall(String callback, ServiceCall call, String origin) throws PushException {
		try {
			String serial = Activator.getTurtleParser().serialize(call);
			if(serial == null)
				serial = Activator.getJsonParser().serialize(call);
			Activator.logI("PushREST.pushServiceCall",
					"Attempting to send call " + origin + " to callback " + callback);
			send(callback + "?o=" + origin, serial);
		} catch (MalformedURLException e) {
			throw new PushException("Unable to send message to malformed URL: " + e.getMessage());
		} catch (IOException e) {
			throw new PushException("Unable to send message through communication channel: " + e.getMessage());
		}
	}

	private static String send(String callback, String body) throws IOException, MalformedURLException {
		URL url = new URL(callback);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String auth = "Basic " + Base64
				.encodeBytes((Configuration.getServerUSR() + ":" + Configuration.getServerPWD()).getBytes("UTF-8"));
		byte[] data = body.getBytes(Charset.forName("UTF-8"));
		conn.setRequestMethod("POST");
		conn.setInstanceFollowRedirects(false);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setReadTimeout(30000);
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", "" + Integer.toString(data.length));
		conn.setRequestProperty("Authorization", auth);

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		wr.close();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line, response = "";
		while ((line = rd.readLine()) != null) {
			response = response + line + "\n";
		}
		rd.close();

		return response;
	}

}
