package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import org.universAAL.ri.gateway.eimanager.impl.importing.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;

public class ExportProcessExecutor implements Runnable {

    private BlockingQueue<InternalExportOperation> exportQueue;

    public ExportProcessExecutor(BlockingQueue<InternalExportOperation> queue) {
	this.exportQueue = queue;
    }

    public void run() {
	while (!(Thread.currentThread().isInterrupted())) {
	    try {
		final InternalExportOperation op = exportQueue.take();
		this.process(op);
	    } catch (InterruptedException e) {
		// Set interrupted flag.
		Thread.currentThread().interrupt();
	    }
	    // Thread is getting ready to die, but first,
	    // drain remaining elements on the queue and process them.
	    final LinkedList<InternalExportOperation> remainingObjects = new LinkedList<InternalExportOperation>();
	    exportQueue.drainTo(remainingObjects);
	    for (InternalExportOperation op : remainingObjects) {
		this.process(op);
	    }
	}
    }

    private void process(InternalExportOperation op) {
	switch(op.getType()){
	case ServiceCallee:
	case ContextPublisher:
	case UIHandler:
	    switch(op.getOp()){
	    case Publish:
		EIRepoAccessManager.Instance.publishExportToRepo(new ExportEntry(op.getMemberId(), op.getBusMember(), op));
		break;
		
	    case Purge:
		EIRepoAccessManager.Instance.publishExportToRepo(new ExportEntry(op.getMemberId(), op.getBusMember(), op));
		break;
	    }
	default:
	    //do nothing as only service callees, context publishers and uihandlers can be exported;
	}
    }

}
