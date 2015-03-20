package org.universAAL.ri.gateway.configuration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.service.owl.ServiceBusOntology;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.ontology.location.LocationOntology;
import org.universAAL.ontology.sysinfo.SysinfoOntology;
import org.universAAL.ontology.sysinfo.SystemInfo;
import org.universAAL.ri.gateway.DummyAALSPaceManager;
import org.universAAL.ri.gateway.DummyBusRegistry;
import org.universAAL.ri.gateway.DummyTenantManager;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.operations.OperationChain.OperationResult;

public class TurtleFileSecurityDefinitionTest {
	
	private static ModuleContext mc;
	
	@BeforeClass
	public static void setUp() throws Exception{
		mc = new JUnitModuleContext();
		mc.getContainer().shareObject(mc,
				new TurtleSerializer(),
				new Object[] { MessageContentSerializer.class.getName() });
		
		mc.getContainer().shareObject(mc,
				new DummyAALSPaceManager(),
				new Object[] { AALSpaceManager.class.getName() });
		
		mc.getContainer().shareObject(mc,
				new DummyTenantManager(),
				new Object[] { TenantManager.class.getName() });
		
		org.universAAL.middleware.tracker.impl.Activator.fetchParams = new Object[] { IBusMemberRegistry.class
				.getName() };
		
		mc.getContainer().shareObject(mc,
				new DummyBusRegistry(),
				IBusMemberRegistry.busRegistryShareParams);

		OntologyManagement.getInstance().register(mc, new DataRepOntology());
		OntologyManagement.getInstance().register(mc, new ServiceBusOntology());
//    	OntologyManagement.getInstance().register(mc, new UIBusOntology());
        OntologyManagement.getInstance().register(mc, new LocationOntology());
		OntologyManagement.getInstance().register(mc, new SysinfoOntology());
//        OntologyManagement.getInstance().register(mc, new ShapeOntology());
//        OntologyManagement.getInstance().register(mc, new PhThingOntology());
//        OntologyManagement.getInstance().register(mc, new SpaceOntology());
//        OntologyManagement.getInstance().register(mc, new VCardOntology());
//    	OntologyManagement.getInstance().register(mc, new ProfileOntology());
//		OntologyManagement.getInstance().register(mc, new MenuProfileOntology());
		
		new Gateway().start(mc);
	}
	
    @Test
    public void test1() {
    	
    	URL f = this.getClass().getResource("/AcceptALL.ttl");
    	assertNotNull(f);
		TurtleFileSecurityDefinition tfsd = new TurtleFileSecurityDefinition(f);
    }

    @Test
    public void test2() {
    	
    	URL f = this.getClass().getResource("/AcceptALL.ttl");
    	assertNotNull(f);
		TurtleFileSecurityDefinition tfsd = new TurtleFileSecurityDefinition(f);
		assertNotNull(tfsd.getExportOperationChain());
		assertNotNull(tfsd.getExportOperationChain().check(new Resource[]{}));
		assertTrue(tfsd.getExportOperationChain().check(new Resource[]{}).equals(OperationResult.ALLOW));
    }
    
    @Test
    public void test3() {
    	
    	URL f = this.getClass().getResource("/AcceptALL.ttl");
    	assertNotNull(f);
		TurtleFileSecurityDefinition tfsd = new TurtleFileSecurityDefinition(f);
		
		ContextEventPattern cep = new ContextEventPattern();
		cep.addRestriction(MergedRestriction.getAllValuesRestriction(
			ContextEvent.PROP_RDF_SUBJECT, SystemInfo.MY_URI));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(
			ContextEvent.PROP_RDF_PREDICATE, SystemInfo.PROP_ALIVE));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(
			ContextEvent.PROP_RDF_OBJECT, new Boolean(true)));
		
		
		assertTrue(tfsd.getExportOperationChain().check(new Resource[]{}).equals(OperationResult.ALLOW));
    }
    
    @Test
    public void test4() {
    	
    	URL f = this.getClass().getResource("/DenyALL.ttl");
    	assertNotNull(f);
		TurtleFileSecurityDefinition tfsd = new TurtleFileSecurityDefinition(f);
		assertNotNull(tfsd.getExportOperationChain());
		assertNotNull(tfsd.getExportOperationChain().check(new Resource[]{}));
		assertTrue(tfsd.getExportOperationChain().check(new Resource[]{}).equals(OperationResult.DENY));
    }
    
    @Test
    public void test5() {
    	
    	URL f = this.getClass().getResource("/DenyALL.ttl");
    	assertNotNull(f);
		TurtleFileSecurityDefinition tfsd = new TurtleFileSecurityDefinition(f);
		
		ContextEventPattern cep = new ContextEventPattern();
		cep.addRestriction(MergedRestriction.getAllValuesRestriction(
			ContextEvent.PROP_RDF_SUBJECT, SystemInfo.MY_URI));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(
			ContextEvent.PROP_RDF_PREDICATE, SystemInfo.PROP_ALIVE));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(
			ContextEvent.PROP_RDF_OBJECT, new Boolean(true)));
		
		
		assertTrue(tfsd.getExportOperationChain().check(new Resource[]{}).equals(OperationResult.DENY));
    }
}
