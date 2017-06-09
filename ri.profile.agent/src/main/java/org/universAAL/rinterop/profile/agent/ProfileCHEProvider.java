package org.universAAL.rinterop.profile.agent;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.UserProfile;

/**
 * Interface for the actual profile storage and retrieval.
 * 
 * Implementations of this interface go in
 * {@link org.universAAL.rinterop.profile.agent.impl} package; this way the
 * actual storage of profiles can be expanded to other methods and service
 * providers can select from these methods the one that fits best and has best
 * performance, or they can implement their own storage method.
 * 
 * @author
 */
@WebService(serviceName = "ProfileCHEProvider", portName = "ProfileCHEProviderPort")
public interface ProfileCHEProvider {

	/**
	 * Returns an {@link org.universAAL.ontology.profile.UserProfile} object
	 * from the profile log that are associated with the given user.
	 * 
	 * @param userID
	 *            The ID of the user who performed the user profile
	 * 
	 * @return the user profile profile that were performed by the user
	 */
	public UserProfile getUserProfile(@WebParam(name = "userID") String userID);

	/**
	 * Stores the new {@link org.universAAL.ontology.profile.UserProfile} that
	 * was performed by the user.
	 * 
	 * @param userID
	 *            The ID of the user who performed the user profile
	 * 
	 * @param userProfile
	 *            The user profile that was performed by the user
	 */
	public void addUserProfile(@WebParam(name = "userID") String userID,
			@WebParam(name = "userProfile") UserProfile userProfile);

	/**
	 * Returns an {@link org.universAAL.ontology.profile.AALSpaceProfile} list
	 * from the profile log that are associated with the given user.
	 * 
	 * @param userID
	 *            The ID of the user who performed the AAL space profile
	 * 
	 * @return list of AALSpace profiles, which owner is the given user
	 */
	public List getAALSpaceProfiles(@WebParam(name = "userID") String userID);

	/** for testing purposes **/

	/**
	 * Stores the new {@link org.universAAL.ontology.profile.AALSpaceProfile}
	 * that was performed by the user.
	 * 
	 * @param userID
	 *            The ID of the user who performed the AAL space profile
	 * 
	 * @param aalSpaceProfile
	 *            The AAL space profile that was performed by the user
	 */
	public void addAALSpaceProfile(@WebParam(name = "userID") String userID,
			@WebParam(name = "aalSpaceProfile") AALSpaceProfile aalSpaceProfile);
}
