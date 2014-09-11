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
package org.universAAL.ri.gateway.eimanager.impl.registry;

import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ExportEntry;

@Deprecated
public enum EIRepoAccessManager {
    Instance;

    static {
	registry = new NonPersistentExportImportRegistry();
    }

    private static NonPersistentExportImportRegistry registry;

    public void publishExportToRepo(final ExportEntry ee) {
	registry.addExportInfo(ee);
    }

    public void removeExportFromRepo(final ExportEntry ee) {
	registry.removeExportInfo(ee);
    }

    public void publishImportToRepo(final ImportEntry ie) {
	registry.addImportInfo(ie);
    }

    public void removeImportFromRepo(final ImportEntry ie) {
	registry.removeImportInfo(ie);
    }

    public void addListener(final IRegistryListener listener) {
	registry.addListener(listener);
    }

    public void removeListener(final IRegistryListener listener) {
	registry.removeListener(listener);
    }
}
