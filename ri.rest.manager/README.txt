==========FOR RUNTIME DEPENDENCIES==========

For running ri.rest.manager you will need Apache CXF JAXRS implementation.
Most recent version can be installed in Karaf with these steps:

1-Run Karaf version 3.0.1 to 3.0.3
The latest version of CXF requires osgi packages version > 1.6.0, so Karaf 2.X does not work
Also, binaries of Karaf 3.X versions outside that range are corrupted in Karaf download page

2-In Karaf, install the CXF feature repo (check if there are newer versions than 3.0.2)
feature:repo-add http://repo1.maven.org/maven2/org/apache/cxf/karaf/apache-cxf/3.0.2/apache-cxf-3.0.2-features.xml

3-Install the feature cxf-jaxrs
feature:install cxf-jaxrs

4-Install the bundle cxf-rt-transports-http-jetty-3.0.2
It may not be reachable in Maven central repo, so you either need to install locally or deploy the jar

5-Restart Karaf
The JAXRS feature is susceptible to race conditions and may miss some classes when following these instructions
Restarting Karaf before installing the rest manager ensures that everything is in place

6-Install the REST manager
Either deploy or install. Step 5 should prevent "Cannot find any registered HttpDestinationFactory from the Bus" start errors



==========THE (INTENDED) REST API==========

http://www.tsb.es/uaal										GET													->	<spaces>
http://www.tsb.es/uaal/spaces								GET, POST <123>										->	<123,234,345,...>
http://www.tsb.es/uaal/spaces/123							GET, PUT/DELETE <123>								->	<context,service>
http://www.tsb.es/uaal/spaces/123/context					GET													->	<publishers,subscribers>
http://www.tsb.es/uaal/spaces/123/context/publishers		GET, POST <456>										->	<456,567,678,...>
http://www.tsb.es/uaal/spaces/123/context/publishers/456	GET, PUT/DELETE <456>, POST <ContextEvent>			->	<Id,ProviderInfo>
http://www.tsb.es/uaal/spaces/123/context/subscribers		GET, POST <789>										->	<789,890,901,...>
http://www.tsb.es/uaal/spaces/123/context/subscribers/789	GET, PUT/DELETE <789>								->	<Id,Url,ContextEventPattern>
http://www.tsb.es/uaal/spaces/123/service					GET													->	<callers,callees>
http://www.tsb.es/uaal/spaces/123/service/callers			GET, POST <654>										->	<654,543,432,...>
http://www.tsb.es/uaal/spaces/123/service/callers/654		GET, PUT/DELETE <654>, POST <ServiceRequest>		->	<Id>, <ServiceResponse>
http://www.tsb.es/uaal/spaces/123/service/callees			GET, POST <987>										->	<987,876,765,...>
http://www.tsb.es/uaal/spaces/123/service/callees/987		GET, PUT/DELETE <987>, POST <ServiceResponse>		->	<Id,Url,ServiceProfile>



==========HOW JAXRS "API" WORKS WITH CXF==========

Each class in HATEOAS package is a "resource" that can build up REST urls.
There is one "root" resource, in this case Uaal, that is registered in the activator.
All other will be resolved and reachable from there.
This is an example class showing all available options for one of these "resources":

@XmlAccessorType(XmlAccessType.NONE)  //Necessary (dont remember why)
@XmlRootElement(name = "resourcenode")  //Allows this class to be de/serializable: POJO <-> HTTP data (e.g. application/json)
@Path("/uaal/resourcelist/{id}")  //url path to this (type of) resource. {Brackets} will be used to fill elements or params
public class Resource {

    @XmlAttribute  //This makes this field be transformed into an attrib (e.g. <... id=""> in xml, "@id:" in json)
    @PathParam("id")  //It will be automatically given the value of {id} from the url
    private String id;
    
    @XmlElement(name = "elem")  //This makes this field be transformed into an element (e.g. <space> in xml, "space:" in json)
    private ArrayList<Elem> elem;
    
    @XmlElement(name = "link")  //This makes this field be transformed into a http hyperlink element, which is special
    @XmlJavaTypeAdapter(JaxbAdapter.class)
    private Link self=Link.fromPath("/uaal/spaces").rel("self").build();

    public ArrayList<Elem> getElem() {  //For each element or attribute there must be get/setters
	return elem;
    }

    public void setElem(ArrayList<Space> elem) {
	this.elem = elem;
    }
    
    ...
    
    public Spaces(){  //Empty default constructor
    }
    
    @GET  //REST GET calls on the url defined in @Path will be answered by this method
    @Produces(Activator.TYPES)  //Produced MIME types, as requested by HTTP Accept.
    public Resource getMyResource(){  //Must return the instance representing itself (the one with the right {id})
    ...
    }
    
    @POST  //REST POST calls on the url defined in @Path will be answered by this method
    @Consumes(Activator.TYPES)  //Accepted body MIME types, as defined in HTTP Content-type
    public Response addElemResource(Elem elem) throws URISyntaxException{  //elem is auto built from body as defined in its class
    ...
	return Response.created(new URI("uaal/resourcelist/{id}/elem.id")).build();  //Return the url to the new elem
    }
    
    @Path("/{elem}")  //@Path here is simply used to redirect to the full url path pattern in the right next resource class
    @Produces(Activator.TYPES)
    public Elem getElemResourceLocator(){
	return new Elem();  //This will deliver the handling to the Elem class
    }
    
    
    
==========POST BODY FORMATS==========

All the "link" elements can be removed because they will be ignored anyway

CALLEE_______________________________
---------------------------------json
{
  "callee": {
    "@id": "6",
    "link": {
      "@href": "/uaal/spaces/23/service/callees/6",
      "@rel": "self"
    },
    "callback": "callbackURLorGCMkey",
    "profile": "\n@prefix owl: <http://www.w3.org/2002/07/owl#> .\n@prefix ns: <http://www.daml.org/services/owl-s/1.1/Profile.owl#> .\n@prefix ns1: <http://www.daml.org/services/owl-s/1.1/Service.owl#> .\n@prefix ns2: <http://ontology.universAAL.org/ProfilingServer.owl#> .\n@prefix ns3: <http://ontology.universAAL.org/Profile.owl#> .\n@prefix : <http://www.daml.org/services/owl-s/1.1/Process.owl#> .\n_:BN000000 ns1:presentedBy ns2:servA ;\n  a ns:Profile ;\n  ns:has_process ns2:servAProcess ;\n  ns:hasResult [\n    :withOutput (\n      [\n        a :OutputBinding ;\n        :toParam ns2:argAo ;\n        :valueForm \"\"\"\n          @prefix : <http://ontology.universAAL.org/Service.owl#> .\n          _:BN000000 a :PropertyPath ;\n            :thePath (\n              <http://ontology.universAAL.org/Profile.owl#controls>\n            ) .\n          \"\"\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>\n      ]\n    ) ;\n    a :Result\n  ] ;\n  ns:hasOutput (\n    ns2:argAo\n  ) .\nns2:argAo a :Output ;\n  :parameterType \"http://ontology.universAAL.org/Profile.owl#User\"^^<http://www.w3.org/2001/XMLSchema#anyURI> .\nns2:servA a ns3:ProfilingService ;\n  <http://ontology.universAAL.org/uAAL.owl#instanceLevelRestrictions> (\n    [\n      a owl:Restriction ;\n      owl:allValuesFrom ns3:User ;\n      owl:onProperty ns3:controls\n    ]\n  ) ;\n  ns1:presents _:BN000000 .\nns3:User a owl:Class .\n"
  }
}
----------------------------------xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<callee id="6">
    <link href="/uaal/spaces/23/service/callees/6" rel="self"/>
    <callback>callbackURLorGCMkey</callback>
    <profile>@prefix owl: &lt;http://www.w3.org/2002/07/owl#&gt; .
@prefix ns: &lt;http://www.daml.org/services/owl-s/1.1/Profile.owl#&gt; .
@prefix ns1: &lt;http://www.daml.org/services/owl-s/1.1/Service.owl#&gt; .
@prefix ns2: &lt;http://ontology.universAAL.org/ProfilingServer.owl#&gt; .
@prefix ns3: &lt;http://ontology.universAAL.org/Profile.owl#&gt; .
@prefix : &lt;http://www.daml.org/services/owl-s/1.1/Process.owl#&gt; .
_:BN000000 ns1:presentedBy ns2:servA ;
  a ns:Profile ;
  ns:has_process ns2:servAProcess ;
  ns:hasResult [
    :withOutput (
      [
        a :OutputBinding ;
        :toParam ns2:argAo ;
        :valueForm &quot;&quot;&quot;
          @prefix : &lt;http://ontology.universAAL.org/Service.owl#&gt; .
          _:BN000000 a :PropertyPath ;
            :thePath (
              &lt;http://ontology.universAAL.org/Profile.owl#controls&gt;
            ) .
          &quot;&quot;&quot;^^&lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral&gt;
      ]
    ) ;
    a :Result
  ] ;
  ns:hasOutput (
    ns2:argAo
  ) .
ns2:argAo a :Output ;
  :parameterType &quot;http://ontology.universAAL.org/Profile.owl#User&quot;^^&lt;http://www.w3.org/2001/XMLSchema#anyURI&gt; .
ns2:servA a ns3:ProfilingService ;
  &lt;http://ontology.universAAL.org/uAAL.owl#instanceLevelRestrictions&gt; (
    [
      a owl:Restriction ;
      owl:allValuesFrom ns3:User ;
      owl:onProperty ns3:controls
    ]
  ) ;
  ns1:presents _:BN000000 .
ns3:User a owl:Class .</profile>
</callee>


CALLER_______________________________
---------------------------------json
{
  "caller": {
    "@id": "6",
    "link": {
      "@href": "/uaal/spaces/23/service/callers/6",
      "@rel": "self"
    }
  }
}
----------------------------------xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<caller id="6">
    <link href="/uaal/spaces/23/service/callers/6" rel="self"/>
</caller>


SUBSCRIBER___________________________
---------------------------------json
{
  "subscriber": {
    "@id": "6",
    "link": {
      "@href": "/uaal/spaces/23/context/subscribers/6",
      "@rel": "self"
    },
    "callback": "callbackURLorGCMkey",
    "pattern": "Java-encoded Serialization of ContextEventPattern. Check CALLEE for example of right serial format."
  }
}
----------------------------------xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<subscriber id="6">
    <link href="/uaal/spaces/23/context/subscribers/6" rel="self"/>
    <callback>callbackURLorGCMkey</callback>
    <pattern>XML-encoded Serialization of ContextEventPattern. Check CALLEE for example of right serial format.</pattern>
</subscriber>


PUBLISHER____________________________
---------------------------------json
{
  "publisher": {
    "@id": "6",
    "link": {
      "@href": "/uaal/spaces/23/context/publishers/6",
      "@rel": "self"
    },
    "providerinfo": "Java-encoded Serialization of ContextProvider. Check CALLEE for example of right serial format."
  } 
}
----------------------------------xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<publisher id="6">
    <link href="/uaal/spaces/23/context/publishers/6" rel="self"/>
    <providerinfo>XML-encoded Serialization of ContextProvider. Check CALLEE for example of right serial format.</providerinfo>
</publisher>


SPACE________________________________
---------------------------------json
{
  "space": {
    "@id": "23",
    "callback": "callbackURLorGCMkey",
    "link": [
      {
        "@href": "/uaal/spaces/23",
        "@rel": "self"
      },
      {
        "@href": "/uaal/spaces/23/context",
        "@rel": "context"
      },
      {
        "@href": "/uaal/spaces/23/service",
        "@rel": "service"
      }
    ]
  }
}
----------------------------------xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<space id="23">
    <link href="/uaal/spaces/23" rel="self"/>
    <link href="/uaal/spaces/23/context" rel="context"/>
    <link href="/uaal/spaces/23/service" rel="service"/>
    <callback>callbackURLorGCMkey</callback>
</space>