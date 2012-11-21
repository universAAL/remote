package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;

public class ImportProcessExecutor implements Runnable{

    private BlockingQueue<InternalImportOperation> importQueue;

    public ImportProcessExecutor(BlockingQueue<InternalImportOperation> queue) {
	this.importQueue = queue;
    }

    public void run() {
	while (!(Thread.currentThread().isInterrupted())) {
	    try {
		final InternalImportOperation op = importQueue.take();
		this.process(op);
	    } catch (InterruptedException e) {
		// Set interrupted flag.
		Thread.currentThread().interrupt();
	    }
	    // Thread is getting ready to die, but first,
	    // drain remaining elements on the queue and process them.
	    final LinkedList<InternalImportOperation> remainingObjects = new LinkedList<InternalImportOperation>();
	    importQueue.drainTo(remainingObjects);
	    for (InternalImportOperation op : remainingObjects) {
		this.process(op);
	    }
	}
    }

    private void process(InternalImportOperation op) {
	switch(op.getType()){
	case ServiceCaller:
	case ContextSubscriber:
	case UICaller:
	    switch(op.getOp()){
	    case Publish:
		EIRepoAccessManager.Instance.publishImportToRepo(new ImportEntry(op.getMemberId(), op.getBusMember(), op.getRemoteRegisteredProxyId(), op));
		break;
		
	    case Purge:
		EIRepoAccessManager.Instance.removeImportToRepo(new ImportEntry(op.getMemberId(), op.getBusMember(), op.getRemoteRegisteredProxyId(), op));
		break;
	    }
	default:
	    //do nothing as only service callers, context subscribers and uicallers can import remote objects;
	}
    }

}
