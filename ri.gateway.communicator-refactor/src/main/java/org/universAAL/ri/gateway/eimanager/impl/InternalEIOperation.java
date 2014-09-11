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
package org.universAAL.ri.gateway.eimanager.impl;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

@Deprecated
public class InternalEIOperation {

    protected String memberId;
    protected BusMember busMember;
    protected BusMemberType type;
    protected RepoOperation op;

    public InternalEIOperation(final BusMember member, final RepoOperation op) {
	this.busMember = member;
	this.op = op;
	if (member instanceof ServiceCallee) {
	    type = BusMemberType.ServiceCallee;
	    memberId = ((ServiceCallee) busMember).getMyID();
	} else if (member instanceof ServiceCaller) {
	    type = BusMemberType.ServiceCaller;
	    memberId = ((ServiceCaller) busMember).getMyID();
	} else if (member instanceof ContextPublisher) {
	    type = BusMemberType.ContextPublisher;
	    memberId = ((ContextPublisher) busMember).getMyID();
	} else if (member instanceof ContextSubscriber) {
	    type = BusMemberType.ContextSubscriber;
	    memberId = ((ContextSubscriber) busMember).getMyID();
	} else if (member instanceof UIHandler) {
	    type = BusMemberType.UIHandler;
	    memberId = ((UIHandler) busMember).getMyID();
	} else if (member instanceof UICaller) {
	    type = BusMemberType.UICaller;
	    memberId = ((UICaller) busMember).getMyID();
	}
    }

    public BusMember getBusMember() {
	return busMember;
    }

    public void setBusMember(final BusMember busMember) {
	this.busMember = busMember;
    }

    public BusMemberType getType() {
	return type;
    }

    public void setType(final BusMemberType type) {
	this.type = type;
    }

    public RepoOperation getOp() {
	return op;
    }

    public void setOp(final RepoOperation op) {
	this.op = op;
    }

    public String getMemberId() {
	return memberId;
    }

    public void setMemberId(final String memberId) {
	this.memberId = memberId;
    }

}