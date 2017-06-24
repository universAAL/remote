/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid - Life Supporting Technologies
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.ri.gateway.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author amedrano
 *
 */
public abstract class UpdatedPropertiesFile extends Properties {

	/**
	 * serial version
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The properties file.
	 */
	private File propertiesFile;

	/**
	 * The last version loaded of Properties file.
	 */
	private long propertiesVersion;

	/**
	 * Get the comments for the Property file.
	 *
	 * @return
	 */
	public abstract String getComments();

	/**
	 * to be implemented to add the default values at the start if there is no
	 * file.
	 *
	 * @param defaults
	 */
	protected abstract void addDefaults(Properties defaults);

	/**
	 * Create a {@link Properties} that is linked and updated with the propfile
	 *
	 * @param propFile
	 *            the properties file
	 */
	public UpdatedPropertiesFile(File propFile) {
		propertiesFile = propFile;
	}

	/**
	 * load configuration properties from a file, setting the default for those
	 * which are not defined.
	 *
	 * @throws IOException
	 * @see HTMLUserGenerator#properties
	 */
	public void loadProperties() throws IOException {

		/*
		 * Try to load from file, if not create file from defaults.
		 */
		FileInputStream fis = null;
		if (propertiesFile != null) {
			try {
				fis = new FileInputStream(propertiesFile);
				load(fis);
				propertiesVersion = propertiesFile.lastModified();
				fis.close();
			} catch (FileNotFoundException e) {
				addDefaults(this);
				storeProperties();
				propertiesVersion = propertiesFile.lastModified();
			} finally {
				try {
					fis.close();
				} catch (Exception e2) {
				}
			}
		}
	}

	/**
	 * Checks for updates the properties file, and updates the properties.
	 *
	 * @return true if the properties have been updated.
	 */
	protected boolean checkPropertiesVersion() {
		if (propertiesFile != null && (propertiesVersion != propertiesFile.lastModified())) {
			;
			try {
				loadProperties();
				return true;
			} catch (IOException e) {
			}
		}
		return false;
	}

	/**
	 * Save the current properties in the file.
	 *
	 * @throws IOException
	 */
	protected void storeProperties() throws IOException {
		if (propertiesFile.getParentFile().exists() || propertiesFile.getParentFile().mkdirs()) {
			FileOutputStream fos = new FileOutputStream(propertiesFile);
			super.store(fos, getComments());
			fos.close();
		}
	}

	/**
	 * Access to the property file.
	 *
	 * @param string
	 *            Key of property to access
	 * @return String Value of the property
	 * @see HTMLUserGenerator#properties
	 */
	public final String getProperty(String key) {
		checkPropertiesVersion();
		return super.getProperty(key);
	}

	/**
	 * Access to the property file.
	 *
	 * @param key
	 *            Key of property to access
	 * @param defaultValue
	 *            default value for the property if isn't in the property file.
	 * @return String Value of the property.
	 * @see HTMLUserGenerator#properties
	 */
	public final String getProperty(String key, String defaultValue) {
		checkPropertiesVersion();
		return super.getProperty(key, defaultValue);
	}

	/**
	 * Update the property file.
	 *
	 * @param key
	 *            Key of property to access
	 * @param newValue
	 *            New value for the property.
	 * @return
	 * @see HTMLUserGenerator#properties
	 */
	public final Object setProperty(String key, String newValue) {
		checkPropertiesVersion();
		return super.setProperty(key, newValue);
	}

}
