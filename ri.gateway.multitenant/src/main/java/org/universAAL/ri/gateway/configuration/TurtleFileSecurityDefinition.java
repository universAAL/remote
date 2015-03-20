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
package org.universAAL.ri.gateway.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.operations.DenyDefault;
import org.universAAL.ri.gateway.operations.MessageOperationChain;
import org.universAAL.ri.gateway.operations.OperationChainManager;
import org.universAAL.ri.gateway.operations.ParameterCheckOpertaionChain;


/**
 * Implementation that reads and interprets security configuration from a turtle file.
 * @author amedrano
 *
 */
public class TurtleFileSecurityDefinition implements OperationChainManager {

    public static final String NAMESPACE = "http://ontology.universAAL.org/Gateway.owl#";
	
	public static final String INBOUND_POLICY = NAMESPACE+"inboundPolicy";
	public static final String INBOUND_MATCH = NAMESPACE+"inboundMatches";
	public static final String OUTBOUND_POLICY = NAMESPACE+"outboundPolicy";
	public static final String OUTBOUND_MATCH = NAMESPACE+"outboundMatches";

	public static final String IMPORT_POLICY = NAMESPACE+"importPolicy";
	public static final String IMPORT_MATCH = NAMESPACE+"importMatches";
	public static final String EXPORT_POLICY = NAMESPACE+"exportPolicy";
	public static final String EXPORT_MATCH = NAMESPACE+"exportMatches";
	
	private Resource def;

	public TurtleFileSecurityDefinition(URL url){
		try {
			Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
			if (s.hasNext()) {
				String src = s.next();
				def = (Resource) Gateway.getInstance().serializer.getObject().deserialize(src);
			}else{
				new RuntimeException("URL: " + url.toString() + " is empty.");
			}
		} catch (IOException e) {
			throw new RuntimeException("URL: " + url.toString() + " cannot be loaded for security definition", e);
		} catch (RuntimeException e) {
			throw new RuntimeException("URL: " + url + " cannot be loaded for security definition", e);
		}
	}
	
	

	private abstract class Match {
		/**
		 * The matchable to match against.
		 */
		List match;
		/**
		 * true is whitelisting, false is blacklisting
		 */
		boolean policy; 

		/**
		 * @param match
		 * @param policy
		 */
		public Match(Matchable match, boolean policy) {
			super();
			this.match = new ArrayList();
			this.match.add(match);
			this.policy = policy;
		}
		
		/**
		 * @param match
		 * @param policy
		 */
		public Match(List match, boolean policy) {
			super();
			this.match = match;
			this.policy = policy;
		}
		
		boolean isMember(Matchable match){
			for (Object m : this.match) {
				if (m instanceof Matchable && ((Matchable)m).matches(match))
					return true;
			}
			return false;
		}
	}
	
	private class RegChecker extends Match implements ParameterCheckOpertaionChain{

		/**
		 * @param match
		 * @param policy
		 */
		public RegChecker(Matchable match, boolean policy) {
			super(match, policy);
		}

		/**
		 * @param match
		 * @param policy
		 */
		public RegChecker(List match, boolean policy) {
			super(match, policy);
		}
		
		boolean isSubset(List<Resource> matchl){
			for (Object m : matchl) {
				if (m instanceof Matchable && !isMember((Matchable)m)){
					return false;
				}
			}
			return matchl.size() !=0;
		}
		
		/**
		 * @param res
		 * @return policy XNOR res matchning match. 
		 */
		private boolean checkB(Resource[] res){
			
			try {
				return policy == isSubset(new ArrayList<Resource>(Arrays.asList(res)));
			} catch (Exception e) {
				LogUtils.logError(Gateway.getInstance().context,
						getClass(),
						"checkB", 
						new String[]{"unable to determine if subset.", " Assuming not"},
						e);
				return false;
			}
	
		}
		
		/** {@inheritDoc} */
		public OperationResult check(Resource[] params) {
			return checkB(params)? OperationResult.ALLOW: OperationResult.DENY;
		}
		
	}
	
	private class MessageChecker extends Match implements MessageOperationChain{

		/**
		 * @param match
		 * @param policy
		 */
		public MessageChecker(Matchable match, boolean policy) {
			super(match, policy);
		}
		
		/**
		 * @param match
		 * @param contains
		 */
		public MessageChecker(List match, boolean contains) {
			super(match, contains);
		}

		/**
		 * @param res
		 * @return policy XNOR res matchning match. 
		 */
		private boolean checkB(Resource res){
			try {
				return policy == isMember((Matchable) res);
			} catch (Exception e) {
				return false;
			}
			
		}

		/** {@inheritDoc} */
		public OperationResult check(ScopedResource message) {
			return checkB(message)? OperationResult.ALLOW: OperationResult.DENY;
		}
		
	}
	
	
	/** {@inheritDoc} */
	public ParameterCheckOpertaionChain getImportOperationChain() {
		String policy = (String) def.getProperty(IMPORT_POLICY);
		Object match = def.getProperty(IMPORT_MATCH);
		if (match instanceof Resource && ((Resource)match).getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
			match = Collections.EMPTY_LIST;
		if (match instanceof Matchable)
			return new RegChecker((Matchable)match, policy.contains("Whitelist"));
		if (match instanceof List)
			return new RegChecker((List)match, policy.contains("Whitelist"));
		LogUtils.logWarn(Gateway.getInstance().context, getClass(), 
				"getImportOperationChain", 
				"Not matchable security definition, assuming Deny by default.");
		return new DenyDefault();
	}

	/** {@inheritDoc} */
	public ParameterCheckOpertaionChain getExportOperationChain() {
		String policy = (String) def.getProperty(EXPORT_POLICY);
		Object match = def.getProperty(EXPORT_MATCH);
		if (match instanceof Resource && ((Resource)match).getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
			match = Collections.EMPTY_LIST;
		if (match instanceof Matchable)
			return new RegChecker((Matchable)match, policy.contains("Whitelist"));
		if (match instanceof List)
			return new RegChecker((List)match, policy.contains("Whitelist"));
		LogUtils.logWarn(Gateway.getInstance().context, getClass(), 
				"getExportOperationChain", 
				"Not matchable security definition, assuming Deny by default.");
		return new DenyDefault();
	}

	/** {@inheritDoc} */
	public MessageOperationChain getIncomingMessageOperationChain() {
		String policy = (String) def.getProperty(INBOUND_POLICY);
		Object match = (Matchable) def.getProperty(INBOUND_MATCH);
		if (match instanceof Resource && ((Resource)match).getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
			match = Collections.EMPTY_LIST;
		if (match instanceof Matchable)
			return new MessageChecker((Matchable)match, policy.contains("Whitelist"));
		if (match instanceof List)
			return new MessageChecker((List)match, policy.contains("Whitelist"));
		LogUtils.logWarn(Gateway.getInstance().context, getClass(), 
				"getIncommingMessageOperationChain", 
				"Not matchable security definition, assuming Deny by default.");
		return new DenyDefault();
	}

	/** {@inheritDoc} */
	public MessageOperationChain getOutgoingMessageOperationChain() {
		String policy = (String) def.getProperty(OUTBOUND_POLICY);
		Object match = (Matchable) def.getProperty(OUTBOUND_MATCH);
		if (match instanceof Resource && ((Resource)match).getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
			match = Collections.EMPTY_LIST;
		if (match instanceof Matchable)
			return new MessageChecker((Matchable)match, policy.contains("Whitelist"));
		if (match instanceof List)
			return new MessageChecker((List)match, policy.contains("Whitelist"));
		LogUtils.logWarn(Gateway.getInstance().context, getClass(), 
				"getOutgoingMessageOperationChain", 
				"Not matchable security definition, assuming Deny by default.");
		return new DenyDefault();
	}

}
