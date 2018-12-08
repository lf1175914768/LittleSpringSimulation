package com.tutorial.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.junit.Test;

public class PropertiesPersisterTests {

	@Test
	public void testPropertiesPersister() throws IOException {
		String propString = "code1=message1\ncode2:message2";
		Properties props = loadProperties(propString, false);
		String propCopy = storeProperties(props, null, false);
		loadProperties(propCopy,false);
	}
	
	@Test
	public void testPropertiesPersisterwithWhitespace() throws IOException {
		String propString = " code1\t= \tmessage1\n   code2  \t : mess\\\n \t age2";
		Properties props = loadProperties(propString, false);
		String propCopy = storeProperties(props, null, false);
		loadProperties(propCopy, false);
	}
	
	@Test
	public void testPropertiesPersisterWithHeader() throws IOException {
		String propString = "code1=message1\ncode2:message2";
		Properties props = loadProperties(propString, false);
		String propCopy = storeProperties(props, "myHeader", false);
		loadProperties(propCopy, false);
	}
	
	@Test
	public void testPropertiesPersisterWithEmptyValue() throws IOException {
		String propString = "code1=message1\ncode2:message2\ncode3=";
		Properties props = loadProperties(propString, false);
		String propCopy = storeProperties(props, null, false);
		loadProperties(propCopy, false);
	}
	
	@Test
	public void testPropertiesPersisterWithReader() throws IOException {
		String propString = "code1=message1\ncode2:message2";
		Properties props = loadProperties(propString, true);
		String propCopy = storeProperties(props, null, true);
		loadProperties(propCopy, false);
	}
	@Test
	public void testPropertiesPersisterWithReaderAndWhitespace() throws IOException {
		String propString = " code1\t= \tmessage1\n  code2 \t  :\t mess\\\n \t  age2";
		Properties props = loadProperties(propString, true);
		String propCopy = storeProperties(props, null, true);
		loadProperties(propCopy, false);
	}
	@Test
	public void testPropertiesPersisterWithReaderAndHeader() throws IOException {
		String propString = "code1\t=\tmessage1\n  code2 \t  : \t message2";
		Properties props = loadProperties(propString, true);
		String propCopy = storeProperties(props, "myHeader", true);
		loadProperties(propCopy, false);
	}

	@Test
	public void testPropertiesPersisterWithReaderAndEmptyValue() throws IOException {
		String propString = "code1=message1\ncode2:message2\ncode3=";
		Properties props = loadProperties(propString, true);
		String propCopy = storeProperties(props, null, true);
		loadProperties(propCopy, false);
	}

	private String storeProperties(Properties props, String header, boolean useWriter) throws IOException {
		DefaultPropertiesPersister persister = new DefaultPropertiesPersister();
		String propCopy = null;
		if(useWriter) {
			StringWriter propWriter = new StringWriter();
			persister.store(props, propWriter, header);
			propCopy = propWriter.toString();
		} else {
			ByteArrayOutputStream propOut = new ByteArrayOutputStream();
			persister.store(props, propOut, header);
			propCopy = new String(propOut.toByteArray());
		}
		if(header != null) {
			assertTrue(propCopy.indexOf(header) != -1);
		}
		assertTrue(propCopy.indexOf("\ncode1=message1") != -1);
		assertTrue(propCopy.indexOf("\ncode2=message2") != -1);
		return propCopy;
	}

	private Properties loadProperties(String propString, boolean useReader) throws IOException {
		PropertiesPersister persister = new DefaultPropertiesPersister();
		Properties props = new Properties();
		if(useReader) {
			persister.load(props, new StringReader(propString));
		} else {
			persister.load(props, new ByteArrayInputStream(propString.getBytes()));
		}
		assertEquals("message1", props.getProperty("code1"));
		assertEquals("message2", props.getProperty("code2"));
		return props;
	}
	
}
