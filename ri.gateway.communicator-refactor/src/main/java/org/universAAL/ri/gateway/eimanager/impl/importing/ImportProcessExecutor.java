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
package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.util.concurrent.BlockingQueue;

import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;

@Deprecated
public class ImportProcessExecutor implements Runnable {

    private final BlockingQueue<InternalImportOperation> importQueue;

    public ImportProcessExecutor(
	    final BlockingQueue<InternalImportOperation> queue) {
	this.importQueue = queue;
    }

    public void run() {
	Thread.currentThread()
		.setName("Space Gateway :: ImportProcessExecutor");
	while (!(Thread.currentThread().isInterrupted())) {
	    try {
		final InternalImportOperation op = importQueue.take();
		this.process(op);
	    } catch (final InterruptedException e) {
		// Set interrupted flag.
		Thread.currentThread().interrupt();
	    }
	}
    }

    private void process(final InternalImportOperation op) {
	switch (op.getType()) {
	case ServiceCaller:
	case ContextSubscriber:
	case UICaller:
	    switch (op.getOp()) {
	    case Publish:
		EIRepoAccessManager.Instance
			.publishImportToRepo(new ImportEntry(op.getMemberId(),
				op.getBusMember(), op
					.getRemoteRegisteredProxyId(), op,
				true, ""));
		break;

	    case Purge:
		EIRepoAccessManager.Instance.removeImportFromRepo(op
			.getImportEntry());
		break;
	    }
	default:
	    // do nothing as only service callers, context subscribers and
	    // uicallers can import remote objects;
	}
    }

}
