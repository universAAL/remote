package org.universAAL.ri.gateway.communicator.service.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;

public class CommunicatorStarterTest {

    @Test
    public void test() {
	CommunicatorStarter.aliases.add(GatewayCommunicator.ALIAS_PREFIX);
	Assert.assertEquals(1, CommunicatorStarter.aliases.size());
	CommunicatorStarter.aliases.remove(GatewayCommunicator.ALIAS_PREFIX);
	Assert.assertEquals(0, CommunicatorStarter.aliases.size());
    }
}
