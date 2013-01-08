package org.universAAL.rinterop.profile.agent;

import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.universAAL.ontology.profile.Profile;

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
	 * Returns a {@java.util.List} of all user profiles in the
	 * profile log that are associated with the given user.
	 * 
	 * @param userURI
	 *            The URI of the user who performed the profiles
	 * 
	 * @return All profiles that were performed by the user
	 */
	@SuppressWarnings("rawtypes")
	public List getAllUserProfiles(@WebParam(name = "userURI") String userURI);

	/**
	 * Returns a {@java.util.List} of all user profiles in the
	 * profile log that are associated with the given user and are between the
	 * given timestamps.
	 * 
	 * @param userURI
	 *            The URI of the user who performed the treatments
	 * @param timestampFrom
	 *            The lower bound of the period
	 * @param timestampTo
	 *            The upper bound of the period
	 * 
	 * @return all user profiles that were performed by the user in a specific
	 *         period of time
	 */
	@SuppressWarnings("rawtypes")
	public List getUserProfilesBetweenTimestamps(
			@WebParam(name = "userURI") String userURI,
			@WebParam(name = "timestampFrom") long timestampFrom,
			@WebParam(name = "timestampTo") long timestampTo);

	/**
	 * Stores the new profile that was performed by the user.
	 * 
	 * @param userURI
	 *            The URI of the user who performed this profile
	 * @param profile
	 *            The profile that was performed by the user
	 */
	public void userProfileDone(@WebParam(name = "userURI") String userURI,
			@WebParam(name = "profile") Profile profile);

	/**
	 * Returns a {@java.util.List} of all AALSpace profiles in
	 * the profile log that are associated with the given user.
	 * 
	 * @param userURI
	 *            The URI of the user who performed the profiles
	 * 
	 * @return All AALSpace profiles that were performed by the user
	 */
	@SuppressWarnings("rawtypes")
	public List getAllAALSpaceProfiles(
			@WebParam(name = "userURI") String userURI);

	/**
	 * Returns a {@java.util.List} of all AALSpace profiles in
	 * the profile log that are associated with the given user and are between
	 * the given timestamps.
	 * 
	 * @param userURI
	 *            The URI of the user who performed the treatments
	 * @param timestampFrom
	 *            The lower bound of the period
	 * @param timestampTo
	 *            The upper bound of the period
	 * 
	 * @return All AALSpace profiles that were performed by the user in a
	 *         specific period of time
	 */
	@SuppressWarnings("rawtypes")
	public List getAALSpaceProfilesBetweenTimestamps(
			@WebParam(name = "userURI") String userURI,
			@WebParam(name = "timestampFrom") long timestampFrom,
			@WebParam(name = "timestampTo") long timestampTo);

	/**
	 * Stores the new ALLSpace profile that was performed by the user.
	 * 
	 * @param userURI
	 *            The URI of the user who performed this profile
	 * @param profile
	 *            The profile that was performed by the user
	 */
	public void ALLSpaceProfileDone(@WebParam(name = "userURI") String userURI,
			@WebParam(name = "profile") Profile profile);

}