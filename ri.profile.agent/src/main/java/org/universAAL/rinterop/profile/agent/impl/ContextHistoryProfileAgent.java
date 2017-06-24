/*
	Copyright 2010-2014 UPM http://www.upm.es
	Universidad Politï¿½cnica de Madrdid

	OCO Source Materials
	ï¿½ Copyright IBM Corp. 2011

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
package org.universAAL.rinterop.profile.agent.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.universAAL.commerce.ustore.tools.OnlineStoreManager;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.che.ContextHistoryService;
import org.universAAL.ontology.profile.AALSpace;
import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.UserProfile;
import org.universAAL.ontology.profile.service.ProfilingService;
import org.universAAL.rinterop.profile.agent.ProfileCHEProvider;
import org.universAAL.rinterop.profile.agent.osgi.Activator;
import org.universAAL.rinterop.profile.agent.impl.Queries;

/**
 * This class actually implements the
 * {@link org.universAAL.ProfileCHEProvider.ProfileCHEProvider.treat.logger.ProfileLogger}
 * by using the context history.
 *
 * @author
 */
public class ContextHistoryProfileAgent implements ProfileCHEProvider {

	static final String CONTEXT_HISTORY_HTL_IMPL_NAMESPACE = "http://ontology.universAAL.org/ContextHistoryHTLImpl.owl#";

	private static final String OUTPUT_QUERY_RESULT = CONTEXT_HISTORY_HTL_IMPL_NAMESPACE + "queryResult";

	private static final String USER_URI_PREFIX = "urn:org.universAAL.aal_space:test_env#";

	private static final String TEST_PROVIDER = "http://ontology.universAAL.org/TestProfileProvider.owl#ProfileContextProvider";

	public static String DUMMYUSER = CONTEXT_HISTORY_HTL_IMPL_NAMESPACE + "dummyUser";
	public static String HAS_LOCATION = CONTEXT_HISTORY_HTL_IMPL_NAMESPACE + "hasLocation";

	private ModuleContext context;
	// private AALSpaceEventHandler aalSpaceManager;
	private AALSpaceManager aalSpaceManager;
	private DeployManager deployManager;
	private OnlineStoreManager storeManager;

	/**
	 * Needed for publishing context events
	 */
	// private ContextPublisher cp = null;

	/**
	 * Needed for making service requests
	 */
	private static ServiceCaller caller = null;

	/**
	 * Actual SCaller that issues the calls.
	 */
	private DefaultServiceCaller defaultCaller;

	/**
	 * Constructor.
	 *
	 * @param context
	 */
	public ContextHistoryProfileAgent(ModuleContext context) {
		this.context = context;
		// aalSpaceManager = (AALSpaceEventHandler)getAALSpaceManager();
		aalSpaceManager = getAALSpaceManager();
		deployManager = getDeployManager();
		storeManager = getOnlineStoreManagerClient();
		defaultCaller = new DefaultServiceCaller(context);

		// prepare for context publishing
		ContextProvider info = new ContextProvider(TEST_PROVIDER);
		// ContextProvider info = new ContextProvider(
		// ProfileServices.PROFILE_SERVICES_NAMESPACE +
		// "ProfileContextProvider");
		info.setType(ContextProviderType.controller);
		// System.out.println("Context Provider is:" + info);
		// System.out.println("ContextProvider type:" + info.getProviderType());
		// System.out.println("Is ContextProvider well-formed: " +
		// info.isWellFormed());

		ContextEventPattern cep = new ContextEventPattern();
		cep.addRestriction(
				MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_PREDICATE,
				new Resource(HAS_LOCATION)));
		info.setProvidedEvents(new ContextEventPattern[] { cep });

		// System.out.println("Is well-formed: " + info.isWellFormed());

		// cp = new DefaultContextPublisher(context, info);

		// the DefaultServiceCaller will be used to make ServiceRequest
		caller = new DefaultServiceCaller(context);
	}

	@SuppressWarnings("rawtypes")
	public UserProfile getUserProfile(String userID) {
		String userURI = USER_URI_PREFIX + userID;
		User user = new User(userURI);

		ServiceResponse sr = caller.call(userProfileRequest(user));

		if (sr.getCallStatus() == CallStatus.succeeded) {
			try {
				List outputAsList = sr.getOutput(OUTPUT_QUERY_RESULT, true);

				if ((outputAsList == null) || (outputAsList.size() == 0)) {
					return null;
				}
				return (UserProfile) outputAsList.get(0);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public void addUserProfile(String userID, UserProfile userProfile) {
		String userURI = USER_URI_PREFIX + userID;
		User user = new User(userURI);

		ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
		req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS }, user);
		req.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE }, userProfile);

		caller.call(req);
	}

	private ServiceRequest userProfileRequest(User user) {
		ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
		req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS }, user);

		req.addTypeFilter(new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE },
				UserProfile.MY_URI);

		req.addRequiredOutput(OUTPUT_QUERY_RESULT,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });

		return req;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList getAALSpaceProfiles(String userID) {
		ArrayList aalSpaceProfiles = new ArrayList();
		String userURI = USER_URI_PREFIX + userID;

		ArrayList allAALSpaces = getAALSpaces();
		if (allAALSpaces != null) {
			for (Object aalSpaceObj : allAALSpaces) {
				AALSpace aalSpace = (AALSpace) aalSpaceObj;
				ArrayList ownersURIs = getOwnsOfSpace(aalSpace);
				if (ownersURIs != null) {
					for (Object ownerURIObj : ownersURIs) {
						String ownerURI = ((User) ownerURIObj).getURI();
						if (userURI.equals(ownerURI)) {
							AALSpaceProfile aalSpaceProfile = (AALSpaceProfile) getAALSpaceProfile(aalSpace);
							if (aalSpaceProfile != null) {
								aalSpaceProfiles.add(aalSpaceProfile);
							}
						}
					}
				}
			}
		}
		return aalSpaceProfiles;
	}

	@SuppressWarnings("rawtypes")
	private ArrayList getOwnsOfSpace(Resource space) {
		return genericGetAllOf(space,
				Queries.GETALLOF.replace(Queries.ARG2, AALSpaceProfile.PROP_SPACE_OWNER).replace(Queries.ARGTYPE,
						User.MY_URI),
				Queries.GETALLOFXTRA.replace(Queries.ARG2, AALSpaceProfile.PROP_SPACE_OWNER).replace(Queries.ARGTYPE,
						User.MY_URI));
	}

	@SuppressWarnings("rawtypes")
	private ArrayList genericGetAllOf(Resource input, String queryall, String queryallxtra) {
		String result1 = getResult(
				defaultCaller.call(getDoSPARQLRequest(queryall.replace(Queries.ARG1, input.getURI()))));
		String result2 = getResult(
				defaultCaller.call(getDoSPARQLRequest(queryallxtra.replace(Queries.ARG1, input.getURI()))));
		Resource bag = (Resource) Activator.parser.deserialize(result1 + " " + result2, Queries.AUXBAG);
		return getResultFromBag(bag);
	}

	@SuppressWarnings("rawtypes")
	private ArrayList getAALSpaces() {
		return genericGetAll(Queries.GETALL.replace(Queries.ARGTYPE, AALSpace.MY_URI),
				Queries.GETALLXTRA.replace(Queries.ARGTYPE, AALSpace.MY_URI));
	}

	private Resource getAALSpaceProfile(Resource aalSpace) {
		String resultx = getResult(defaultCaller
				.call(getDoSPARQLRequest(Queries.Q_GET_PRF_OF_SPACE_XTRA.replace(Queries.ARG1, aalSpace.getURI()))));
		Object objx = Activator.parser.deserialize(resultx);
		if (objx == null)
			return null;
		String result = getResult(defaultCaller
				.call(getDoSPARQLRequest(Queries.Q_GET_PRF_OF_SPACE.replace(Queries.ARG1, aalSpace.getURI()))));
		String uri = ((Resource) objx).getURI();
		return (Resource) Activator.parser.deserialize(result, uri);
	}

	@SuppressWarnings("rawtypes")
	private ArrayList genericGetAll(String queryall, String queryallxtra) {
		// Or final choice: construct a bag with the results and a bag with the
		// types. Then combine the RDF in a single string and deserialize. It�s
		// cheating but it works. And it only uses 2 calls and a serialize.
		String result = getResult(defaultCaller.call(getDoSPARQLRequest(queryall)));
		String result2 = getResult(defaultCaller.call(getDoSPARQLRequest(queryallxtra)));
		Resource bag = (Resource) Activator.parser.deserialize(result + " " + result2, Queries.AUXBAG);
		return getResultFromBag(bag);
	}

	/**
	 * Gets all results from a RDF Bag resource and returns them as an ArrayList
	 * of uAAL ontologies.
	 *
	 * @param bag
	 *            The RDF Bag Resource
	 * @return The ArrayList with results
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList getResultFromBag(Resource bag) {
		if (bag != null) {
			Object content = bag.getProperty(Queries.AUXBAGPROP);
			ArrayList list = new ArrayList();
			OntologyManagement mng = OntologyManagement.getInstance();
			if (content instanceof List) {
				Iterator iter = ((ArrayList) content).iterator();
				while (iter.hasNext()) {
					Resource res = (Resource) iter.next();
					list.add(mng.getResource(mng.getMostSpecializedClass(res.getTypes()), res.getURI()));
				}
			} else {
				Resource res = (Resource) content;
				list.add(mng.getResource(mng.getMostSpecializedClass(res.getTypes()), res.getURI()));
			}
			return list;
		} else {
			return null;
		}
	}

	/**
	 * Helper method to get the result from the Service Response of CHE.
	 *
	 * @param call
	 *            The service response
	 * @return the result SPARQL string
	 */
	@SuppressWarnings("rawtypes")
	private String getResult(ServiceResponse call) {
		Object returnValue = null;
		List outputs = call.getOutputs();
		if (outputs == null) {
			return null;
		} else {
			for (Iterator i = outputs.iterator(); i.hasNext();) {
				ProcessOutput output = (ProcessOutput) i.next();
				if (output.getURI().equals(OUTPUT_QUERY_RESULT)) {
					if (returnValue == null) {
						returnValue = output.getParameterValue();
					}
				}
			}
			if (returnValue instanceof String) {
				return (String) returnValue;
			} else {
				return null;
			}
		}
	}

	/**
	 * Prepares the call to the Do SPARQL service of CHE.
	 *
	 * @param query
	 *            The SPARQL query
	 * @return The prepared request
	 */
	private ServiceRequest getDoSPARQLRequest(String query) {
		ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(null), null);
		MergedRestriction r = MergedRestriction.getFixedValueRestriction(ContextHistoryService.PROP_PROCESSES, query);

		getQuery.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });
		getQuery.addSimpleOutputBinding(new ProcessOutput(OUTPUT_QUERY_RESULT),
				new PropertyPath(null, true, new String[] { ContextHistoryService.PROP_RETURNS }).getThePath());
		return getQuery;
	}

	public AALSpaceManager getAALSpaceManager() {
		LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
				new Object[] { "Fetching the AALSpaceManager..." }, null);
		if (aalSpaceManager == null) {
			Object ref = context.getContainer().fetchSharedObject(context,
					new Object[] { AALSpaceManager.class.getName().toString() });
			if (ref != null) {
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "AALSpaceManager found!" }, null);
				aalSpaceManager = (AALSpaceManager) ref;
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "AALSpaceManager fetched" }, null);
				return aalSpaceManager;
			} else {
				LogUtils.logWarn(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "No AALSpaceManager found" }, null);
				return null;
			}
		} else
			return aalSpaceManager;

		// LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
		// "contextHistoryProfileAgent", new Object[]
		// {"Fetching the AALSpaceManager..."}, null);
		// Object ref = context.getContainer().fetchSharedObject(context, new
		// Object[]
		// {AALSpaceManager.class.getName().toString()});
		// if (ref != null) {
		// LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
		// "contextHistoryProfileAgent", new Object[]
		// {"AALSpaceManager found!"}, null);
		// if ((AALSpaceManager)ref instanceof AALSpaceEventHandler)
		// aalSpaceManager = (AALSpaceEventHandler)ref;
		// LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
		// "contextHistoryProfileAgent", new Object[]
		// {"AALSpaceManager fetched"}, null);
		// } else {
		// LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
		// "contextHistoryProfileAgent", new Object[]
		// {"No AALSpaceManager found"}, null);
		// }
		// return aalSpaceManager;
	}

	public DeployManager getDeployManager() {
		LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
				new Object[] { "Fetching the DeployManager..." }, null);
		if (deployManager == null) {
			Object ref = context.getContainer().fetchSharedObject(context,
					new Object[] { DeployManager.class.getName().toString() });
			if (ref != null) {
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "DeployManager found!" }, null);
				deployManager = (DeployManager) ref;
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "DeployManager fetched" }, null);
				return deployManager;
			} else {
				LogUtils.logWarn(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "No DeployManager found" }, null);
				return null;
			}
		} else
			return deployManager;

	}

	public OnlineStoreManager getOnlineStoreManagerClient() {
		LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
				new Object[] { "Fetching the OnlineStoreManager client..." }, null);
		if (storeManager == null) {
			Object ref = context.getContainer().fetchSharedObject(context,
					new Object[] { OnlineStoreManager.class.getName().toString() });
			if (ref != null) {
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "OnlineStoreManager client found!" }, null);
				storeManager = (OnlineStoreManager) ref;
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "OnlineStoreManager client fetched" }, null);
				return storeManager;
			} else {
				LogUtils.logWarn(context, ContextHistoryProfileAgent.class, "contextHistoryProfileAgent",
						new Object[] { "No OnlineStoreManager client found" }, null);
				return null;
			}
		} else
			return storeManager;
	}

	public void close() {
		defaultCaller.close();
	}

	/** for testing purposes **/

	public void addAALSpaceProfile(String userID, AALSpaceProfile aalSpaceProfile) {
		String userURI = USER_URI_PREFIX + userID;
		User owner = new User(userURI);
		// check user authorization

		// User owner = aalSpaceProfile.getOwner();
		// if(owner==null){
		// aalSpaceProfile.setOwner(user);
		// }
		// else{
		// if(owner!=user){//space can't have more than one owner
		// }
		// }

		addAALSpaceProfile(aalSpaceProfile);

		// ServiceRequest req = new ServiceRequest(new ProfilingService(),
		// null);
		// req.addValueFilter(new String[] {ProfilingService.PROP_CONTROLS},
		// owner);
		// req.addAddEffect(new String[] {ProfilingService.PROP_CONTROLS,
		// Profilable.PROP_HAS_PROFILE}, aalSpaceProfile);

		// caller.call(req);

		String aalSpaceURI = aalSpaceProfile.getURI() + "_SPACE";
		AALSpace aalSpace = new AALSpace(aalSpaceURI);
		addAALSpace(aalSpace);
		addProfToProfilable(aalSpace, aalSpaceProfile);

		addOwnToSpace(aalSpace, owner);
	}

	private void addAALSpaceProfile(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	private void addAALSpace(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	private void addOwnToSpace(Resource aalSpace, Resource owner) {
		genericAddToSpace(aalSpace, owner,
				Queries.ADDTOPROFILABLE.replace(Queries.ARGTYPE, AALSpaceProfile.PROP_SPACE_OWNER));
	}

	private void genericAddToSpace(Resource input, Resource what, String queryadd) {
		String serialized = Activator.parser.serialize(what);
		String[] split = splitPrefixes(serialized);
		defaultCaller.call(getDoSPARQLRequest(split[0] + " " + queryadd.replace(Queries.ARG1, input.getURI())
				.replace(Queries.ARG2, what.getURI()).replace(Queries.ARGTURTLE, split[1])));
	}

	private void genericAdd(Resource input, String addquery) {
		String serialized = Activator.parser.serialize(input);
		String[] split = splitPrefixes(serialized);
		defaultCaller.call(getDoSPARQLRequest(split[0] + " " + addquery.replace(Queries.ARGTURTLE, split[1])));
	}

	/**
	 * Splits a Turtle serialized string into prefixes and content, so it can be
	 * used inside SPARQL queries.
	 *
	 * @param serialized
	 *            The turtle string
	 * @return An array of length 2. The first item [0] is the string with the
	 *         prefixes, and the second [1] is the string with the triples
	 *         content
	 */
	private static String[] splitPrefixes(String serialized) {
		int lastprefix = 0, lastprefixdot = 0, lastprefixuri = 0;
		lastprefix = serialized.toLowerCase().lastIndexOf("@prefix");
		if (lastprefix >= 0) {
			lastprefixuri = serialized.substring(lastprefix).indexOf(">");
			lastprefixdot = serialized.substring(lastprefix + lastprefixuri).indexOf(".");
		}
		String[] result = new String[2];
		result[0] = serialized.substring(0, lastprefixuri + lastprefixdot + lastprefix + 1).replace("@", " ")
				.replace(">.", "> ").replace(" .", " ").replace(". ", " ");
		result[1] = serialized.substring(lastprefixuri + lastprefixdot + lastprefix + 1);
		return result;
	}

	private void addProfToProfilable(Resource input, Resource what) {
		genericAddToSpace(input, what, Queries.ADDPROFTOPROFILABLE);
	}
}
