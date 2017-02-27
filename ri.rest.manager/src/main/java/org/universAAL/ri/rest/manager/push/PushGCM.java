/*
	Copyright 2015 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.ri.rest.manager.push;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.server.Configuration;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;

/**
 * Class that manages the push of callbacks to client remote node endpoints
 * using Google Cloud Messaging.
 * 
 * @author alfiva
 * 
 */
public class PushGCM {
    
    /**
     * GCM Key identifying the (this) server
     */
    private static final String GCM_APP_KEY = Configuration.getGCMKey();
    /**
     * Keyword used as GCM RegID key of clients with outdated keys (determined
     * by GCM server)
     */
    private static final String REG_ID_OUTDATED = "Outdated";
    /**
     * Holds in memory the pending calls awaiting response from remote clients
     */
    public static Hashtable<String,ServiceResponse> pendingCalls=new Hashtable<String,ServiceResponse>();

    public static void pushContextEvent(String callback, String id, ContextEvent event) throws PushException {
	boolean test=Configuration.getGCMDry();
	
	int ttl=0; // 4 weeks is the default in GCM
	Long millis=event.getExpirationTime();
	if(millis!=null && millis < 2419200000l){
	    Long secs=millis/1000;
	    ttl=secs.intValue();
	}
	
	String subject=event.getRDFSubject().toString();
	String predicate=event.getRDFPredicate();
	String object=event.getRDFObject().toString();
	String serial=Activator.getParser().serialize(event);
	
	int size=0; //Find out size of the WHOLE payload
	StringBuilder combined=new StringBuilder(serial)
	.append(subject).append(predicate).append(object)
	.append(ContextEvent.PROP_RDF_SUBJECT)
	.append(ContextEvent.PROP_RDF_PREDICATE)
	.append(ContextEvent.PROP_RDF_OBJECT)
	.append("method=SENDC")
	.append("to=").append(id);

	Builder build = new Message.Builder();
	//If included, allows developers to test their request without actually sending a message
	if(test) build.dryRun(true);
	//How long (in seconds) the message should be kept on GCM storage if the device is offline
	if(ttl>0) build.timeToLive(ttl);
	//Payload data, expressed as parameters prefixed with data. and suffixed as the key
	build.addData("method", "SENDC")
	.addData("to", id)
	.addData(ContextEvent.PROP_RDF_SUBJECT, subject)
	.addData(ContextEvent.PROP_RDF_PREDICATE, predicate)
	.addData(ContextEvent.PROP_RDF_OBJECT, object);
	//There is no limit on the number of key/value pairs, though there is a limit on the total size of the message (4kb)
	try {
	    size=combined.toString().getBytes("UTF-8").length; 
	} catch (UnsupportedEncodingException e) {
	    size=serial.length()*4; // worst case, 4 bytes for all chars
	    e.printStackTrace();
	}
	if(size<4000){ //If >4k, dont send the serial, just the SpO (4000 to round up)
	    build.addData("param", serial);
	}else{
	    Activator.logW("PushGCM.pushContextEvent", "Payload data too big. Sending just the SpO triple");
	}
	
	Message msg=build.build();
	Activator.logI("PushGCM.pushContextEvent", "Attempting to send event "+event.getURI()+" to callback "+callback);
	send(callback, id, msg);
    }

    public static void pushServiceCall(String callback, String id, ServiceCall call, String origin) throws PushException {
	boolean test=Configuration.getGCMDry();

	List inputs = (List) call.getProperty(ServiceCall.PROP_OWLS_PERFORM_HAS_DATA_FROM);
	String serial=Activator.getParser().serialize(call);
	
	int size=0; //Find out size of the WHOLE payload
	StringBuilder combined=new StringBuilder(serial);
	
	Builder build = new Message.Builder();
	//If included, allows developers to test their request without actually sending a message
	if(test) build.dryRun(true);
	//Payload data, expressed as parameters prefixed with data. and suffixed as the key
	build.addData("method", "CALLS");
	build.addData("to", id);
	combined.append("method=CALLS");
	combined.append("to=").append(id);
	combined.append("o=").append(origin);
	if (inputs != null) {
	    for (Iterator i = inputs.iterator(); i.hasNext();) {
		Resource binding = (Resource) i.next(), in = (Resource) binding
			.getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
		if (in != null) {
		    build.addData(in.getURI(), call.getInputValue(in.getURI()).toString());
		    combined.append(in.getURI()).append(call.getInputValue(in.getURI()).toString());
		}
	    }
	}
	
	//There is no limit on the number of key/value pairs, though there is a limit on the total size of the message (4kb)
	try {
	    size=combined.toString().getBytes("UTF-8").length; 
	} catch (UnsupportedEncodingException e) {
	    size=serial.length()*4; // worst case, 4 bytes for all chars
	    e.printStackTrace();
	}
	if(size<4000){ //If >4k, dont send the serial, just the inputs (4000 to round up)
	    build.addData("param", serial);
	}else{
	    Activator.logW("PushGCM.pushServiceCall", "Payload data too big. Sending just the input URIs");
	}
	
	Message msg=build.build();
	Activator.logI("PushGCM.pushServiceCall", "Attempting to send call "+origin+" to callback "+callback);
	send(callback, id, msg);
    }
    
    private static void send(String callback, String id, Message msg) throws PushException{
	Sender sender=new Sender(GCM_APP_KEY);
	if(callback.equals(REG_ID_OUTDATED)){//See below for when this happens
	    //not a valid GCM key, so dont send anything
	    throw new PushException("The GCM key is outdated. Remote node should get a new one");
	}

	try {
	    //This procedure is mandated by GCM (See Result javadoc)
	    Result res=sender.send(msg, callback, 3);
	    String msgid = res.getMessageId();
	    if(msgid!=null){
		String canon = res.getCanonicalRegistrationId();
		if(canon!=null){
		    //TODO!!!!!!!!!!!!
//		    Activator.getRemoteAPI().register(nodeid, canon); //This just updates the remoteid
//		    Activator.getPersistence().storeRegister(nodeid, canon, null);
		}
	    }else{
		if(res.getErrorCodeName().equals("NotRegistered")){
		    //Remote node app was uninstalled/updated/unregistered/refreshed by Google
		    //Maintain the MW wrappers, but dont allow push messages until remote node updates itself
		    //TODO!!!!!!!!!!!!
//		    Activator.getRemoteAPI().register(nodeid, REG_ID_OUTDATED); //This just updates the remoteid
//		    Activator.getPersistence().storeRegister(nodeid, REG_ID_OUTDATED, null);
		}
		throw new PushException("Error sending to GCM. Error code received: "+res.getErrorCodeName());
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new PushException("Error sending to GCM. Unable to use communication channel: "+e.getMessage());
	} catch (Exception e) {
	    e.printStackTrace();
	    Activator.logW("PushGCM.send",
		    "Unable to register new remoteID of node. The stored remoteID will remain the old invalid one");
	}
    }

}
