package org.universAAL.ri.keepalive.receiver;

import java.util.HashMap;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class CSubscriber extends ContextSubscriber {

    private static HashMap<String, Long> lastknownof = new HashMap<String, Long>();

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
	    }
	}
    }
    
    public static HashMap<String, Long> getLastknownof() {
        return lastknownof;
    }

}
