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
package org.universAAL.ri.api.manager.push;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ri.api.manager.Activator;
import org.universAAL.ri.api.manager.Configuration;
import org.universAAL.ri.api.manager.RemoteAPI;
import org.universAAL.ri.api.manager.exceptions.APIImplException;
import org.universAAL.ri.api.manager.exceptions.PushException;

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

    /**
     * Build a Context Event callback message and send it to the client remote
     * node endpoint through GCM.
     * 
     * @param nodeid
     *            The client id, needed in case the GCM key needs to be updated
     *            in the DB
     * @param remoteid
     *            The client remote node endpoint
     * @param event
     *            The serialized Context Event to send
     * @param toURI 
     */
    public static void sendC(String nodeid, String remoteid, ContextEvent event, String toURI) throws PushException {
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
	.append("to=").append(toURI);

	Builder build = new Message.Builder();
	//If included, allows developers to test their request without actually sending a message
	if(test) build.dryRun(true);
	//How long (in seconds) the message should be kept on GCM storage if the device is offline
	if(ttl>0) build.timeToLive(ttl);
	//Payload data, expressed as parameters prefixed with data. and suffixed as the key
	build.addData(RemoteAPI.KEY_METHOD, "SENDC")
	.addData(RemoteAPI.KEY_TO, toURI)
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
	    Activator.logW("PushGCM.sendC", "Payload data too big. Sending just the SpO triple");
	}
	
	Message msg=build.build();
	send(nodeid, remoteid, msg);
    }

    /**
     * Build a ServiceCall callback message and send it to the client remote
     * node endpoint through GCM.
     * 
     * @param nodeid
     *            The client id, needed in case the GCM key needs to be updated
     *            in the DB
     * @param remoteid
     *            The client remote node endpoint
     * @param call
     *            The serialized Service Call to send
     * @param toURI 
     * @return The Service Response that the client remote node will have sent
     *         as response to the callback
     */
    public static ServiceResponse callS(String nodeid, String remoteid, ServiceCall call, String toURI) throws PushException {
	boolean test=Configuration.getGCMDry();

	List inputs = (List) call.getProperty(ServiceCall.PROP_OWLS_PERFORM_HAS_DATA_FROM);
	String serial=Activator.getParser().serialize(call);
	
	int size=0; //Find out size of the WHOLE payload
	StringBuilder combined=new StringBuilder(serial);
	
	Builder build = new Message.Builder();
	//If included, allows developers to test their request without actually sending a message
	if(test) build.dryRun(true);
	//Payload data, expressed as parameters prefixed with data. and suffixed as the key
	build.addData(RemoteAPI.KEY_METHOD, "CALLS");
	build.addData(RemoteAPI.KEY_TO, toURI);
	combined.append("method=CALLS");
	combined.append("to=").append(toURI);
	if (inputs != null) {
	    for (Iterator i = inputs.iterator(); i.hasNext();) {
		Resource binding = (Resource) i.next(), in = (Resource) binding
			.getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
		if (in != null) {
		    build.addData(in.getURI(), call.getInputValue(in.getURI()).toString());
		    combined.append(in.getURI()).append(in.getURI());
		}
	    }
	}
	
	//Add the call URI so the remote node can assign its response to the call
	build.addData(RemoteAPI.KEY_CALL, call.getURI());
	combined.append(RemoteAPI.KEY_CALL).append(call.getURI());
	
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
	    Activator.logW("PushGCM.callS", "Payload data too big. Sending just the input URIs");
	}
	
	// First we say we are waiting for a call (timeout response as default)
	ServiceResponse sr=new ServiceResponse(CallStatus.responseTimedOut);
	pendingCalls.put(call.getURI()+"@"+nodeid,sr); //For multitenancy, where 1 call can address N nodes
	//Do it before sending to avoid getting answer before putting the pendingCall
	
	Message msg=build.build();
	send(nodeid, remoteid, msg);
	
	long t=System.currentTimeMillis()+30000l;
	try {
	    synchronized (pendingCalls) {
		while (System.currentTimeMillis()<t){
		    // Wait for servlet to write a non-timeout response for the call
		    Activator.logD("PushGCM.callS", "WAITING FOR RESPONSE");
		    pendingCalls.wait(30000);
		    ServiceResponse srfinal=pendingCalls.get(call.getURI()+"@"+nodeid);
		    if(!srfinal.getCallStatus().equals(CallStatus.responseTimedOut)){
			Activator.logD("PushGCM.callS", "GOT RESPONSE");
			return pendingCalls.remove(call.getURI()+"@"+nodeid);//if so, return it and remove from pending
		    }
		    Activator.logD("PushGCM.callS", "NO RESPONSE YET");
		    //If is still responseTimedOut it means servlet updated other call, not this one. Wait again
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    pendingCalls.put(call.getURI()+"@"+nodeid, new ServiceResponse(CallStatus.serviceSpecificFailure));
	}
	return pendingCalls.remove(call.getURI()+"@"+nodeid);
    }
    
    /**
     * Sends an arbitrary message to the remote endpoint client through GCM.
     * 
     * @param nodeid
     *            The client id, needed in case the GCM key needs to be updated
     *            in the DB
     * @param remoteid
     *            The client remote node endpoint (GCM Key)
     * @param msg
     *            The built GCM Message
     * @throws PushException
     *             If there is an error when sending the message to GCM server
     */
    private static void send(String nodeid, String remoteid, Message msg) throws PushException{
	Sender sender=new Sender(GCM_APP_KEY);
	if(remoteid.equals(REG_ID_OUTDATED)){//See below for when this happens
	    //not a valid GCM key, so dont send anything
	    throw new PushException("The GCM key is outdated. Remote node should get a new one");
	}
	String regid=remoteid;
	if(remoteid.startsWith("gcm:")){//Remove the gcm: prefix from remoteid. DEPRECATED
	    regid=remoteid.substring(4);
	}
	try {
	    //This procedure is mandated by GCM (See Result javadoc)
	    Result res=sender.send(msg, regid, 3);
	    String id = res.getMessageId();
	    if(id!=null){
		String canon = res.getCanonicalRegistrationId();
		if(canon!=null){
		    Activator.getRemoteAPI().register(nodeid, canon); //This just updates the remoteid
		    Activator.getPersistence().storeRegister(nodeid, canon, null);
		}
	    }else{
		if(res.getErrorCodeName().equals("NotRegistered")){
		    //Remote node app was uninstalled/updated/unregistered/refreshed by Google
		    //Maintain the MW wrappers, but dont allow push messages until remote node updates itself
		    Activator.getRemoteAPI().register(nodeid, REG_ID_OUTDATED); //This just updates the remoteid
		    Activator.getPersistence().storeRegister(nodeid, REG_ID_OUTDATED, null);
		}
		throw new PushException("Error sending to GCM. Error code received: "+res.getErrorCodeName());
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new PushException("Error sending to GCM. Unable to use communication channel: "+e.getMessage());
	} catch (APIImplException e) {
	    e.printStackTrace();
	    Activator.logW("PushGCM.send",
		    "Unable to register new remoteID of node. The stored remoteID will remain the old invalid one");
	}
    }

    /**
     * Handles the response sent by the remote client, sending it to the right
     * pending call.
     * 
     * @param resp
     *            The encoded response text sent by the client
     * @param nodeid
     *            The id of the remote node, used to identify the right pending
     *            call
     * @throws PushException
     *             if the response message could not be properly read
     */
    public static void handleResponse(String resp, String nodeid) throws PushException {
	Activator.logD("PushGCM.handleResponse", "RECEIVED RESPONSE");
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	StringBuilder strb = new StringBuilder();
	InputStreamReader ir = new InputStreamReader(new ByteArrayInputStream(resp.getBytes()));
	BufferedReader br = new BufferedReader(ir);
	String key=null;
	try{
	    String line = br.readLine();
	    while (line != null && !line.equals(RemoteAPI.FLAG_TURTLE)) {
		String[] parts = line.split("=", 2);//parts[0]: key (output URI)    parts[1]: value
		if (parts.length == 2) {
		    if (!parts[0].equals(RemoteAPI.KEY_STATUS) && !parts[0].equals(RemoteAPI.KEY_CALL)) {
			String[] resource = parts[1].split("@", 2);//resource[0]: uri    resource[1]: type
			if (resource.length != 2)
			    throw new PushException("Required Outputs are not properly defined. " +
			    		"They must be in the form instanceURI@typeURI");
			if(resource[0].startsWith("[")){//Its a list
			    String[] list=resource[0].replace("[", "").replace("]","").trim().split(",");
			    ArrayList listouts=new ArrayList(list.length);
			    for(int i=0;i<list.length;i++){
				if (resource[1].startsWith("http://www.w3.org/2001/XMLSchema")) {//Its datatypes
				    listouts.add(TypeMapper.getJavaInstance(resource[0], resource[1]));
				}else{//Its resources
				    listouts.add(Resource.getResource(resource[1], resource[0]));
				}
			    }
			    sr.addOutput(new ProcessOutput(parts[0],listouts));
			}else{//Its one item
			    if (resource[1].startsWith("http://www.w3.org/2001/XMLSchema")) {//Its datatype
				sr.addOutput(new ProcessOutput(parts[0], TypeMapper.getJavaInstance(resource[0], resource[1])));
			    } else {//Its resource
				sr.addOutput(new ProcessOutput(parts[0], Resource.getResource(resource[1], resource[0])));
			    }
			}
		    } else if(parts[0].equals(RemoteAPI.KEY_CALL)){
			key = parts[1]+"@"+nodeid;//Identifies the call in pendingCalls
		    } else if(parts[0].equals(RemoteAPI.KEY_STATUS)){
			if(!parts[1].equals(CallStatus.succeeded)){
			    sr=new ServiceResponse(CallStatus.valueOf(parts[1]));
			    //do not break the while because we should keep reading lines so we get to serialized sr (better) 
			}
		    }
		}
		line = br.readLine();
	    } 
	    while (line != null) {
		// We only get here if there was something after TURTLE (and there was TURTLE)
		line = br.readLine();
		if (line != null)
		    strb.append(line);
	    }
	    br.close();
	} catch (IOException e) {//Do not send response, it will timeout.
	    throw new PushException("Unable to read Response message from client"); 
	}
	String serialized = strb.toString();
	if (key==null){ //Do not send response, it will timeout.
	    throw new PushException("Response message from client does not contain a call identifier"); 
	}
	if (serialized.isEmpty()) {
	    // no serialized response included, rely on the built sr
	    synchronized (pendingCalls) {
		if (pendingCalls.containsKey(key)) {
		    // IF there is a call waiting for this sr, put it in the pending and wake up thread
		    pendingCalls.put(key, sr);
		    pendingCalls.notifyAll();
		}
	    }
	}else{
	    Object parsedsr = Activator.getParser().deserialize(serialized);
	    if (parsedsr instanceof ServiceResponse) {
		Activator.logD("PushGCM.handleResponse", "UPDATING RESPONSE");
		synchronized (pendingCalls) {
		    if (pendingCalls.containsKey(key)){
			//IF there is a call waiting for this sr, put it in the pending and wake up thread
			pendingCalls.put(key, (ServiceResponse) parsedsr);
			pendingCalls.notifyAll();
		    }
		}
	    }
	}
    }

}
