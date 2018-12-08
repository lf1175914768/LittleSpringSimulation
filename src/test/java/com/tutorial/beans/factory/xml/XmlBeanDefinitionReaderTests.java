package com.tutorial.beans.factory.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import org.xml.sax.InputSource;

import com.tutorial.beans.factory.BeanDefinitionStoreException;
import com.tutorial.beans.factory.support.BeanDefinitionRegistry;
import com.tutorial.beans.factory.support.DefaultListableBeanFactory;
import com.tutorial.beans.factory.support.SimpleBeanDefinitionRegistry;
import com.tutorial.core.io.ClassPathResource;
import com.tutorial.core.io.InputStreamResource;
import com.tutorial.core.io.Resource;

import test.beans.TestBean;

public class XmlBeanDefinitionReaderTests {

	@Test
	public void testSetParserClassSunnyDay() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		new XmlBeanDefinitionReader(registry).setDocumentReaderClass(DefaultBeanDefinitionDocumentReader.class);
	}
	
	@Test
	public void testSetParserClasstoNull() {
		try {
			SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();;
			new XmlBeanDefinitionReader(registry).setDocumentReaderClass(null);
			fail("Should have thrown IllegalArgumentException (null parserClass)");
		}
		catch (IllegalArgumentException expected) {
		}
	}
	
	@Test
	public void testSetParserClasstoUnsupportedParserType() {
		try {
			SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();;
			new XmlBeanDefinitionReader(registry).setDocumentReaderClass(String.class);
			fail("Should have thrown IllegalArgumentException (unsupported parserClass)");
		}
		catch (IllegalArgumentException expected) {
		}
	}
	
	@Test
	public void testWithOpenInputStream() {
		try {
			SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();;
			Resource resource = new InputStreamResource(getClass().getResourceAsStream("test.xml"));
			new XmlBeanDefinitionReader(registry).loadBeanDefinitions(resource);
			fail("Should have thrown BeanDefinitionStoreException (can't determine validation mode)");
		}
		catch (BeanDefinitionStoreException expected) {
		}
	}
	
	@Test
	public void testWithOpenInputStreamAndExplicitValidationMode() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		Resource resource = new InputStreamResource(getClass().getResourceAsStream("test.xml"));
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_DTD);
		reader.loadBeanDefinitions(resource);
		testBeanDefinitions(registry);
	}
	
	@Test
	public void testWithImport() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		Resource resource = new ClassPathResource("import.xml", getClass());
		new XmlBeanDefinitionReader(registry).loadBeanDefinitions(resource);
		testBeanDefinitions(registry);
	}
	
	@Test
	public void testWithWildcardImport() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		Resource resource = new ClassPathResource("importPattern.xml", getClass());
		new XmlBeanDefinitionReader(registry).loadBeanDefinitions(resource);
		testBeanDefinitions(registry);
	}
	
	@Test
	public void testWithInputSource() {
		try {
			SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
			InputSource resource = new InputSource(getClass().getResourceAsStream("test.xml"));
			new XmlBeanDefinitionReader(registry).loadBeanDefinitions(resource);
			fail("Should have thrown BeanDefinitionStoreException (can't determine validation mode)");
		} catch (BeanDefinitionStoreException e) {
		}
	}
	
	@Test
	public void testWithInputSourceAndExplicitValidationMode() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		InputSource resource = new InputSource(getClass().getResourceAsStream("test.xml"));
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_DTD);
		reader.loadBeanDefinitions(resource);
		testBeanDefinitions(registry);
	}
	
	@Test
	public void testWithFreshInputStream() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		Resource resource = new ClassPathResource("test.xml", getClass());
		new XmlBeanDefinitionReader(registry).loadBeanDefinitions(resource);
		testBeanDefinitions(registry);
	}

	private void testBeanDefinitions(BeanDefinitionRegistry registry) {
		assertEquals(24, registry.getBeanDefinitionCount());
		assertEquals(24, registry.getBeanDefinitionNames().length);
		assertTrue(Arrays.asList(registry.getBeanDefinitionNames()).contains("rod"));
		assertTrue(Arrays.asList(registry.getBeanDefinitionNames()).contains("aliased"));
		assertTrue(registry.containsBeanDefinition("rod"));
		assertTrue(registry.containsBeanDefinition("aliased"));
		assertEquals(TestBean.class.getName(), registry.getBeanDefinition("rod").getBeanClassName());
		assertEquals(TestBean.class.getName(), registry.getBeanDefinition("aliased").getBeanClassName());
		assertTrue(registry.isAlias("youralias"));
		assertEquals(2, registry.getAliases("aliased").length);
		assertEquals("myalias", registry.getAliases("aliased")[1]);
		assertEquals("youralias", registry.getAliases("aliased")[0]);
	}
	
	@Test
	public void testDtdValidationAutodetect() {
		doTestValidation("validateWithDtd.xml");
	}
	
	@Test
	public void testXsdValidationAutodetect() throws Exception {
		doTestValidation("validateWithXsd.xml");
	}

	private void doTestValidation(String resourceName) {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		Resource resource = new ClassPathResource(resourceName, getClass());
		new XmlBeanDefinitionReader(factory).loadBeanDefinitions(resource);
		TestBean bean = (TestBean) factory.getBean("testBean");
		assertNotNull(bean);
	}
}
