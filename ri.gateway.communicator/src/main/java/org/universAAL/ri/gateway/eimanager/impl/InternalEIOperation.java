package org.universAAL.ri.gateway.eimanager.impl;

import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public class InternalEIOperation {

    protected String memberId;
    protected BusMember busMember;
    protected BusMemberType type;
    protected RepoOperation op;

    public InternalEIOperation(BusMember member, RepoOperation op){
	this.busMember = member;
	this.op = op;
	if (member instanceof ServiceCallee){
	    type = BusMemberType.ServiceCallee;
	    memberId = ((ServiceCallee)busMember).getMyID();
	}else if (member instanceof ServiceCaller){
	    type = BusMemberType.ServiceCaller;
	    memberId = ((ServiceCaller)busMember).getMyID();
	}else if (member instanceof ContextPublisher){
	    type = BusMemberType.ContextPublisher;
	    memberId = ((ContextPublisher)busMember).getMyID();
	}else if (member instanceof ContextSubscriber){
	    type = BusMemberType.ContextSubscriber;
	    memberId = ((ContextSubscriber)busMember).getMyID();
	}else if (member instanceof UIHandler){
	    type = BusMemberType.UIHandler;
	    memberId = ((UIHandler)busMember).getMyID();
	}else if (member instanceof UICaller){
	    type = BusMemberType.UICaller;
	    memberId = ((UICaller)busMember).getMyID();
	}
    }

    public BusMember getBusMember() {
        return busMember;
    }

    public void setBusMember(BusMember busMember) {
        this.busMember = busMember;
    }

    public BusMemberType getType() {
        return type;
    }

    public void setType(BusMemberType type) {
        this.type = type;
    }

    public RepoOperation getOp() {
        return op;
    }

    public void setOp(RepoOperation op) {
        this.op = op;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

}