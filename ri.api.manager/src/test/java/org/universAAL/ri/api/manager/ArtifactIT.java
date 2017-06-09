package org.universAAL.ri.api.manager;

import junit.framework.Assert;

import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.ri.api.manager.exceptions.APIImplException;

public class ArtifactIT extends IntegrationTest {

	private static final String ID = "testid";
	private static final String REMOTE = "http://localhost:8080/client";
	private static final String CEVENT = "@prefix ph: <http://ontology.universaal.org/PhThing.owl#> ."
			+ " @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
			+ " @prefix tst: <http://ontology.igd.fhg.de/LightingServer.owl#> ."
			+ " @prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
			+ " @prefix light: <http://ontology.universaal.org/Lighting.owl#> ."
			+ " @prefix ctxt: <http://ontology.universAAL.org/Context.owl#> ."
			+ " @prefix owl: <http://www.w3.org/2002/07/owl#> ."
			+ " <urn:org.universAAL.middleware.context.rdf:ContextEvent#_:c043cf9e99c148f8:be9> ctxt:hasProvider tst:LightServer ;"
			+ " a ctxt:ContextEvent ;" + " rdf:subject tst:controlledLamp1 ;"
			+ " ctxt:hasTimestamp \"1384864157846\"^^xsd:long ;" + " rdf:predicate light:srcBrightness ;"
			+ " rdf:object \"100\"^^xsd:int ."
			+ " tst:controlledLamp1 a light:LightSource , ph:Device , ph:PhysicalThing ;"
			+ " light:srcBrightness \"100\"^^xsd:int ." + " ctxt:gauge a ctxt:ContextProviderType ."
			+ " tst:LightServer a ctxt:ContextProvider ;" + " ctxt:hasType ctxt:gauge ;"
			+ " ctxt:myClassesOfEvents [ a ctxt:ContextEventPattern ;"
			+ " <http://www.w3.org/2000/01/rdf-schema#subClassOf> [ a owl:Restriction ;"
			+ " owl:allValuesFrom light:LightSource ;" + " owl:onProperty rdf:subject ] ] .";
	private static final String CPATTERN = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
			+ " @prefix ns: <http://ontology.universaal.org/Lighting.owl#> ."
			+ " @prefix : <http://www.w3.org/2002/07/owl#> ."
			+ " _:BN000000 a <http://ontology.universAAL.org/Context.owl#ContextEventPattern> ;"
			+ " <http://www.w3.org/2000/01/rdf-schema#subClassOf> [ a :Restriction ;"
			+ " :allValuesFrom ns:LightSource ;" + " :onProperty rdf:subject ] , [ :hasValue ns:srcBrightness ;"
			+ " a :Restriction ;" + " :onProperty rdf:predicate ] , [ a :Restriction ;"
			+ " :allValuesFrom <http://www.w3.org/2001/XMLSchema#int> ;" + " :onProperty rdf:object ] ."
			+ " ns:LightSource a :Class .";
	private static final String SPROFILE = "@prefix ns: <http://www.daml.org/services/owl-s/1.1/Profile.owl#> ."
			+ " @prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> ."
			+ " @prefix owl: <http://www.w3.org/2002/07/owl#> ."
			+ " @prefix ns1: <http://ontology.igd.fhg.de/LightingServer.owl#> ."
			+ " @prefix ns2: <http://www.daml.org/services/owl-s/1.1/Service.owl#> ."
			+ " @prefix ns3: <http://ontology.universaal.org/Lighting.owl#> ."
			+ " @prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
			+ " @prefix psn: <http://ontology.universAAL.org/Service.owl#> ."
			+ " @prefix : <http://www.daml.org/services/owl-s/1.1/Process.owl#> ."
			+ " _:BN000000 ns2:presentedBy ns1:turnOn ;" + " a ns:Profile ;" + " ns:has_process ns1:turnOnProcess ;"
			+ " ns:hasResult [ a :Result ;" + " :hasEffect [ psn:affectedProperty [ a psn:PropertyPath ;"
			+ " psn:thePath ( ns3:controls ns3:srcBrightness ) ] ;" + " a psn:ChangeEffect ;"
			+ " psn:propertyValue \"100\"^^xsd:int ] ] ;" + " ns:hasInput ns1:lampURI ."
			+ " ns1:turnOn a ns1:LightingService , ns3:Lighting ;"
			+ " pvn:instanceLevelRestrictions [ owl:hasValue [ :fromProcess :ThisPerform ;" + " a :ValueOf ;"
			+ " :theVar ns1:lampURI ] ;" + " a owl:Restriction ;" + " owl:onProperty ns3:controls ] ;"
			+ " ns2:presents _:BN000000 ;" + " pvn:numberOfValueRestrictions \"1\"^^xsd:int ."
			+ " ns1:lampURI psn:parameterCardinality \"1\"^^xsd:int ;" + " a :Input ;"
			+ " :parameterType \"http://ontology.universaal.org/Lighting.owl#LightSource\"^^xsd:anyURI ."
			+ " :ThisPerform a :Perform .";
	private static final String SREQUEST = "@prefix owl: <http://www.w3.org/2002/07/owl#> ."
			+ " @prefix ns: <http://ontology.universaal.org/PhThing.owl#> ."
			+ " @prefix ns1: <http://ontology.igd.fhg.de/LightingServer.owl#> ."
			+ " @prefix ns2: <http://ontology.universaal.org/Lighting.owl#> ."
			+ " @prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> ."
			+ " @prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
			+ " @prefix ns3: <http://www.daml.org/services/owl-s/1.1/Process.owl#> ."
			+ " @prefix : <http://ontology.universAAL.org/Service.owl#> ." + " _:BN000000 a pvn:ServiceRequest ;"
			+ " pvn:requiredResult [ a ns3:Result ;" + " ns3:hasEffect [ :affectedProperty [ a :PropertyPath ;"
			+ " :thePath ( ns2:controls ns2:srcBrightness ) ] ;" + " a :ChangeEffect ;"
			+ " :propertyValue \"100\"^^xsd:int ] ] ;" + " pvn:requestedService [ a ns2:Lighting ;"
			+ " pvn:instanceLevelRestrictions [ owl:hasValue <http://ontology.igd.fhg.de/LightingServer.owl#controlledLamp0> ;"
			+ " a owl:Restriction ;" + " owl:onProperty ns2:controls ] ;"
			+ " pvn:numberOfValueRestrictions \"1\"^^xsd:int ] ."
			+ " <http://ontology.igd.fhg.de/LightingServer.owl#controlledLamp0> a ns2:LightSource , ns:Device , ns:PhysicalThing .";

	/**
	 * Test 1: Check all artifacts in the log
	 */
	public void test0Composite() {
		logAllBundles();
	}

	/**
	 * Test 2: Check UN/REGISTER
	 */
	public void test1REGISTER() {
		boolean testFlag = false;
		RemoteAPIImpl api = Activator.getRemoteAPI();
		// send c and fail
		try {
			api.sendC(ID, CEVENT);
		} catch (APIImplException e) {
			testFlag = true;
		}
		Assert.assertTrue("Accepted call to API before REGISTER", testFlag);
		testFlag = false;
		// register
		try {
			api.register(ID, REMOTE);
		} catch (Exception e) {
			Assert.fail("Failed REGISTER " + e.toString());
		}
		// send c and correct
		try {
			api.sendC(ID, CEVENT);
		} catch (APIImplException e) {
			Assert.fail("Failed SENDC " + e.toString());
		}
		// unregister
		try {
			api.unregister(ID);
		} catch (APIImplException e) {
			Assert.fail("Failed UNREGISTER " + e.toString());
		}
		// send c and fail
		try {
			api.sendC(ID, CEVENT);
		} catch (APIImplException e) {
			testFlag = true;
		}
		Assert.assertTrue("Accepted call to API after UNREGISTER", testFlag);
		testFlag = false;
		// register (for next tests)
		try {
			api.register(ID, REMOTE);
		} catch (Exception e) {
			Assert.fail("Failed REGISTER " + e.toString());
		}
		// }

		/**
		 * Test 3: Check SENDC/SUBSCRIBEC
		 */
		// public void test2CONTEXT(){
		// RemoteAPIImpl api = Activator.getRemoteAPI();
		// subscribe c
		try {
			api.subscribeC(ID, CPATTERN);
		} catch (APIImplException e) {
			Assert.fail("Failed SUBSCRIBEC " + e.toString());
		}
		// send c and receive c correctly
		try {
			api.sendC(ID, CEVENT);
		} catch (APIImplException e) {
			Assert.fail("Failed SENDC " + e.toString());
		}
		// }

		/**
		 * Test 4: Check CALLS/PROVIDES
		 */
		// public void test3SERVICE() {
		// RemoteAPIImpl api = Activator.getRemoteAPI();
		// provide s
		try {
			api.provideS(ID, SPROFILE);
		} catch (APIImplException e) {
			Assert.fail("Failed PROVIDES " + e.toString());
		}
		// call s
		String resp = null;
		try {
			resp = api.callS(ID, SREQUEST);
		} catch (APIImplException e) {
			Assert.fail("Failed CALLS " + e.toString());
		}
		// get resp
		Assert.assertNotNull("Received response is null", resp);
		if (resp != null) {
			int index = resp.indexOf(RemoteAPI.KEY_STATUS);
			Assert.assertTrue("Received response does not contain STATUS", index > -1);
			if (index > -1) {
				Assert.assertTrue("Received response is not serviceSpecificFailure (because of network error)",
						resp.substring(index).trim()
								.startsWith(RemoteAPI.KEY_STATUS + "=" + CallStatus.serviceSpecificFailure.toString()));
			}
		}
	}

	// Test Others: Android GCM? Persistence? HTTP? How?
}
