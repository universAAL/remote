package org.universAAL.ri.rest.manager;

public class Body {
    private final static String PREFIX = "http://ontology.universAAL.org/Test.owl#";
    public static final String R_SPACE="$%s";
    public static final String R_CALLBACK="$%h";
    public static final String R_CALLER="$%a";
    public static final String R_SUBSCRIBER="$%u";
    public static final String R_ID="$%i";
    public static final String R_TYPE="$%y";

    public static final String CREATE_SPACE="{\r\n"
	    + "   \"space\": {\r\n"
	    + "     \"@id\": \""+R_SPACE+"\",\r\n"
	    + "     \"callback\": \""+R_CALLBACK+"\"\r\n"
	    + "   }\r\n"
	    + " }";
    public static final String CREATE_CALLER="{\r\n"
	    + "  \"caller\": {\r\n"
	    + "    \"@id\": \""+R_CALLER+"\"\r\n"
	    + "  }\r\n"
	    + " }";
    public static final String CREATE_PUBLISHER="{\r\n"
	    + "  \"publisher\": {\r\n"
	    + "    \"@id\": \""+R_ID+"\",\r\n"
	    + "    \"providerinfo\": \""
	    + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\\r\\n"
	    + "@prefix owl: <http://www.w3.org/2002/07/owl#> .\\r\\n"
	    + "@prefix : <http://ontology.universAAL.org/Context.owl#> .\\r\\n"
	    + "<"+PREFIX+"providerOf"+R_ID+"> :myClassesOfEvents (\\r\\n"
	    + "    [\\r\\n"
	    + "      a :ContextEventPattern ;\\r\\n"
	    + "      <http://www.w3.org/2000/01/rdf-schema#subClassOf> [\\r\\n"
	    + "          owl:hasValue <"+PREFIX+R_ID+"> ;\\r\\n"
	    + "          a owl:Restriction ;\\r\\n"
	    + "          owl:onProperty rdf:subject\\r\\n"
	    + "        ]\\r\\n"
	    + "    ]\\r\\n"
	    + "  ) ;\\r\\n"
	    + "  a :ContextProvider ;\\r\\n"
	    + "  :hasType :controller .\\r\\n"
	    + ":controller a :ContextProviderType .\\r\\n"
	    + "<"+PREFIX+R_ID+"> a <http://ontology.universaal.org/PhThing.owl#Device>,"
	    + " <http://ontology.universaal.org/PhThing.owl#PhysicalThing> .\"\r\n"
	    + "  } \r\n"
	    + " }";   
    public static final String CREATE_CALLEE="{\r\n"
	    + "  \"callee\": {\r\n"
	    + "    \"@id\": \""+R_ID+"\",\r\n"
	    + "     \"callback\": \""+R_CALLBACK+R_ID+"\",\r\n"
	    + "     \"profile\": \"@prefix ns: <http://ontology.universaal.org/PhThing.owl#> .\\r\\n"
	    + "@prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> .\\r\\n"
	    + "@prefix ns1: <http://www.daml.org/services/owl-s/1.1/Profile.owl#> .\\r\\n"
	    + "@prefix owl: <http://www.w3.org/2002/07/owl#> .\\r\\n"
	    + "@prefix ns2: <http://ontology.universAAL.org/Workbench.owl#> .\\r\\n"
	    + "@prefix ns3: <http://www.daml.org/services/owl-s/1.1/Service.owl#> .\\r\\n"
	    + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\\r\\n"
	    + "@prefix ns4: <http://ontology.universAAL.org/Device.owl#> .\\r\\n"
	    + "@prefix : <http://www.daml.org/services/owl-s/1.1/Process.owl#> .\\r\\n"
	    + "_:BN000000 ns3:presentedBy <"+PREFIX+"serverOf"+R_ID+"> ;\\r\\n"
	    + "  a ns1:Profile ;\\r\\n"
	    + "  ns1:has_process <"+PREFIX+"serverOf"+R_ID+"Process> ;\\r\\n"
	    + "  ns1:hasResult [\\r\\n"
	    + "    :withOutput (\\r\\n"
	    + "      [\\r\\n"
	    + "        a :OutputBinding ;\\r\\n"
	    + "        :toParam <"+PREFIX+"output> ;\\r\\n"
	    + "        :valueForm \\\"\\\"\\\"\\r\\n"
	    + "          @prefix : <http://ontology.universAAL.org/Service.owl#> .\\r\\n"
	    + "          _:BN000000 a :PropertyPath ;\\r\\n"
	    + "            :thePath (\\r\\n"
	    + "              <http://ontology.universaal.org/PhThing.owl#controls>\\r\\n"
	    + "            ) .\\r\\n"
	    + "          \\\"\\\"\\\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>\\r\\n"
	    + "      ]\\r\\n"
	    + "    ) ;\\r\\n"
	    + "    a :Result\\r\\n"
	    + "  ] ;\\r\\n"
	    + "  ns1:hasOutput (\\r\\n"
	    + "    <"+PREFIX+"output>\\r\\n"
	    + "  ) ;\\r\\n"
	    + "  ns1:hasInput (\\r\\n"
	    + "    <"+PREFIX+"input>\\r\\n"
	    + "  ) .\\r\\n"
	    + "<"+PREFIX+"output> a :Output ;\\r\\n"
	    + "  :parameterType \\\""+R_TYPE+"\\\"^^xsd:anyURI .\\r\\n"
	    + ":ThisPerform a :Perform .\\r\\n"
	    + "<"+R_TYPE+"> a owl:Class .\\r\\n"
	    + "<"+PREFIX+"input> <http://ontology.universAAL.org/Service.owl#parameterCardinality> \\\"1\\\"^^xsd:int ;\\r\\n"
	    + "  a :Input ;\\r\\n"
	    + "  :parameterType \\\""+R_TYPE+"\\\"^^xsd:anyURI .\\r\\n"
	    + "<"+PREFIX+"serverOf"+R_ID+"> a ns:DeviceService ;\\r\\n"
	    + "  pvn:instanceLevelRestrictions (\\r\\n"
	    + "    [\\r\\n"
	    + "      owl:hasValue [\\r\\n"
	    + "        :fromProcess :ThisPerform ;\\r\\n"
	    + "        a :ValueOf ;\\r\\n"
	    + "        :theVar <"+PREFIX+"input>\\r\\n"
	    + "      ] ;\\r\\n"
	    + "      a owl:Restriction ;\\r\\n"
	    + "      owl:onProperty ns:controls\\r\\n"
	    + "    ]\\r\\n"
	    + "    [\\r\\n"
	    + "      a owl:Restriction ;\\r\\n"
	    + "      owl:allValuesFrom <"+R_TYPE+"> ;\\r\\n"
	    + "      owl:onProperty ns:controls\\r\\n"
	    + "    ]\\r\\n"
	    + "  ) ;\\r\\n"
	    + "  ns3:presents _:BN000000 ;\\r\\n"
	    + "  pvn:numberOfValueRestrictions \\\"1\\\"^^xsd:int . \"\r\n"
	    + "  }\r\n"
	    + " }";
    public static final String CREATE_SUBSCRIBER="{\r\n"
	    + "  \"subscriber\": {\r\n"
	    + "    \"@id\": \""+R_ID+"\",\r\n"
	    + "     \"callback\": \""+R_CALLBACK+R_ID+"\",\r\n"
	    + "    \"pattern\": \""
	    + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\\r\\n"
	    + "@prefix : <http://www.w3.org/2002/07/owl#> .\\r\\n"
	    + "_:BN000000 a <http://ontology.universAAL.org/Context.owl#ContextEventPattern> ;\\r\\n"
	    + "  <http://www.w3.org/2000/01/rdf-schema#subClassOf> [\\r\\n"
	    + "      a :Restriction ;\\r\\n"
	    + "      :hasValue <"+PREFIX+R_ID+"> ;\\r\\n"
	    + "      :onProperty rdf:subject\\r\\n"
	    + "    ] .\\r\\n"
	    + "<"+PREFIX+R_ID+"> a <http://ontology.universaal.org/PhThing.owl#Device>,"
	    + " <http://ontology.universaal.org/PhThing.owl#PhysicalThing> .\"\r\n"
	    + "  }\r\n"
	    + " }";    
    public static final String EVENT="@prefix ns: <http://ontology.universaal.org/PhThing.owl#> ."
	    + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
	    + "@prefix ns1: <http://ontology.universAAL.org/Workbench.owl#> ."
	    + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
	    + "@prefix ns2: <http://ontology.universAAL.org/Device.owl#> ."
	    + "@prefix : <http://ontology.universAAL.org/Context.owl#> ."
	    + "<urn:org.universAAL.middleware.context.rdf:ContextEvent#_:c0a883e1ef646809:a73> :hasProvider ns1:providerX ;"
	    + "  a :ContextEvent ;"
	    + "  rdf:subject <"+PREFIX+R_ID+"> ;"
	    + "  :hasTimestamp \"1528876561633\"^^xsd:long ;"
	    + "  rdf:predicate ns2:hasValue ;"
	    + "  rdf:object \"26.5\"^^xsd:float ."
	    + "ns1:providerX a :ContextProvider ;"
	    + "  :hasType :controller ."
	    + ":controller a :ContextProviderType ."
	    + "<"+PREFIX+R_ID+"> a ns2:TemperatureSensor ,"
	    + "    ns:Device ,"
	    + "    ns:PhysicalThing ;"
	    + "  ns2:hasValue \"26.5\"^^xsd:float .";
    public static final String REQUEST="@prefix ns: <http://www.daml.org/services/owl-s/1.1/Profile.owl#> ."
	    + "@prefix ns1: <http://ontology.universaal.org/PhThing.owl#> ."
	    + "@prefix owl: <http://www.w3.org/2002/07/owl#> ."
	    + "@prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> ."
	    + "@prefix ns2: <http://www.daml.org/services/owl-s/1.1/Process.owl#> ."
	    + "@prefix ns3: <http://www.daml.org/services/owl-s/1.1/Service.owl#> ."
	    + "@prefix : <http://ontology.universAAL.org/Workbench.owl#> ."
	    + "_:BN000000 a pvn:ServiceRequest ;"
	    + "  pvn:requiredResult ["
	    + "    ns2:withOutput ("
	    + "      ["
	    + "        a ns2:OutputBinding ;"
	    + "        ns2:toParam :output1 ;"
	    + "        ns2:valueForm \"\"\""
	    + "          @prefix : <http://ontology.universAAL.org/Service.owl#> ."
	    + "          _:BN000000 a :PropertyPath ;"
	    + "            :thePath ("
	    + "              <http://ontology.universaal.org/PhThing.owl#controls>"
	    + "            ) ."
	    + "          \"\"\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>"
	    + "      ]"
	    + "    ) ;"
	    + "    a ns2:Result"
  	    + "] ;"
	    + "  pvn:requestedService :serviceX ."
	    + "<"+PREFIX+R_ID+"> a <http://ontology.universAAL.org/Device.owl#TemperatureSensor> ,"
	    + "    ns1:Device ,"
	    + "    ns1:PhysicalThing ."
	    + ":output1 a ns2:Output ."
	    + ":serviceX a ns1:DeviceService ;"
	    + "  pvn:instanceLevelRestrictions ("
	    + "    ["
	    + "      owl:hasValue <"+PREFIX+R_ID+"> ;"
	    + "      a owl:Restriction ;"
	    + "      owl:onProperty ns1:controls"
	    + "    ]"
	    + "  ) ;"
	    + "  ns3:presents ["
	    + "    ns3:presentedBy :serviceX ;"
	    + "    a ns:Profile ;"
	    + "    ns:has_process :serviceXProcess"
	    + "  ] ;"
	    + "  pvn:numberOfValueRestrictions \"1\"^^<http://www.w3.org/2001/XMLSchema#int> .";
}