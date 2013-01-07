package org.universAAL.rinterop.profile.ws;

import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.buslifecycle.CXFBusLifeCycleManager;
import org.apache.cxf.endpoint.EndpointResolverRegistry;
import org.apache.cxf.endpoint.EndpointResolverRegistryImpl;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.endpoint.ServerRegistryImpl;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.headers.HeaderManager;
import org.apache.cxf.headers.HeaderManagerImpl;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.phase.PhaseManagerImpl;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.http.QueryHandlerRegistryImpl;
//import org.apache.cxf.transport.http.WSDLQueryHandler;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.workqueue.WorkQueueManagerImpl;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.universAAL.rinterop.profile.agent.osgi.Activator;

public class SoapExportServlet extends CXFNonSpringServlet {

  private static final long serialVersionUID = 1L;
  
  private final Class<?> serviceClass;
  private final Object service;
  
  public SoapExportServlet(Class<?> serviceClass, Object service) {
    super();
    this.serviceClass = serviceClass;
    this.service = service;
  }

  @Override
  public void loadBus(ServletConfig config) /*throws ServletException*/ {
    super.loadBus(config);

    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(service.getClass().getClassLoader());
      
      Bus bus = getBus();
      setExtensions(bus);
      
      BusFactory.setDefaultBus(bus);
      bus.getInInterceptors().add(new InInterceptor());
      
      bus.getOutInterceptors().add(new OutInterceptor()); 
       
      SoapBindingFactory soapFactory = new SoapBindingFactory();
      soapFactory.setBus(bus);
      
      BindingFactoryManager mgr = bus.getExtension(BindingFactoryManager.class);
      mgr.registerBindingFactory(SoapBindingFactory.SOAP_11_BINDING, soapFactory);
      
      createServer(bus);
      
    } catch (BusException ex) {
      Activator.log("CXF Bus Exception", ex);
      try {
		throw new ServletException(ex);
	} catch (ServletException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }
  
  private void createServer(Bus bus) {
    ServerFactoryBean factory = new JaxWsServerFactoryBean();
    
    factory.setBus(bus);
    factory.setServiceClass(serviceClass);
    factory.setServiceBean(service);
    factory.getServiceFactory().setDataBinding(new JAXBDataBinding());
    factory.setAddress("/");
    factory.create();
    
    factory.getServer().getEndpoint().getService().setExecutor(new AALuisExecutor());    
  }

  private static void setExtensions(Bus bus) throws BusException {
    bus.setExtension(new WorkQueueManagerImpl(), WorkQueueManager.class);
    bus.setExtension(new CXFBusLifeCycleManager(), BusLifeCycleManager.class);
    bus.setExtension(new ServerRegistryImpl(), ServerRegistry.class);
    
//    QueryHandlerRegistry qhr = new QueryHandlerRegistryImpl(bus,
//      Arrays.asList(new QueryHandler[]{new WSDLQueryHandler(bus)}));
//    bus.setExtension(qhr, QueryHandlerRegistry.class);
    bus.setExtension(new EndpointResolverRegistryImpl(), EndpointResolverRegistry.class);
    bus.setExtension(new HeaderManagerImpl(), HeaderManager.class);

    bus.setExtension(new WSDLManagerImpl(), WSDLManager.class);
    bus.setExtension(new PhaseManagerImpl(), PhaseManager.class);
    
    bus.setExtension(new SoapTransportFactory(), DestinationFactory.class);
  }

}
