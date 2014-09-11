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
package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.IOException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

@Deprecated
public class ProxyUICaller extends ProxyBusMember {

    private final ProxiedUICaller caller;
    private String modalityRegex;
    private UIHandlerProfile[] handlerProfiles;

    public ProxyUICaller(final ExportedProxyManager manager,
	    final String targetId, final ModuleContext mc,
	    final String modalityRegex, final UIHandlerProfile[] handlerProfiles) {
	super(manager, targetId, "", mc);
	this.modalityRegex = modalityRegex;
	this.handlerProfiles = handlerProfiles;
	caller = new ProxiedUICaller(mc);
    }

    public void invoke(final UIRequest req) {
	caller.sendUIRequest(req);
    }

    @Override
    public void removeProxy() {
	caller.close();
    }

    class ProxiedUICaller extends UICaller {

	protected ProxiedUICaller(final ModuleContext context) {
	    super(context);
	}

	@Override
	public void communicationChannelBroken() {
	}

	@Override
	public void dialogAborted(final String dialogID, final Resource data) {
	}

	@Override
	public void handleUIResponse(final UIResponse input) {
	    try {
		((ExportedProxyManager) getManager()).handleUIResponse(
			targetId, input);
	    } catch (final IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    @Override
    public String getId() {
	return caller.getMyID();
    }

    public String getModalityRegex() {
	return modalityRegex;
    }

    public void setModalityRegex(final String modalityRegex) {
	this.modalityRegex = modalityRegex;
    }

    public UIHandlerProfile[] getHandlerProfiles() {
	return handlerProfiles;
    }

    public void setHandlerProfiles(final UIHandlerProfile[] handlerProfiles) {
	this.handlerProfiles = handlerProfiles;
    }
}
