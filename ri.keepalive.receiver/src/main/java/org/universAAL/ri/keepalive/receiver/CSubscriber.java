/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.ri.keepalive.receiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class CSubscriber extends ContextSubscriber {

    private static HashMap<String, Long> lastknownof = new HashMap<String, Long>();
    private static HashSet<String> missing = new HashSet<String>();

    protected CSubscriber(ModuleContext context,
	    ContextEventPattern[] initialSubscriptions) {
	super(context, initialSubscriptions);
    }

    protected CSubscriber(ModuleContext context) {
	super(context, getPermanentSubscriptions());
    }

    private static ContextEventPattern[] getPermanentSubscriptions() {
	ContextEventPattern cep = new ContextEventPattern();
	cep.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_SUBJECT, SystemInfo.MY_URI));
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, SystemInfo.PROP_ALIVE));
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_OBJECT, new Boolean(true)));
	return new ContextEventPattern[] { cep };
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    public void handleContextEvent(ContextEvent event) {
	List<String> scopes = event.getScopes(); //TODO Use SystemInfo URI instead?
	Long tst = event.getTimestamp();
	for (String scope : scopes) {
	    if (scope != null && !scope.isEmpty()) {
		lastknownof.put(scope, tst);
		missing.remove(scope);
	    }
	}
    }
    
    public static HashMap<String, Long> getLastknownof() {
        return lastknownof;
    }

    public static HashSet<String> getMissing() {
        return missing;
    }
}
