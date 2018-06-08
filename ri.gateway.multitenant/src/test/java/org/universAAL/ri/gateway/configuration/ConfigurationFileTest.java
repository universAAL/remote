package org.universAAL.ri.gateway.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationFileTest {

	private static final File test_prop_file = new File("./target/test.properties");

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		test_prop_file.delete();
	}

	@Test
	public void test0FiledoesNotExist() {
		ConfigurationFile cf = new ConfigurationFile(test_prop_file);
		checkDefaults(cf, true);
	}

	@Test
	public void test1emptyFileExists() throws IOException {
		test_prop_file.delete();
		assertTrue(test_prop_file.createNewFile());
		ConfigurationFile cf = new ConfigurationFile(test_prop_file);
		checkDefaults(cf, true);
	}

	@Test
	public void test2FileExistsWithNonDefaultProperties() throws IOException {
		test_prop_file.delete();
		assertTrue(test_prop_file.createNewFile());
		PrintWriter out = new PrintWriter(test_prop_file);
		String testValue = "localhost";
		out.println(ConfigurationFile.REMOTE_HOST + "=" +testValue);
		out.flush();
		out.close();
		ConfigurationFile cf = new ConfigurationFile(test_prop_file);
		// check defaults are loaded
		checkDefaults(cf, true);
		// check value
		assertEquals(testValue, cf.getProperty(ConfigurationFile.REMOTE_HOST));
	}

	@Test
	public void test3FileExistsWithDefaultPropertiesOverriten() throws IOException {
		test_prop_file.delete();
		assertTrue(test_prop_file.createNewFile());
		PrintWriter out = new PrintWriter(test_prop_file);
		String testValue = "5";
		out.println(ConfigurationFile.QUEUES + "=" +testValue);
		out.flush();
		out.close();
		ConfigurationFile cf = new ConfigurationFile(test_prop_file);
		// check defaults are loaded
		checkDefaults(cf, false);
		// check value
		assertEquals(testValue, cf.getProperty(ConfigurationFile.QUEUES));
	}
	private void checkDefaults(ConfigurationFile cf, boolean checkValues) {

		Properties defaults = new Properties();
		cf.addDefaults(defaults);

		Set<Entry<Object, Object>> dfaultEntries = defaults.entrySet();
		for (Entry<Object, Object> e : dfaultEntries) {
			String actualValue = cf.getProperty((String)e.getKey());
			assertTrue( actualValue != null && !actualValue.isEmpty());
			if (checkValues) {
				assertEquals(e.getValue(), actualValue);
			}
		}
	}
}
