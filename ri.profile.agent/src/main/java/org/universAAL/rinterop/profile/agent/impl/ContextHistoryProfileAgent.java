/*	
	Copyright 2010-2014 UPM http://www.upm.es
	Universidad Polit�cnica de Madrdid
	
	OCO Source Materials
	� Copyright IBM Corp. 2011
	
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

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.commerce.ustore.tools.OnlineStoreManager;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.mpa.MPACard;
import org.universAAL.middleware.interfaces.mpa.MPAStatus;
import org.universAAL.middleware.interfaces.mpa.Pair;
import org.universAAL.middleware.interfaces.mpa.model.Part;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.api.InstallationResults;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.che.ContextHistoryService;
import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.UserProfile;
import org.universAAL.rinterop.profile.agent.ProfileCHEProvider;
import org.universAAL.rinterop.profile.agent.ServiceInfo;

/**
 * This class actually implements the
 * {@link org.universAAL.ProfileCHEProvider.ProfileCHEProvider.treat.logger.ProfileLogger}
 * by using the context history.
 * 
 * @author
 */
public class ContextHistoryProfileAgent implements ProfileCHEProvider {

	private static final String CONTEXT_HISTORY_HTL_IMPL_NAMESPACE = "http://ontology.universAAL.org/ContextHistoryHTLImpl.owl#";

	private static final String OUTPUT_QUERY_RESULT = CONTEXT_HISTORY_HTL_IMPL_NAMESPACE
			+ "queryResult";

	private static final String TEST_PROVIDER = "http://ontology.universAAL.org/TestProfileProvider.owl#ProfileContextProvider";

	public static String DUMMYUSER = CONTEXT_HISTORY_HTL_IMPL_NAMESPACE
			+ "dummyUser";
	public static String HAS_LOCATION = CONTEXT_HISTORY_HTL_IMPL_NAMESPACE
			+ "hasLocation";

	private ModuleContext context;
	private AALSpaceEventHandler aalSpaceManager;
	private DeployManager deployManager;
	private OnlineStoreManager storeManager;
	/**
	 * Needed for publishing context events
	 */
	private ContextPublisher cp = null;

	

	/**
	 * Needed for making service requests
	 */
	private static ServiceCaller caller = null;

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public ContextHistoryProfileAgent(ModuleContext context) {
		this.context = context;
		aalSpaceManager = (AALSpaceEventHandler) getAALSpaceManager();
		deployManager = getDeployManager();
		storeManager = getOnlineStoreManagerClient();

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
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));
		info.setProvidedEvents(new ContextEventPattern[] { cep });

		// System.out.println("Is well-formed: " + info.isWellFormed());

		cp = new DefaultContextPublisher(context, info);

		// the DefaultServiceCaller will be used to make ServiceRequest
		caller = new DefaultServiceCaller(context);
	}

	/**
	 * Returns a {@java.util.List} of all User profiles in the
	 * profile log that are associated to the given user.
	 * 
	 * @param userURI
	 *            The URI of the user who performed the profiles
	 * 
	 * @return All the User profiles that were performed by the user
	 */
	@SuppressWarnings("rawtypes")
	public List getAllUserProfiles(String userURI) {

		ServiceResponse sr = caller.call(allUserProfileLogRequest(userURI));

		if (sr.getCallStatus() == CallStatus.succeeded) {

			try {
				List userProfileList = sr.getOutput(OUTPUT_QUERY_RESULT, true);

				if (userProfileList == null || userProfileList.size() == 0) {
					// LogUtils.logInfo(Activator.mc,
					// ContextHistoryProfileLogger.class,
					// "getAllProfileLog",
					// new Object[] { "there are no profiles in the log" },
					// null);
					return null;
				}
				return userProfileList;

			} catch (Exception e) {
				// LogUtils.logError(Activator.mc,
				// ContextHistoryProfileLogger.class,
				// "getAllProfileLog", new Object[] { "got exception",
				// e.getMessage() }, e);
				return null;
			}
		} else {
			// LogUtils.logWarn(Activator.mc,
			// ContextHistoryProfileLogger.class,
			// "getAllProfileLog",
			// new Object[] { "callstatus is not succeeded" }, null);
			return null;
		}
	}

	/**
	 * Returns a {@java.util.List} of all User profiles in the
	 * profile log that are associated to the given user and are between the
	 * given timestamps.
	 * 
	 * @param userURI
	 *            The URI of the user who performed the profiles
	 * @param timestampFrom
	 *            The lower bound of the period
	 * @param timestampTo
	 *            The upper bound of the period
	 * 
	 * @return The User profiles that were performed by the user in a specific
	 *         period of time
	 */
	@SuppressWarnings("rawtypes")
	public List getUserProfilesBetweenTimestamps(String userURI,
			long timestampFrom, long timestampTo) {

		ServiceResponse sr = caller
				.call(userProfileLogBetweenTimestampsRequest(userURI,
						timestampFrom, timestampTo));

		if (sr.getCallStatus() == CallStatus.succeeded) {
			try {
				List userProfileList = sr.getOutput(OUTPUT_QUERY_RESULT, true);

				if (userProfileList == null || userProfileList.size() == 0) {
					// LogUtils.logInfo(Activator.mc,
					// ContextHistoryProfileLogger.class,
					// "getProfileLogBetweenTimestamps",
					// new Object[] { "there are no profiles in the log" },
					// null);
					return null;
				}
				return userProfileList;

			} catch (Exception e) {
				// LogUtils.logError(Activator.mc,
				// ContextHistoryProfileLogger.class,
				// "getAllProfileLog", new Object[] { "got exception",
				// e.getMessage() }, e);
				return null;
			}
		} else {
			// LogUtils.logWarn(Activator.mc,
			// ContextHistoryProfileLogger.class,
			// "getAllProfileLog",
			// new Object[] { "callstatus is not succeeded" }, null);
			return null;
		}
	}

	/**
	 * Stores the new User profile that was performed by the user in the context
	 * history.
	 * 
	 * @param userURI
	 *            The URI of the user who performed this profile
	 * @param profile
	 *            The User profile that was performed by the user
	 */
	public void userProfileDone(String userURI, Profile profile) {

		UserProfile userProfile = new UserProfile(
				CONTEXT_HISTORY_HTL_IMPL_NAMESPACE + "UserProfile");

		userProfile.setProperty(Profile.PROP_HAS_SUB_PROFILE, profile);
		cp.publish(new ContextEvent(userProfile, Profile.PROP_HAS_SUB_PROFILE));
		// userProfile.setProperty(UserProfile.PROP_HAS_SUB_PROFILE, profile);
		// cp.publish(new ContextEvent(userProfile,
		// UserProfile.PROP_HAS_SUB_PROFILE));
	}

	/**
	 * Creates a ServiceRequest to retrieve all User profiles that were
	 * performed by the given user.
	 * 
	 * @param userURI
	 *            The URI of the user that performed these profiles
	 * 
	 * @return The User profiles that were performed
	 */
	public ServiceRequest allUserProfileLogRequest(String userURI) {// userURI
																		// is
																		// not
																		// used

		String query = null;
//		ServiceRequest request = new ServiceRequest(new ContextHistoryService(null), null);

		Resource involvedHumanUser = new Resource(userURI);

		ServiceRequest request = new ServiceRequest(new ContextHistoryService(// new
																				// ContextHistoryService(userURI)
				userURI), involvedHumanUser);// second parameter Resource
												// involvedHumanUser can be
												// userURI

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(
				ContextHistoryService.PROP_PROCESSES, query);// query is null =>
																// getFixedValueRestriction
																// returns
																// null???

		System.out.println("[ProfileAgent] The requested service uri is: "
				+ request.getRequestedService().getURI());

		request.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });// returns
																		// false
																		// and
																		// do
																		// nothing,
																		// because
																		// r is
																		// null

		request.addSimpleOutputBinding(new ProcessOutput(OUTPUT_QUERY_RESULT),
				new PropertyPath(null, true,// PrepertyPath the first param is
											// the uri of the object
						new String[] { ContextHistoryService.PROP_RETURNS })
						.getThePath());

		return request;
	}

	/**
	 * Creates a ServiceRequest to retrieve all the profiles that were performed
	 * by the given user between the given timestamps.
	 * 
	 * @param userURI
	 *            The URI of the user that perfomed these profiles
	 * @param timestampFrom
	 *            The lower bound of the period
	 * @param timestampTo
	 *            The upper bound of the period
	 * 
	 * @return The profiles that were performed
	 */
	public ServiceRequest userProfileLogBetweenTimestampsRequest(
			String userURI, long timestampFrom, long timestampTo) {//???

		String query = null;

		ServiceRequest request = new ServiceRequest(new ContextHistoryService(
				null), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(
				ContextHistoryService.PROP_PROCESSES, query);

		request.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });

		request.addSimpleOutputBinding(new ProcessOutput(OUTPUT_QUERY_RESULT),
				new PropertyPath(null, true,
						new String[] { ContextHistoryService.PROP_RETURNS })
						.getThePath());

		return request;
	}

	@SuppressWarnings("rawtypes")
	public List getAllAALSpaceProfiles(String userURI) {
		ServiceResponse sr = caller.call(allAALSpaceProfileLogRequest(userURI));

		if (sr.getCallStatus() == CallStatus.succeeded) {

			try {
				List spaceProfileList = sr.getOutput(OUTPUT_QUERY_RESULT, true);

				if (spaceProfileList == null || spaceProfileList.size() == 0) {
					// LogUtils.logInfo(Activator.mc,
					// ContextHistoryProfileLogger.class,
					// "getAllProfileLog",
					// new Object[] { "there are no profiles in the log" },
					// null);
					return null;
				}
				return spaceProfileList;

			} catch (Exception e) {
				// LogUtils.logError(Activator.mc,
				// ContextHistoryProfileLogger.class,
				// "getAllProfileLog", new Object[] { "got exception",
				// e.getMessage() }, e);
				return null;
			}
		} else {
			// LogUtils.logWarn(Activator.mc,
			// ContextHistoryProfileLogger.class,
			// "getAllProfileLog",
			// new Object[] { "callstatus is not succeeded" }, null);
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public List getAALSpaceProfilesBetweenTimestamps(String userURI,
			long timestampFrom, long timestampTo) {
		ServiceResponse sr = caller
				.call(AALSpaceProfileLogBetweenTimestampsRequest(userURI,
						timestampFrom, timestampTo));

		if (sr.getCallStatus() == CallStatus.succeeded) {
			try {
				List spaceProfileList = sr.getOutput(OUTPUT_QUERY_RESULT, true);

				if (spaceProfileList == null || spaceProfileList.size() == 0) {
					// LogUtils.logInfo(Activator.mc,
					// ContextHistoryProfileLogger.class,
					// "getProfileLogBetweenTimestamps",
					// new Object[] { "there are no profiles in the log" },
					// null);
					return null;
				}
				return spaceProfileList;

			} catch (Exception e) {
				// LogUtils.logError(Activator.mc,
				// ContextHistoryProfileLogger.class,
				// "getAllProfileLog", new Object[] { "got exception",
				// e.getMessage() }, e);
				return null;
			}
		} else {
			// LogUtils.logWarn(Activator.mc,
			// ContextHistoryProfileLogger.class,
			// "getAllProfileLog",
			// new Object[] { "callstatus is not succeeded" }, null);
			return null;
		}
	}

	public void ALLSpaceProfileDone(String userURI, Profile profile) {
		AALSpaceProfile spaceProfile = new AALSpaceProfile(
				CONTEXT_HISTORY_HTL_IMPL_NAMESPACE + "AALSpaceProfile");
		spaceProfile.setProperty(Profile.PROP_HAS_SUB_PROFILE, profile);
		cp.publish(new ContextEvent(spaceProfile, Profile.PROP_HAS_SUB_PROFILE));
	}

	/**
	 * Creates a ServiceRequest to retrieve all AAL space profiles that were
	 * performed by the given user.
	 * 
	 * @param userURI
	 *            The URI of the user that performed these profiles
	 * 
	 * @return The AAL space profiles that were performed
	 */
	public ServiceRequest allAALSpaceProfileLogRequest(String userURI) {// userURI
																			// is
																			// not
																			// used

		String query = null;
		
//		ServiceRequest request = new ServiceRequest(new ContextHistoryService(null), null);

		Resource involvedHumanUser = new Resource(userURI);//tova az sym go pisala

		ServiceRequest request = new ServiceRequest(new ContextHistoryService(// new
																				// ContextHistoryService(userURI)
				userURI), involvedHumanUser);// second parameter Resource
												// involvedHumanUser can be
												// userURI

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(
				ContextHistoryService.PROP_PROCESSES, query);// query is null =>
																// getFixedValueRestriction
																// returns
																// null???

		System.out.println("[ProfileAgent] The requested service uri is: "
				+ request.getRequestedService().getURI());

		request.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });// returns
																		// false
																		// and
																		// do
																		// nothing,
																		// because
																		// r is
																		// null

		request.addSimpleOutputBinding(new ProcessOutput(OUTPUT_QUERY_RESULT),
				new PropertyPath(null, true,// PrepertyPath the first param is
											// the uri of the object
						new String[] { ContextHistoryService.PROP_RETURNS })
						.getThePath());

		return request;
	}

	/**
	 * Creates a ServiceRequest to retrieve all ALLSpace profiles that were
	 * performed by the given user between the given timestamps.
	 * 
	 * @param userURI
	 *            The URI of the user that perfomed these profiles
	 * @param timestampFrom
	 *            The lower bound of the period
	 * @param timestampTo
	 *            The upper bound of the period
	 * 
	 * @return The AALSpace profiles that were performed
	 */
	public ServiceRequest AALSpaceProfileLogBetweenTimestampsRequest(
			String userURI, long timestampFrom, long timestampTo) {

		String query = null;

		ServiceRequest request = new ServiceRequest(new ContextHistoryService(
				null), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(
				ContextHistoryService.PROP_PROCESSES, query);

		request.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });

		request.addSimpleOutputBinding(new ProcessOutput(OUTPUT_QUERY_RESULT),
				new PropertyPath(null, true,
						new String[] { ContextHistoryService.PROP_RETURNS })
						.getThePath());

		return request;
	}

	public AALSpaceManager getAALSpaceManager() {
		LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
				"contextHistoryProfileAgent",
				new Object[] { "Fetching the AALSpaceManager..." }, null);
		Object ref = context.getContainer().fetchSharedObject(context,
				new Object[] { AALSpaceManager.class.getName().toString() });
		if (ref != null) {
			LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
					"contextHistoryProfileAgent",
					new Object[] { "AALSpaceManager found!" }, null);
			if ((AALSpaceManager) ref instanceof AALSpaceEventHandler)
				aalSpaceManager = (AALSpaceEventHandler) ref;
			LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
					"contextHistoryProfileAgent",
					new Object[] { "AALSpaceManager fetched" }, null);
		} else {
			LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
					"contextHistoryProfileAgent",
					new Object[] { "No AALSpaceManager found" }, null);
		}
		return aalSpaceManager;
	}

	public DeployManager getDeployManager() {
		LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
				"contextHistoryProfileAgent",
				new Object[] { "Fetching the DeployManager..." }, null);
		if (deployManager == null) {
			Object ref = context.getContainer().fetchSharedObject(context,
					new Object[] { DeployManager.class.getName().toString() });
			if (ref != null) {
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
						"contextHistoryProfileAgent",
						new Object[] { "DeployManager found!" }, null);
				deployManager = (DeployManager) ref;
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
						"contextHistoryProfileAgent",
						new Object[] { "DeployManager fetched" }, null);
				return deployManager;
			} else {
				LogUtils.logWarn(context, ContextHistoryProfileAgent.class,
						"contextHistoryProfileAgent",
						new Object[] { "No DeployManager found" }, null);
				return null;
			}
		} else
			return deployManager;

	}
	
	public OnlineStoreManager getOnlineStoreManagerClient(){
		LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
				"contextHistoryProfileAgent",
				new Object[] { "Fetching the OnlineStoreManager client..." }, null);
		if (storeManager == null) {
			Object ref = context.getContainer().fetchSharedObject(context,
					new Object[] { OnlineStoreManager.class.getName().toString() });
			if (ref != null) {
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
						"contextHistoryProfileAgent",
						new Object[] { "OnlineStoreManager client found!" }, null);
				storeManager = (OnlineStoreManager) ref;
				LogUtils.logDebug(context, ContextHistoryProfileAgent.class,
						"contextHistoryProfileAgent",
						new Object[] { "OnlineStoreManager client fetched" }, null);
				return storeManager;
			} else {
				LogUtils.logWarn(context, ContextHistoryProfileAgent.class,
						"contextHistoryProfileAgent",
						new Object[] { "No OnlineStoreManager client found" }, null);
				return null;
			}
		} else
			return storeManager;
	}
}
