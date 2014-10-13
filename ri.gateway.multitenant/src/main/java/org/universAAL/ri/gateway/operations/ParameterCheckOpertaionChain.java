/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.operations;

import org.universAAL.middleware.rdf.Resource;

/**
 * Interface to Check whether a BusMember is allowed to be exported, or imported
 * using it´s params.
 * 
 * @author amedrano
 * 
 */
public interface ParameterCheckOpertaionChain extends OperationChain {

    OperationResult check(Resource[] params);

}
