package org.universAAL.ri.servicegateway.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.servicegateway.osgi.Activator;

/**
 * 
 * @author eandgrg
 * 
 */
public class ArtifactIntegrationTest extends IntegrationTest {

    public final void testComposite() {
	LogUtils.logInfo(Activator.getModuleContext(),
		ArtifactIntegrationTest.class, "testComposite",
		new String[] { "Test 1 - log all bundles" }, null);
	logAllBundles();

    }

    public final void testServletURLs() {

	LogUtils.logInfo(Activator.getModuleContext(),
		ArtifactIntegrationTest.class, "testServletURLs",
		new String[] { "Test 2 - testServletURLs" }, null);

	GatewayPortTestImpl gp = new GatewayPortTestImpl();

	assertEquals("/universAALTest", gp.url());
	assertEquals("/notExistingDir", gp.dataDir());

    }

    public final void testDoPost() throws IOException, ServletException {

	GatewayPortTestImpl servlet = new GatewayPortTestImpl();

	LogUtils.logInfo(Activator.getModuleContext(),
		ArtifactIntegrationTest.class, "testDoPost",
		new String[] { "Test 3 - testDoPost" }, null);

	HttpServletRequest request = EasyMock
		.createMock(HttpServletRequest.class);
	HttpServletResponse response = EasyMock
		.createMock(HttpServletResponse.class);

	// String[] headerNames = {"Authorization", "auth"};
	// String[] headerValues = {"someEncodedUsername:someEncodedPassword",
	// "Basic amFjazphYQ=="};
	// Map<String, List<String>> inmap = new HashMap<String,
	// List<String>>();
	// for (int i = 0; i < headerNames.length; i++) {
	// inmap.put(headerNames[i], Arrays.asList(headerValues[i]));
	// }
	//	
	// EasyMock.expect(request.getHeaderNames()).andReturn(Collections.enumeration(inmap.keySet()));

	EasyMock.expect(request.getHeader("Authorization")).andReturn(
		"someEncodedUsername:someEncodedPassword").anyTimes();

	// call EasyMock.replay(mock) before calling the method under test.
	// After calling the method under test you can call
	// EasyMock.verify(mock) to verify the mock is called.
	EasyMock.replay(request);
	EasyMock.replay(response);

	// call method under test after replay
	servlet.doPost(request, response);

	assertNull(servlet
		.getUserAndPass(" "));
	assertEquals("someEncodedUsername:someEncodedPassword", request
		.getHeader("Authorization"));

	// check if mock received all the calls we expected
	EasyMock.verify(request);
	EasyMock.verify(response);

    }

    public final void testDoGet() throws IOException, ServletException {

	GatewayPortTestImpl servlet = new GatewayPortTestImpl();

	LogUtils.logInfo(Activator.getModuleContext(),
		ArtifactIntegrationTest.class, "testDoGet",
		new String[] { "Test 4- testDoGet" }, null);

	HttpServletRequest request = EasyMock
		.createMock(HttpServletRequest.class);
	HttpServletResponse response = EasyMock
		.createMock(HttpServletResponse.class);

	EasyMock.expect(request.getHeader("Authorization")).andReturn(
		"someEncodedUsername:someEncodedPassword").anyTimes();

	// call EasyMock.replay(mock) before calling the method under test.
	// After calling the method under test you can call
	// EasyMock.verify(mock) to verify the mock is called.
	EasyMock.replay(request);
	EasyMock.replay(response);

	// call method under test after replay
	servlet.doGet(request, response);

	// check if mock received all the calls we expected
	EasyMock.verify(request);
	EasyMock.verify(response);

    }

}
