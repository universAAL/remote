/** 
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information 
 * regarding copyright ownership. The ASF licenses this file 
 * to you under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations 
 * under the License. 
 */
package org.apache.cxf.dosgi.singlebundle;

import org.apache.cxf.dosgi.dsw.Activator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/*
 * Starts the DSW in the singlebundle distribution 
 * which would normally be done by the Spring OSGi Extender
 * */
public class DSWActivator implements BundleActivator {

    private Activator dsw;

    public void start(BundleContext ctx) throws Exception {
        dsw = createActivator();
        dsw.setBundleContext(ctx);
        dsw.start();
    }

    public void stop(BundleContext ctx) throws Exception {
        dsw.stop();
    }

    // separated for test case
    protected Activator createActivator() {
        return new Activator();
    }

}
