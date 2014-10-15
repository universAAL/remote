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
package org.universAAL.ri.internetgateway;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.ri.wsdlToolkit.invocation.InvocationResult;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.parser.WSDLParser;

public class InternetGateway {

	static private List<ParsedWSDLDefinition> parsedWSDLDefinitions = new ArrayList<ParsedWSDLDefinition>();

	static public boolean registerWebService(String url) {
		if (ping(url, 100000)) {
			try {
				ParsedWSDLDefinition definition = new ParsedWSDLDefinition();
				definition = WSDLParser.parseWSDLwithAxis(url, true, true);
				parsedWSDLDefinitions.add(definition);
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		} else
			return false;
	}

	static public ParsedWSDLDefinition getWebServiceDefinition(String url){
		for(int i=0;i<parsedWSDLDefinitions.size();i++){
			ParsedWSDLDefinition def=parsedWSDLDefinitions.get(i);
			if(def!=null){
				if(def.getWsdlURL().toString().equalsIgnoreCase(url)){
					return def;
				}
			}
		}
		return null;
	}
	
	
	public static boolean ping(String url, int timeout) {
		url = url.replaceFirst("https", "http"); // Otherwise an exception may
													// be thrown on invalid SSL
													// certificates.
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url)
					.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		} catch (Exception exception) {
			return false;
		}
	}
}
