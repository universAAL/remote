/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.ri.gateway.eimanager.impl.security;

import org.universAAL.ri.gateway.communicator.service.impl.SecurityEntry;
import org.universAAL.ri.gateway.communicator.service.impl.SecurityManager;
import org.universAAL.ri.gateway.eimanager.ExportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.BusMemberType;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

@Deprecated
public class ExportSecurityOperationInterceptor implements
	ExportOperationInterceptor {

    public void process(final ImportRequest importRequest)
	    throws InterruptExecutionException {
	String[] uids = null;
	String errorUid = "";
	if (importRequest.getMember().equals(
		BusMemberType.ServiceCallee.toString())
		|| importRequest.getMember().equals(
			BusMemberType.ServiceCaller.toString())) {
	    uids = new String[1];
	    uids[0] = importRequest.getServerNamespace();
	} else if (importRequest.getMember().equals(
		BusMemberType.ContextPublisher.toString())
		|| importRequest.getMember().equals(
			BusMemberType.ContextSubscriber.toString())) {
	    uids = importRequest.getSubjectURIs();
	}

	boolean shouldPass = true;
	for (final SecurityEntry entry : SecurityManager.Instance
		.getDenyExportSecurityEntries()) {
	    for (final String uid : uids) {
		if (uid.matches(entry.getEntryRegex())) {
		    shouldPass = false;
		    errorUid = uid;
		    break;
		}
	    }
	}

	if (!shouldPass) {
	    throw new InterruptExecutionException(
		    "UID: "
			    + errorUid
			    + " was matched by one or more EXPORT DENY security entries.");
	}

	int passCount = 0;
	for (final SecurityEntry entry : SecurityManager.Instance
		.getAllowExportSecurityEntries()) {
	    for (final String uid : uids) {
		if (uid.matches(entry.getEntryRegex())) {
		    passCount++;
		}
	    }
	}
	if (passCount != uids.length) {
	    throw new InterruptExecutionException(
		    "Not all provided uids (only " + passCount + " from "
			    + uids.length
			    + ") was matched to EXPORT ALLOW security entries.");
	}
    }

    public int getPriority() {
	return -1;
    }

}
