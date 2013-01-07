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
package org.universAAL.rinterop.profile.agent;

import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.rinterop.profile.agent.impl.ContextHistoryProfileAgent;

/**
 * This class provides the profile logger services.
 * 
 * @author
 */
public class ProfileProvider {

	private static final String OUTPUT_PROFILES = "http://ontology.universAAL.org/RemoteProfiles.owl#matchingProfiles";
	// the actual profile logger
	private ProfileCHEProvider profileCHEProvider = null;

	// prepare a standard error message for later use
	// private static final ServiceResponse invalidInput = new ServiceResponse(
	// CallStatus.serviceSpecificFailure);
	// static {
	// invalidInput.addOutput(new ProcessOutput(
	// ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input!"));
	// }

	// /**
	// * Constructor.
	// *
	// * @param context
	// * @param realizedServices
	// */
	// protected ProfileLoggerProvider(ModuleContext context, ServiceProfile[]
	// realizedServices) {
	// super(context, realizedServices);
	// }

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public ProfileProvider(ModuleContext context) {
		// // as a service providing component, we have to extend ServiceCallee
		// // this in turn requires that we introduce which services we would
		// like
		// // to provide to the universAAL-based AAL Space
		// super(context, ProfileLoggerServices.profiles);
		//
		// // the actual implementation of the profile logger
		profileCHEProvider = new ContextHistoryProfileAgent(context);
	}

	// @Override
	// public void communicationChannelBroken() {
	// // TODO Auto-generated method stub
	// }

	/**
	 * Creates a service response that including all the profiles that are
	 * associated with the given user.
	 * 
	 * @param userURI
	 *            The URI of the user
	 */
	@SuppressWarnings("rawtypes")
	public ServiceResponse getAllUserProfiles(String userURI) {
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);

		List profilesList = profileCHEProvider.getAllUserProfiles(userURI);
		sr.addOutput(new ProcessOutput(OUTPUT_PROFILES, profilesList));

		return sr;
	}

	/**
	 * Creates a service response that including all the profiles that are
	 * associated with the given user and are between the given timestamps.
	 * 
	 * @param userURI
	 *            The URI of the user
	 * @param timestampFrom
	 *            The lower bound of the period
	 * @param timestampTo
	 *            The upper bound of the period
	 */
	@SuppressWarnings("rawtypes")
	public ServiceResponse getUserProfilesBetweenTimestamps(String userURI,
			long timestampFrom, long timestampTo) {
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);

		List profilesList = profileCHEProvider
				.getUserProfilesBetweenTimestamps(userURI, timestampFrom,
						timestampTo);
		sr.addOutput(new ProcessOutput(OUTPUT_PROFILES, profilesList));

		return sr;
	}

	/**
	 * Creates a service response for storing a profile that was performed by
	 * the given user.
	 * 
	 * @param userURI
	 *            The URI of the user
	 * @param profile
	 *            The profile that was performed by the user
	 */
	public ServiceResponse userProfileDone(String userURI, Profile profile) {
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);

		profileCHEProvider.userProfileDone(userURI, profile);

		return sr;
	}
}
