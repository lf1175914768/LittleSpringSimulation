package com.tutorial.context.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.junit.Test;

import com.tutorial.context.NoSuchMessageException;

public class ResourceBundleMessageSourceTests {
	
	@Test
	public void testMessageAccessWithDefaultMessageSource() {
		doTestMessageAccess(false, true, false, false, false);
	}	
	
	protected void doTestMessageAccess(boolean reloadable, boolean fallbackToSystemLocale, 
			boolean expectGermanFallback, boolean useCodeDefaultMessage, boolean alwaysUseMessageFormat) {
		
	}

	@Test
	public void testResourceBundleMessageSourceStandalone() {
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("com/tutorial/context/support/messages");
		assertEquals("message1", ms.getMessage("code1", null, Locale.ENGLISH));
		assertEquals("nachricht2", ms.getMessage("code2", null, Locale.GERMAN));
	}
	
	@Test
	public void testResourceBundleMessageSourceWithWhitespaceInBasename() {
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("  com/tutorial/context/support/messages   ");
		assertEquals("message1", ms.getMessage("code1", null, Locale.ENGLISH));
		assertEquals("nachricht2", ms.getMessage("code2", null, Locale.GERMAN));
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceStandalone() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("com/tutorial/context/support/messages");
		assertEquals("message1", ms.getMessage("code1", null, Locale.ENGLISH));
		assertEquals("nachricht2", ms.getMessage("code2", null, Locale.GERMAN));
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceWithWhitespaceInBasename() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("    com/tutorial/context/support/messages   ");
		assertEquals("message1", ms.getMessage("code1", null, Locale.ENGLISH));
		assertEquals("nachricht2", ms.getMessage("code2", null, Locale.GERMAN));
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceWithDefaultCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("com/tutorial/context/support/messages");
		ms.setDefaultEncoding("ISO-8859-1");
		assertEquals("message1", ms.getMessage("code1", null, Locale.ENGLISH));
		assertEquals("nachricht2", ms.getMessage("code2", null, Locale.GERMAN));	
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceWithInappropriateDefaultCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("com/tutorial/context/support/messages");
		ms.setDefaultEncoding("unicode");
		Properties fileCharsets = new Properties();
		fileCharsets.setProperty("com/tutorial/context/support/messages_de", "unicode");
		ms.setFileEncodings(fileCharsets);
		ms.setFallbackToSystemLocale(false);
		try {
			ms.getMessage("code1", null, Locale.ENGLISH);
			fail("Should have thrown NoSuchMessageException");
		} catch (NoSuchMessageException ex) {
			// expected
		}
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceWithInappropriateEnglishCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("com/tutorial/context/support/messages");
		ms.setFallbackToSystemLocale(false);
		Properties fileCharsets = new Properties();
		fileCharsets.setProperty("com/tutorial/context/support/messages", "unicode");
		ms.setFileEncodings(fileCharsets);
		try {
			ms.getMessage("code1", null, Locale.ENGLISH);
			fail("Should have thrown NoSuchMessageException");
		}
		catch (NoSuchMessageException ex) {
			// expected
		}
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceWithInappropriateGermanCharset() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasename("com/tutorial/context/support/messages");
		ms.setFallbackToSystemLocale(false);
		Properties fileCharsets = new Properties();
		fileCharsets.setProperty("com/tutorial/context/support/messages_de", "unicode");
		ms.setFileEncodings(fileCharsets);
		assertEquals("message1", ms.getMessage("code1", null, Locale.ENGLISH));
		assertEquals("message2", ms.getMessage("code2", null, Locale.GERMAN));
	}
	
	@Test
	public void testReloadableResourceBundleMessageSourceFileNameCalculation() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		List<String> filenames = ms.calculateFilenamesForLocale("messages", Locale.ENGLISH);
		assertEquals(1, filenames.size());
		assertEquals("messages_en", filenames.get(0));
		
		filenames = ms.calculateFilenamesForLocale("messages", Locale.UK);
		assertEquals(2, filenames.size());
		assertEquals("messages_en", filenames.get(1));
		assertEquals("messages_en_GB", filenames.get(0));
		
		filenames = ms.calculateFilenamesForLocale("messages", new Locale("en", "GB", "POSIX"));
		assertEquals(3, filenames.size());
		assertEquals("messages_en", filenames.get(2));
		assertEquals("messages_en_GB", filenames.get(1));
		assertEquals("messages_en_GB_POSIX", filenames.get(0));

		filenames = ms.calculateFilenamesForLocale("messages", new Locale("en", "", "POSIX"));
		assertEquals(2, filenames.size());
		assertEquals("messages_en", filenames.get(1));
		assertEquals("messages_en__POSIX", filenames.get(0));

		filenames = ms.calculateFilenamesForLocale("messages", new Locale("", "UK", "POSIX"));
		assertEquals(2, filenames.size());
		assertEquals("messages__UK", filenames.get(1));
		assertEquals("messages__UK_POSIX", filenames.get(0));

		filenames = ms.calculateFilenamesForLocale("messages", new Locale("", "", "POSIX"));
		assertEquals(0, filenames.size());
	}

}
