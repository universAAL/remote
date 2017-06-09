/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.ri.api.manager.exceptions;

/**
 * Exception to be thrown by Remote API Implementation errors.
 * 
 * @author alfiva
 * 
 */
public class APIImplException extends RAPIException {

	private static final long serialVersionUID = 813977082451476624L;

	/**
	 * Constructor with message
	 * 
	 * @param msg
	 *            Error message
	 */
	public APIImplException(String msg) {
		super(msg);
	}

}
