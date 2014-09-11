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
package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;

/**
 * Interface used by callbacks that will be notified about response arrivals.
 * 
 * @author skallz
 * 
 * @deprecated
 * 
 */
@Deprecated
public interface ResponseCallback {

    /**
     * Is invoked upon arrival of the response for which this callback was
     * registered.
     * 
     * @param response
     *            the response message
     * @throws IOException
     */
    void collectResponse(Message response) throws IOException;

}
