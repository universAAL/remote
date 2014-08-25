/*
Copyright 2014 Universidad Politécnica de Madrid, http://www.upm.es/
Life Supporting Technologies

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
package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.util.concurrent.BlockingQueue;

import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;

public class ExportProcessExecutor implements Runnable {

	private final BlockingQueue<InternalExportOperation> exportQueue;

	public ExportProcessExecutor(
			final BlockingQueue<InternalExportOperation> queue) {
		this.exportQueue = queue;
	}

	public void run() {
		Thread.currentThread()
				.setName("Space Gateway :: ExportProcessExecutor");
		while (!(Thread.currentThread().isInterrupted())) {
			try {
				final InternalExportOperation op = exportQueue.take();
				this.process(op);
			} catch (final InterruptedException e) {
				// Set interrupted flag.
				Thread.currentThread().interrupt();
			}
		}
	}

	private void process(final InternalExportOperation op) {
		switch (op.getType()) {
		case ServiceCallee:
		case ContextPublisher:
		case UIHandler:
			switch (op.getOp()) {
			case Publish:
				EIRepoAccessManager.Instance
						.publishExportToRepo(new ExportEntry(op.getMemberId(),
								op.getBusMember(), op));
				break;

			case Purge: // FIXME Check call (it is the same as above)
				EIRepoAccessManager.Instance
						.publishExportToRepo(new ExportEntry(op.getMemberId(),
								op.getBusMember(), op));
				break;
			}
		default:
			// do nothing as only service callees, context publishers and
			// uihandlers can be exported;
		}
	}

}
