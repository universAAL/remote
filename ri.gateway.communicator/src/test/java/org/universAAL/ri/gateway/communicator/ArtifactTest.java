package org.universAAL.ri.gateway.communicator;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.ResponseCallback;
import org.universAAL.ri.gateway.communicator.service.impl.CommunicatorStarter;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.ExportManager;
import org.universAAL.ri.gateway.eimanager.ImportManager;

/**
 * Unit tests for AALSpace Gateway Communicator.
 * 
 * @author skallz
 * 
 */
public class ArtifactTest extends IntegrationTest {

    /**
     * HTTP port.
     */
    private static final int HTTP_PORT = 8182;
    /**
     * Timeout that should not elapse before tested invocations. Used no to let
     * the test hang.
     */
    private static final int LONG_ENOUGH_TIMEOUT = 2000;
    /**
     * How many spurious wake-ups of the testing thread are allowed. It may be
     * an exaggeration ;)
     */
    private static final int SPURIOUS_WAKEUPS_ALLOW = 3;

    /**
     * test alias/id 1.
     */
    private String test1 = "test1";
    /**
     * test alias/id 2.
     */
    private String test2 = "test2";

    /**
     * starter 1.
     */
    private CommunicatorStarter inst1;
    /**
     * starter 2.
     */
    private CommunicatorStarter inst2;

    /**
     * communicator 1.
     */
    private GatewayCommunicator comm1;
    /**
     * communicator 2.
     */
    private GatewayCommunicator comm2;

    /**
     * communicator 1's url.
     */
    private URL url1;
    /**
     * communicator 2's url.
     */
    private URL url2;

    /**
     * mock manager of space 1.
     */
    private ImportManager import1;
    /**
     * mock manager of space 2.
     */
    private ImportManager import2;

    /**
     * mock manager of space 1.
     */
    private ExportManager export1;
    /**
     * mock manager of space 2.
     */
    private ExportManager export2;

    @Override
    public void onSetUp() {
	log("====== setUp =======");
	if (true)
		return;
	// starting two instances of GatewatCommunicator
//	log("starting two gateway communicators");
//	import1 = createMock(ImportManager.class);
//	import2 = createMock(ImportManager.class);
//
//	export1 = createMock(ExportManager.class);
//	export2 = createMock(ExportManager.class);
//
//	System.setProperty(GatewayCommunicator.REMOTE_GATEWAYS_PROP,
//		"localhost:" + HTTP_PORT + ":" + "test1");
//	try {
//	    inst1 = new CommunicatorStarter(bundleContext, test1);
//	
//	    inst1.setManagers(import1, export1);
//	} catch (Exception e1) {
//	    // TODO Auto-generated catch block
//	    e1.printStackTrace();
//	}
//	System.setProperty(GatewayCommunicator.REMOTE_GATEWAYS_PROP,
//		"localhost:" + HTTP_PORT + ":" + "test2");
//	try {
//	    inst2 = new CommunicatorStarter(bundleContext, test2);
//	
//	    inst2.setManagers(import2, export2);
//	} catch (Exception e1) {
//	    // TODO Auto-generated catch block
//	    e1.printStackTrace();
//	}
//	// getting the two OSGi services
//	log("getting OSGi references of the two gateway communicators");
//	comm1 = inst1.getCommunicator();
//	comm2 = inst2.getCommunicator();
//
//	try {
//	    url1 = new URL(String.format("http://localhost:%d/%s-%s",
//		    HTTP_PORT, GatewayCommunicator.ALIAS_PREFIX, test1));
//	    url2 = new URL(String.format("http://localhost:%d/%s-%s",
//		    HTTP_PORT, GatewayCommunicator.ALIAS_PREFIX, test2));
//	} catch (MalformedURLException e) {
//	    assertTrue("Malformed test URLs", false);
//	}
//
//	log("====== start =======");
    }

    @Override
    protected void onTearDown() throws Exception {
	log("====== stop =======");
	if (true)
		return;
	inst1.stop();
	inst2.stop();
    }

    /**
     * Test for context bus.
     * @throws IOException 
     */
    public void atestContext() throws IOException {
	String sourceId = "";
	ContextEvent context1 = new ContextEvent(
		"urn:org.universAAL.middleware.context.rdf:ContextEvent#ctx1");
	ContextEvent context2 = new ContextEvent(
		"urn:org.universAAL.middleware.context.rdf:ContextEvent#ctx2");

	// context 1 -> 2
	import2.sendContextEvent(sourceId, context1);
	replay(import2);
	log("sending context message: comm1 -> comm2");
	comm1.sendContextEvent(Serializer.Instance.marshallObject(context1),
		new URL[] { url2 });
	verify(import2);

	// context 2 -> 1
	import1.sendContextEvent(sourceId, context2);
	replay(import1);

	log("sending context message: comm2 -> comm1");
	comm2.sendContextEvent(Serializer.Instance.marshallObject(context2),
		new URL[] { url1 });
	verify(import2);
    }

    /**
     * Test for service bus.
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void atestService() throws IOException, ClassNotFoundException {
	String sourceId1 = "";
	String sourceId2 = "";
	ServiceResponse realRes1;
	ServiceResponse realRes2;

	ServiceRequest req1 = new ServiceRequest();
	ServiceResponse res1 = new ServiceResponse();

	ServiceRequest req2 = new ServiceRequest();
	ServiceResponse res2 = new ServiceResponse();

	Message m;

	// service request 1 -> 2
	expect(export2.sendServiceRequest(sourceId1, req1)).andReturn(res1);
	replay(export2);
	log("sending service request: comm1 -> comm2");

	m = comm1.sendServiceRequest(Serializer.Instance.marshallObject(req1),
		new URL[] { url2 })[0];
	realRes1 = Serializer.Instance.unmarshallObject(ServiceResponse.class,
		m);

	verify(export2);
	assertEquals(res1, realRes1);
	// it cannot be the same object, as it was serialized and unserialized
	assertFalse(res1 == realRes1);

	// service request 2 -> 1
	expect(export1.sendServiceRequest(sourceId2, req2)).andReturn(res2);
	replay(export1);
	log("sending service request: comm2 -> comm1");

	m = comm2.sendServiceRequest(Serializer.Instance.marshallObject(req2),
		new URL[] { url1 })[0];
	realRes2 = Serializer.Instance.unmarshallObject(ServiceResponse.class,
		m);

	verify(export1);
	assertEquals(res2, realRes2);
	// it cannot be the same object, as it was serialized and unserialized
	assertFalse(res2 == realRes2);
    }

    /**
     * Test for asynchronous communication for service bus.
     * @throws IOException 
     */
    public void atestServiceAsync() throws IOException {
	String sourceId = "";

	ServiceRequest req = new ServiceRequest();
	final ServiceResponse res = new ServiceResponse();

	// asynchronous service request 1 -> 2
	reset(export2);
	expect(export2.sendServiceRequest(sourceId, req)).andReturn(res);
	replay(export2);
	final AtomicBoolean done = new AtomicBoolean(false);
	final ResponseCallback callback = new ResponseCallback() {
	    public synchronized void collectResponse(final Message response) throws IOException {
		this.notifyAll();
		assertEquals(response, Serializer.Instance.marshallObject(res));
		done.set(true);
	    }
	};
	log("sending asynchronous service request: comm1 -> comm2");
	comm1.sendServiceRequestAsync(Serializer.Instance.marshallObject(req),
		url1, url2, callback);
	synchronized (callback) {
	    // we let some spurious wake-ups just in case
	    for (int i = 0; i < SPURIOUS_WAKEUPS_ALLOW; i++) {
		try {
		    callback.wait(LONG_ENOUGH_TIMEOUT);
		    break;
		} catch (InterruptedException e) {
		    continue;
		}
	    }
	}
	verify(export2);
	assertTrue(done.get());
    }

    /**
     * Test for timed communication for service bus.
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void atestServiceTimed() throws IOException, ClassNotFoundException {
	Message m;
	String sourceId = "";
	ServiceResponse realResponse;

	ServiceRequest request0 = new ServiceRequest();

	ServiceRequest request1 = new ServiceRequest();
	ServiceResponse response1 = new ServiceResponse();

	ServiceRequest request2 = new ServiceRequest();
	ServiceResponse response2 = new ServiceResponse();

	// service request 1 -> 2
	log("sending service request with timeout 1: comm1 -> comm2");
	try {
	    comm1.sendServiceRequest(Serializer.Instance
		    .marshallObject(request0), new URL[] { url2 }, 1);
	    // assertTrue("an exception was expected", false);
	} catch (TimeoutException ex) {
	    assertTrue(true);
	}

	// service request 2 -> 1
	expect(export1.sendServiceRequest(sourceId, request1)).andReturn(
		response1);
	replay(export1);
	log("sending service request with timeout 2000: comm2 -> comm1");
	try {
	    m = comm2.sendServiceRequest(Serializer.Instance
		    .marshallObject(request1), new URL[] { url1 },
		    LONG_ENOUGH_TIMEOUT)[0];
	    realResponse = Serializer.Instance.unmarshallObject(
		    ServiceResponse.class, m);
	    verify(export1);
	    assertEquals(response1, realResponse);
	    // it cannot be the same object, as it was serialized and
	    // unserialized
	    assertFalse(response1 == realResponse);
	} catch (TimeoutException e) {
	    assertTrue(false);
	}

	// service request 2 -> 1
	reset(export1);
	expect(export1.sendServiceRequest(sourceId, request2)).andReturn(
		response2);
	replay(export1);
	log("sending service request with timeout 0: comm2 -> comm1");
	try {
	    m = comm2.sendServiceRequest(Serializer.Instance
		    .marshallObject(request2), new URL[] { url1 }, 0)[0];
	    realResponse = Serializer.Instance.unmarshallObject(
		    ServiceResponse.class, m);
	    verify(export1);
	    assertEquals(response2, realResponse);
	    // it cannot be the same object, as it was serialized and
	    // unserialized
	    assertFalse(response2 == realResponse);
	} catch (TimeoutException e) {
	    assertTrue(false);
	}
    }

    /**
     * An aggregation of all above tests.
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    public void testAll() throws IOException, ClassNotFoundException {
/*	testServiceTimed();
	reset(export1, export2, import1, import2);
	testServiceAsync();
	reset(export1, export2, import1, import2);
	testService();
	reset(export1, export2, import1, import2);
	testContext();
	reset(export1, export2, import1, import2);

	testServiceTimed();
	reset(export1, export2, import1, import2);
	testServiceAsync();
	reset(export1, export2, import1, import2);
	testService();
	reset(export1, export2, import1, import2);
	testContext();
	reset(export1, export2, import1, import2);*/
    }

    // /**
    // * looks up the OSGi service with given id as a registration property.
    // *
    // * @param id
    // * id
    // * @return the service reference
    // */
    // private GatewayCommunicator getCommunicator(final String id) {
    //
    // GatewayCommunicator communicator = null;
    //
    // String clazz = GatewayCommunicator.class.getName();
    // Filter filter = null;
    // try {
    // filter = bundleContext.createFilter(String.format(
    // "(&(%s=%s)(%s=%s))", Constants.OBJECTCLASS, clazz,
    // GatewayCommunicator.OSGI_ID_PROPERTY, id));
    // } catch (InvalidSyntaxException e1) {
    // assertTrue(false);
    // }
    // assertNotNull("filter syntax wrong", filter);
    //
    // // waiting for the service
    // ServiceTracker t = new ServiceTracker(bundleContext, filter, null);
    // t.open();
    // while (true) {
    // try {
    // communicator = (GatewayCommunicator) t
    // .waitForService(WAIT_FOR_SERVICE);
    // break;
    // } catch (InterruptedException e) {
    // continue;
    // }
    // }
    //
    // assertNotNull("failed to get OSGi service id: " + id, communicator);
    // return communicator;
    // }

}
