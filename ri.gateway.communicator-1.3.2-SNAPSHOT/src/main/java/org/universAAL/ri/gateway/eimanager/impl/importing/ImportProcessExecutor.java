package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.util.concurrent.BlockingQueue;

import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;

public class ImportProcessExecutor implements Runnable {

    private BlockingQueue<InternalImportOperation> importQueue;

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
	    } catch (InterruptedException e) {
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
					.getRemoteRegisteredProxyId(), op, true, ""));
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
