package com.tutorial.beans.factory.support;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tutorial.core.io.ClassPathResource;

import test.beans.TestBean;

public class PropertiesBeanDefinitionReaderTests {
	
	private DefaultListableBeanFactory beanFactory;

	private PropertiesBeanDefinitionReader reader;
	
	@Before
	public void setUp() {
		this.beanFactory = new DefaultListableBeanFactory();
		this.reader = new PropertiesBeanDefinitionReader(beanFactory);
	}
	
	@Test
	public void testWithSimpleConstructorArg() {
		this.reader.loadBeanDefinitions(new ClassPathResource("simpleConstructorArg.properties", getClass()));
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		assertEquals("Rob Harrop", bean.getName());
	}

	@Test
	public void testWithConstructorArgRef() throws Exception {
		this.reader.loadBeanDefinitions(new ClassPathResource("refConstructorArg.properties", getClass()));
		TestBean rob = (TestBean)this.beanFactory.getBean("rob");
		TestBean sally = (TestBean)this.beanFactory.getBean("sally");
		assertEquals(sally, rob.getSpouse());
	}

	@Test
	public void testWithMultipleConstructorsArgs() throws Exception {
		this.reader.loadBeanDefinitions(new ClassPathResource("multiConstructorArgs.properties", getClass()));
		TestBean bean = (TestBean)this.beanFactory.getBean("testBean");
		assertEquals("Rob Harrop", bean.getName());
		assertEquals(23, bean.getAge());
	}

}
