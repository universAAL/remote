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
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;

/**
 * @author amedrano
 *
 */
public class DummyAALSPaceManager implements AALSpaceManager {

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
    public Set<AALSpaceCard> getAALSpaces() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public AALSpaceDescriptor getAALSpaceDescriptor() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public Map<String, AALSpaceDescriptor> getManagedAALSpaces() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public void join(AALSpaceCard spaceCard) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void leaveAALSpace(AALSpaceDescriptor spaceDescriptor) {
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
    public Map<String, Serializable> getPeerAttributes(List<String> attributes,
	    PeerCard target) {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public void addAALSpaceListener(AALSpaceListener listener) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void removeAALSpaceListener(AALSpaceListener listener) {
	// TODO Auto-generated method stub

    }

}
