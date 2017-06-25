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
package org.universAAL.ri.gateway;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.managers.api.SpaceListener;
import org.universAAL.middleware.managers.api.SpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;

/**
 * @author amedrano
 *
 */
public class DummySpaceManager implements SpaceManager {

	/** {@inheritDoc} */
	public void loadConfigurations(Dictionary configurations) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	/** {@inheritDoc} */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public PeerCard getMyPeerCard() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public Set<SpaceCard> getSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public SpaceDescriptor getSpaceDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public Map<String, SpaceDescriptor> getManagedSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public void join(SpaceCard spaceCard) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public void leaveSpace(SpaceDescriptor spaceDescriptor) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public Map<String, PeerCard> getPeers() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public MatchingResult getMatchingPeers(Map<String, Serializable> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public Map<String, Serializable> getPeerAttributes(List<String> attributes, PeerCard target) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public void addSpaceListener(SpaceListener listener) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	public void removeSpaceListener(SpaceListener listener) {
		// TODO Auto-generated method stub

	}

}
