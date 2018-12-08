package com.tutorial.beans.factory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.MutablePropertyValues;
import com.tutorial.beans.NotWritablePropertyException;
import com.tutorial.beans.PropertyEditorRegistrar;
import com.tutorial.beans.PropertyEditorRegistry;
import com.tutorial.beans.PropertyValue;
import com.tutorial.beans.TypeConverter;
import com.tutorial.beans.TypeMisMatchException;
import com.tutorial.beans.factory.config.AutowireCapableBeanFactory;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.config.BeanPostProcessor;
import com.tutorial.beans.factory.config.ConfigurableBeanFactory;
import com.tutorial.beans.factory.config.ConstructorArgumentValues;
import com.tutorial.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import com.tutorial.beans.factory.config.RuntimeBeanReference;
import com.tutorial.beans.factory.config.TypedStringValue;
import com.tutorial.beans.factory.support.AbstractBeanDefinition;
import com.tutorial.beans.factory.support.AbstractBeanFactory;
import com.tutorial.beans.factory.support.BeanDefinitionBuilder;
import com.tutorial.beans.factory.support.ChildBeanDefinition;
import com.tutorial.beans.factory.support.DefaultListableBeanFactory;
import com.tutorial.beans.factory.support.ManagedList;
import com.tutorial.beans.factory.support.PropertiesBeanDefinitionReader;
import com.tutorial.beans.factory.support.RootBeanDefinition;
import com.tutorial.beans.factory.xml.ConstructorDependenciesBean;
import com.tutorial.beans.factory.xml.DependenciesBean;
import com.tutorial.beans.propertyeditors.CustomNumberEditor;
import com.tutorial.core.MethodParameter;
import com.tutorial.core.convert.converter.Converter;
import com.tutorial.core.convert.support.DefaultConversionService;
import com.tutorial.core.convert.support.GenericConversionService;
import com.tutorial.core.io.Resource;
import com.tutorial.core.io.UrlResource;
import com.tutorial.util.StopWatch;

import test.beans.DerivedTestBean;
import test.beans.DummyFactory;
import test.beans.ITestBean;
import test.beans.LifecycleBean;
import test.beans.NestedTestBean;
import test.beans.TestBean;

public class DefaultListableBeanFactoryTests {
	
	private static final Log factoryLog = LogFactory.getLog(DefaultListableBeanFactoryTests.class);
	
	@Test
	public void testUnreferencedSingletonWasInitialized() {
		KnowsIfInstantiated.clearInstantiationRecord();
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", KnowsIfInstantiated.class.getName());
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		lbf.preInstantiateSingletons();
		assertTrue("Singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
	}
	
	@Test
	public void testLazyInitialization() {
		KnowsIfInstantiated.clearInstantiationRecord();
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", KnowsIfInstantiated.class.getName());
		p.setProperty("x1.(lazy-init)", "true");
		assertTrue("Singleton not instantaited", !KnowsIfInstantiated.wasInstantiated());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.preInstantiateSingletons();
		
		assertTrue("Singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.getBean("x1");
		assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
	}
	
	@Test
	public void testFactoryBeanDitNotCreatePrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", DummyFactory.class.getName());
		// Reset static state
		DummyFactory.reset();
		p.setProperty("x1.singleton", "false");
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		assertEquals(TestBean.class, lbf.getType("x1"));
		lbf.preInstantiateSingletons();
		
		assertTrue("prototype not implement", !DummyFactory.wasPrototypeCreated());
		lbf.getBean("x1");
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertTrue(lbf.containsBean("&x1"));
		assertTrue("prototype was instantiated", DummyFactory.wasPrototypeCreated());
	}
	
	@Test
	public void testPrototypeFactoryBeanIgnoredByNonEagerTypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", DummyFactory.class.getName());
		// reset static state.
		DummyFactory.reset();
		p.setProperty("x1.(singleton)", "false");
		p.setProperty("x1.singleton", "false");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(0, beanNames.length);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertTrue(lbf.containsBean("&x1"));
		assertFalse(lbf.isSingleton("x1"));
		assertFalse(lbf.isSingleton("&x1"));
		assertTrue(lbf.isPrototype("x1"));
		assertTrue(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertTrue(lbf.isTypeMatch("&x1", DummyFactory.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(DummyFactory.class, lbf.getType("&x1"));
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
	}
	
	@Test
	public void testPrototypeSingletonFactoryBeanIgnoredByNonEagerTypematch() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", DummyFactory.class.getName());
		// Reset static state
		DummyFactory.reset();
		p.setProperty("x1.(singleton)", "false");
		p.setProperty("x1.singleton", "true");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(0, beanNames.length);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertTrue(lbf.containsBean("&x1"));
		assertFalse(lbf.isSingleton("x1"));
		assertFalse(lbf.isSingleton("&x1"));
		assertTrue(lbf.isPrototype("x1"));
		assertTrue(lbf.isPrototype("&x1"));
		
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertTrue(lbf.isTypeMatch("&x1", DummyFactory.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(DummyFactory.class, lbf.getType("&x1"));
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
	}
	
	@Test
	public void testNonInitializedFactoryBeanIgnoredByNonEagertypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", DummyFactory.class.getName());
		// Reset static state
		DummyFactory.reset();
		p.setProperty("x1.singleton", "false");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);

		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(0, beanNames.length);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertTrue(lbf.containsBean("&x1"));
		assertFalse(lbf.isSingleton("x1"));
		assertTrue(lbf.isSingleton("&x1"));
		assertTrue(lbf.isPrototype("x1"));
		assertFalse(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertTrue(lbf.isTypeMatch("&x1", DummyFactory.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(DummyFactory.class, lbf.getType("&x1"));
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
	}
	
	@Test
	public void testInitializedFactoryBeanfoundByNonEagerTypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", DummyFactory.class.getName());
		// Reset static state
		DummyFactory.reset();
		p.setProperty("x1.singleton", "false");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		lbf.preInstantiateSingletons();

		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(1, beanNames.length);
		assertEquals("x1", beanNames[0]);
		assertTrue(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertTrue(lbf.containsBean("&x1"));
		assertTrue(lbf.containsLocalBean("x1"));
		assertTrue(lbf.containsLocalBean("&x1"));
		assertFalse(lbf.isSingleton("x1"));
		assertTrue(lbf.isSingleton("&x1"));
		assertTrue(lbf.isPrototype("x1"));
		assertFalse(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertTrue(lbf.isTypeMatch("&x1", DummyFactory.class));
		assertTrue(lbf.isTypeMatch("x1", Object.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(DummyFactory.class, lbf.getType("&x1"));
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		
		lbf.registerAlias("x1", "x2");
		assertTrue(lbf.containsBean("x2"));
		assertTrue(lbf.containsBean("&x2"));
		assertTrue(lbf.containsLocalBean("x2"));
		assertTrue(lbf.containsLocalBean("&x2"));
		assertFalse(lbf.isSingleton("x2"));
		assertTrue(lbf.isSingleton("&x2"));
		assertTrue(lbf.isPrototype("x2"));
		assertFalse(lbf.isPrototype("&x2"));
		assertTrue(lbf.isTypeMatch("x2", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x2", TestBean.class));
		assertTrue(lbf.isTypeMatch("&x2", DummyFactory.class));
		assertTrue(lbf.isTypeMatch("x2", Object.class));
		assertTrue(lbf.isTypeMatch("&x2", Object.class));
		assertEquals(TestBean.class, lbf.getType("x2"));
		assertEquals(DummyFactory.class, lbf.getType("&x2"));
		assertEquals(1, lbf.getAliases("x1").length);
		assertEquals("x2", lbf.getAliases("x1")[0]);
		assertEquals(1, lbf.getAliases("&x1").length);
		assertEquals("&x2", lbf.getAliases("&x1")[0]);
		assertEquals(1, lbf.getAliases("x2").length);
		assertEquals("x1", lbf.getAliases("x2")[0]);
		assertEquals(1, lbf.getAliases("&x2").length);
		assertEquals("&x1", lbf.getAliases("&x2")[0]);
	}
	
	@Test
	public void testStaticFactoryMethodFoundByNonEagerTypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(TestBeanFactory.class);
		rbd.setFactoryMethodName("createTestBean");
		lbf.registerBeanDefinition("x1", rbd);
		
		TestBeanFactory.initialized = false;
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(1, beanNames.length);
		assertEquals("x1", beanNames[0]);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertFalse(lbf.containsBean("&x1"));
		assertTrue(lbf.isSingleton("x1"));	
		assertFalse(lbf.isSingleton("&x1"));
		assertFalse(lbf.isPrototype("x1"));
		assertFalse(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(null, lbf.getType("&x1"));
		assertFalse(TestBeanFactory.initialized);
	}
	
	@Test
	public void testStaticPrototypeFactoryMethodFoundbyNonEagerTypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(TestBeanFactory.class);
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		rbd.setFactoryMethodName("createTestBean");
		lbf.registerBeanDefinition("x1", rbd);
		
		TestBeanFactory.initialized = false;
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(1, beanNames.length);
		assertEquals("x1", beanNames[0]);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertFalse(lbf.containsBean("&x1"));
		assertFalse(lbf.isSingleton("x1"));
		assertFalse(lbf.isSingleton("&x1"));
		assertTrue(lbf.isPrototype("x1"));
		assertFalse(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(null, lbf.getType("&x1"));
		assertFalse(TestBeanFactory.initialized);
	}

	@Test
	public void testNonStaticFactoryMethodFoundByNonEagerTypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition factoryBd = new RootBeanDefinition(TestBeanFactory.class);
		lbf.registerBeanDefinition("factory", factoryBd);
		RootBeanDefinition rbd = new RootBeanDefinition(TestBeanFactory.class);
		rbd.setFactoryBeanName("factory");
		rbd.setFactoryMethodName("createTestBeanNonStatic");
		lbf.registerBeanDefinition("x1", rbd);
		
		TestBeanFactory.initialized = false;
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(1, beanNames.length);
		assertEquals("x1", beanNames[0]);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertFalse(lbf.containsBean("&x1"));
		assertTrue(lbf.isSingleton("x1"));
		assertFalse(lbf.isSingleton("&x1"));
		assertFalse(lbf.isPrototype("x1"));
		assertFalse(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(null, lbf.getType("&x1"));
		assertFalse(TestBeanFactory.initialized);
	}
	
	@Test
	public void testNonStaticPrototypeFactoryMethodFoundByNonEagerTypeMatching() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition factoryBd = new RootBeanDefinition(TestBeanFactory.class);
		lbf.registerBeanDefinition("factory", factoryBd);
		RootBeanDefinition rbd = new RootBeanDefinition();
		rbd.setFactoryBeanName("factory");
		rbd.setFactoryMethodName("createTestBeanNonStatic");
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		lbf.registerBeanDefinition("x1", rbd);
		
		TestBeanFactory.initialized = false;
		String[] beanNames = lbf.getBeanNamesForType(TestBean.class, true, false);
		assertEquals(1, beanNames.length);
		assertEquals("x1", beanNames[0]);
		assertFalse(lbf.containsSingleton("x1"));
		assertTrue(lbf.containsBean("x1"));
		assertFalse(lbf.containsBean("&x1"));
		assertTrue(lbf.containsLocalBean("x1"));
		assertFalse(lbf.containsLocalBean("&x1"));
		assertFalse(lbf.isSingleton("x1"));
		assertFalse(lbf.isSingleton("&x1"));
		assertTrue(lbf.isPrototype("x1"));
		assertFalse(lbf.isPrototype("&x1"));
		assertTrue(lbf.isTypeMatch("x1", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x1", TestBean.class));
		assertTrue(lbf.isTypeMatch("x1", Object.class));
		assertFalse(lbf.isTypeMatch("&x1", Object.class));
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertEquals(null, lbf.getType("&x1"));
		assertFalse(TestBeanFactory.initialized);
		
		lbf.registerAlias("x1", "x2");
		assertTrue(lbf.containsBean("x2"));
		assertFalse(lbf.containsBean("&x2"));
		assertTrue(lbf.containsLocalBean("x2"));
		assertFalse(lbf.containsLocalBean("&x2"));
		assertFalse(lbf.isSingleton("x2"));
		assertFalse(lbf.isSingleton("&x2"));
		assertTrue(lbf.isPrototype("x2"));
		assertFalse(lbf.isPrototype("&x2"));
		assertTrue(lbf.isTypeMatch("x2", TestBean.class));
		assertFalse(lbf.isTypeMatch("&x2", TestBean.class));
		assertTrue(lbf.isTypeMatch("x2", Object.class));
		assertFalse(lbf.isTypeMatch("&x2", Object.class));
		assertEquals(TestBean.class, lbf.getType("x2"));
		assertEquals(null, lbf.getType("&x2"));
		assertEquals(1, lbf.getAliases("x1").length);
		assertEquals("x2", lbf.getAliases("x1")[0]);
		assertEquals(1, lbf.getAliases("&x1").length);
		assertEquals("&x2", lbf.getAliases("&x1")[0]);
		assertEquals(1, lbf.getAliases("x2").length);
		assertEquals("x1", lbf.getAliases("x2")[0]);
		assertEquals(1, lbf.getAliases("&x2").length);
		assertEquals("&x1", lbf.getAliases("&x2")[0]);
	}
	
	@Test
	public void testEmpty() {
		ListableBeanFactory lbf = new DefaultListableBeanFactory();
		assertTrue("No bean defined -> array != null", lbf.getBeanDefinitionNames() != null);
		assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionNames().length == 0);
		assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionCount() == 0);
	}
	
	@Test
	public void testEmptyPropertiesPopulation() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("NO beans defined after ignorable invalid", lbf.getBeanDefinitionCount() == 0);
	}
	
	@Test
	public void testHarmlessIgnorableRubbish() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("foo", "bar");
		p.setProperty("qwert", "er");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, "test");
		assertTrue("NO bean defined after harmless ignorable rubbish", lbf.getBeanDefinitionCount() == 0);
	}
	
	@Test
	public void testPropertiesPopulationWithNullPrefix() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.(class)", TestBean.class.getName());
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("1 beans registered, not " + count, count == 1);
		testSingleTestBean(lbf);
	}
	
	@Test
	public void testPropertiesPopulationWithPrefix() {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty(PREFIX + "test.(class)", TestBean.class.getName());
		p.setProperty(PREFIX + "test.name", "Tony");
		p.setProperty(PREFIX + "test.age", "0x30");
		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);
		assertTrue("1 beans registered, not " + count, count == 1);
		testSingleTestBean(lbf);
	}
	
	@Test
	public void testSimpleReference() {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty(PREFIX + "rod.(class)", TestBean.class.getName());
		p.setProperty(PREFIX + "rod.name", "Rod");
		p.setProperty(PREFIX + "kerry.(class)", TestBean.class.getName());
		p.setProperty(PREFIX + "kerry.name", "Kerry");
		p.setProperty(PREFIX + "kerry.age", "35");
		p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");
		
		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);
		assertTrue("2 beans registered, not " + count, count == 2);
		
		TestBean kerry = (TestBean) lbf.getBean("kerry", TestBean.class);
		assertTrue("Kerry name is Kerry", "Kerry".equals(kerry.getName()));
		ITestBean spouse = kerry.getSpouse();
		assertTrue("Kerry spouse is non null", spouse != null);
		assertTrue("Kerry spouse name is Rod", "Rod".equals(spouse.getName()));
	}
	
	@Test
	public void tesPropertiesWithDotInKey() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("tb.(class)", TestBean.class.getName());
		p.setProperty("tb.someMap[my.key]", "my.value");
		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("1 bean registered", count == 1);
		assertEquals(1, lbf.getBeanDefinitionCount());
		
		TestBean tb = (TestBean) lbf.getBean("tb", TestBean.class);
		assertEquals("my.value", tb.getSomeMap().get("my.key"));
	}
	
	@Test
	public void testUnresolvedReference() {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();

		try {
			p.setProperty(PREFIX + "kerry.(class)", TestBean.class.getName());
			p.setProperty(PREFIX + "kerry.name", "Kerry");
			p.setProperty(PREFIX + "kerry.age", "35");
			p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");

			(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);

			lbf.getBean("kerry");
			fail("Unresolved reference should have been detected");
		}
		catch (BeansException ex) {
			// cool
		}
	}
	
//	@SuppressWarnings("deprecation")
//	@Test
//	public void testSelfReference() {
//		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
//		MutablePropertyValues pvs = new MutablePropertyValues();
//		pvs.add("spouse", new RuntimeBeanReference("self"));
//		lbf.registerBeanDefinition("self", new RootBeanDefinition(TestBean.class, pvs));
//		TestBean self = (TestBean) lbf.getBean("self");
//		assertEquals(self, self.getSpouse());
//	}
	
	@Test
	public void testPossibleMatches() {
		try {
			DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.add("ag", "foobar");
			lbf.registerBeanDefinition("tb", new RootBeanDefinition(TestBean.class, pvs));
			lbf.getBean("tb");
			fail("should throw exception on invalid property");
		} catch (BeanCreationException e) {
			//e.printStackTrace();
			assertTrue(e.getCause() instanceof NotWritablePropertyException);
			NotWritablePropertyException cause = (NotWritablePropertyException) e.getCause();
			// expected
			assertEquals(1, cause.getPossibleMatches().length);
			assertEquals("age", cause.getPossibleMatches()[0]);
		}
	}
	
	@Test
	public void testPrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.(class)", TestBean.class.getName());
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean kerry1 = (TestBean) lbf.getBean("kerry");
		TestBean kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("non null", kerry1 != null);
		assertTrue("Singletons equals", kerry1 == kerry2);
		
		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.(class)", TestBean.class.getName());
		p.setProperty("kerry.(scope)", "prototype");
		p.setProperty("kerry.age", "35");		
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Prototypes NOT equal", kerry1 != kerry2);
		
		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.(class)", TestBean.class.getName());
		p.setProperty("kerry.(scope)", "singleton");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Specified singletons equal", kerry1 == kerry2);
	}
	
	@Test
	public void testPrototypeCircleLeadsToException() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.(class)", TestBean.class.getName());
		p.setProperty("kerry.(singleton)", "false");
		p.setProperty("kerry.age", "53");
		p.setProperty("kerry.spouse", "*rod");
		p.setProperty("rod.(class)", TestBean.class.getName());
		p.setProperty("rod.(singleton)", "false");
		p.setProperty("rod.age", "34");
		p.setProperty("rod.spouse", "*kerry");
		
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		try {
			lbf.getBean("kerry");
			fail("should have thrown BeanCreationException");
		} catch(BeanCreationException ex) {
			assertTrue(ex.contains(BeanCurrentlyInCreationException.class));
		}
	}
	
	@Test
	public void testPropertyExtendsPrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("wife.(class)", TestBean.class.getName());
		p.setProperty("wife.name", "kerry");
		
		p.setProperty("kerry.(parent)", "wife");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean kerry1 = (TestBean) lbf.getBean("kerry");
		TestBean kerry2 = (TestBean) lbf.getBean("kerry");
		assertEquals("kerry", kerry1.getName());
		assertNotNull("non null", kerry1);
		assertTrue("Singletons equals", kerry1 == kerry2);
		
		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("wife.(class)", TestBean.class.getName());
		p.setProperty("wife.name", "kerry");
		p.setProperty("wife.(singleton)", "false");
		p.setProperty("kerry.(parent)", "wife");
		p.setProperty("kerry.(singleton)", "false");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertFalse(lbf.isSingleton("kerry"));
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Prototypes NOT equal", kerry1 != kerry2);
		
		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.(class)", TestBean.class.getName());
		p.setProperty("kerry.(singleton)", "true");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Specified singletons equal", kerry1 == kerry2);
	}
	
	@Test
	public void testCanReferenceParentBeanFromChildViaAlias() {
		final String EXPECTED_NAME = "Juergen";
		final int EXPECTED_AGE = 41;
		
		RootBeanDefinition parentDefinition = new RootBeanDefinition(TestBean.class);
		parentDefinition.setAbstract(true);
		parentDefinition.getPropertyValues().add("name", EXPECTED_NAME);
		parentDefinition.getPropertyValues().add("age", new Integer(EXPECTED_AGE));
		
		ChildBeanDefinition childDefinition = new ChildBeanDefinition("alias");
		
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("parent", parentDefinition);
		factory.registerBeanDefinition("child", childDefinition);
		factory.registerAlias("parent", "alias");
		
		TestBean child = (TestBean) factory.getBean("child");
		assertEquals(EXPECTED_NAME, child.getName());
		assertEquals(EXPECTED_AGE, child.getAge());
		
		assertEquals("use cached merged bean definition", factory.getMergedBeanDefinition("child"), 
				factory.getMergedBeanDefinition("child"));
	}
	
	@Test
	public void testNameAlreadyBound() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.(class)", TestBean.class.getName());
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		try {
			(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		} catch(BeanDefinitionStoreException ex) {
			
		}
	}
	
	@Test
	public void testAliasCircle() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerAlias("test", "test2");
		lbf.registerAlias("test2", "test3");
		try {
			lbf.registerAlias("test3", "test");
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}
	
	@Test
	public void testBeanDefinitionOverriding() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
		lbf.registerAlias("otherTest", "test2");
		lbf.registerAlias("test", "test2");
		assertTrue(lbf.getBean("test") instanceof NestedTestBean);
		assertTrue(lbf.getBean("test2") instanceof NestedTestBean);
	}
	
	@Test
	public void testBeanDefinitionRemoval() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.setAllowBeanDefinitionOverriding(true);
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		lbf.registerAlias("test", "test2");
		lbf.preInstantiateSingletons();
		lbf.removeBeanDefinition("test");
		lbf.removeAlias("test2");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
		lbf.registerAlias("test", "test2");
		assertTrue(lbf.getBean("test") instanceof NestedTestBean);
		assertTrue(lbf.getBean("test2") instanceof NestedTestBean);
	}
	
	@Test
	public void testBeanDefinitionOverridingNotAllowed() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.setAllowBeanDefinitionOverriding(false);
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		try {
			lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			assertEquals("test", ex.getBeanName());
			// expected
		}
	}
	
	private void testSingleTestBean(ListableBeanFactory lbf) {
		assertTrue("1 bean defined", lbf.getBeanDefinitionCount() == 1);
		String[] names = lbf.getBeanDefinitionNames();
		assertTrue("array length == 1", names.length == 1);
		assertTrue("0th element == test", names[0].equals("test"));
		TestBean tb = (TestBean) lbf.getBean("test");
		assertTrue("Test is non null", tb != null);
		assertTrue("Test bean name is Tony", "Tony".equals(tb.getName()));
		assertTrue("Test bean age is 48", tb.getAge() == 48);
	}
	
	@Test
	public void testBeanDefinitionOverridingWithAlias() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		lbf.registerAlias("test", "testAlias"); 
		lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
		lbf.registerAlias("test", "testAlias");
		assertTrue(lbf.getBean("test") instanceof NestedTestBean);
		assertTrue(lbf.getBean("testAlias") instanceof NestedTestBean);
	}
	
	@Test
	public void testAliasChaining() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
		lbf.registerAlias("test", "testAlias");
		lbf.registerAlias("testAlias", "testAlias2");
		lbf.registerAlias("testAlias2", "testAlias3");
		Object bean = lbf.getBean("test");
		assertSame(bean, lbf.getBean("testAlias"));
		assertSame(bean, lbf.getBean("testAlias2"));
		assertSame(bean, lbf.getBean("testAlias3"));
	}
	
	@Test
	public void testBeanReferenceWithNewSyntax() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("r.(class)", TestBean.class.getName());
		p.setProperty("r.name", "rod");
		p.setProperty("k.(class)", TestBean.class.getName());
		p.setProperty("k.name", "kerry");
		p.setProperty("k.spouse", "*r");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean k = (TestBean) lbf.getBean("k");
		TestBean r = (TestBean) lbf.getBean("r");
		assertTrue(k.getSpouse() == r);
	}
	
	@Test
	public void testCanEscapeBeanReferenceSyntax() {
		String name = "*name";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("r.(class)", TestBean.class.getName());
		p.setProperty("r.name", "*" + name);
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean r = (TestBean) lbf.getBean("r");
		assertTrue(r.getName().equals(name));
	}
	
	@Test
	public void testCustomEditor() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.addPropertyEditorRegistrar(new PropertyEditorRegistrar() {
			public void registerCustomEditors(PropertyEditorRegistry registry) {
				NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
				registry.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, nf, true));
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1, 1");
		lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, pvs));
		TestBean testBean = (TestBean) lbf.getBean("testBean");
		assertTrue(testBean.getMyFloat().floatValue() == 1.1f);
	}
	
	@Test
	public void testCustomConverter() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		GenericConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new Converter<String, Float>() {
			public Float convert(String source) {
				try {
					NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
					return nf.parse(source).floatValue();
				} catch (ParseException e) {
					throw new IllegalArgumentException(e);				}
			}
		});
		lbf.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1,1");
		lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, pvs));
		TestBean test = (TestBean) lbf.getBean("testBean");
		assertTrue(test.getMyFloat().floatValue() == 1.1f);
	}
	
	@Test
	public void testCustomEditorWithBeanReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.addPropertyEditorRegistrar(new PropertyEditorRegistrar() {
			public void registerCustomEditors(PropertyEditorRegistry registry) {
				NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
				registry.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, nf, true));
			}
		});
		MutablePropertyValues pvs = new  MutablePropertyValues();
		pvs.add("myFloat", new RuntimeBeanReference("myFloat"));
		lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, pvs));
		lbf.registerSingleton("myFloat", "1,1");
		TestBean testBean = (TestBean) lbf.getBean("testBean");
		assertTrue(testBean.getMyFloat().floatValue() == 1.1f);
	}
	
	@Test
	public void testCustomTypeConverter() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
		lbf.setTypeConverter(new CustomTypeConverter(nf));
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1,1");
		ConstructorArgumentValues cav = new ConstructorArgumentValues();
		cav.addIndexedArgumentValue(0, "myName");
		cav.addIndexedArgumentValue(1, "myAge");
		lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, cav, pvs));
		TestBean testBean = (TestBean) lbf.getBean("testBean");
		assertEquals("myName", testBean.getName());
		assertEquals(5, testBean.getAge());
		assertTrue(testBean.getMyFloat().floatValue() == 1.1f);
	}

	@Test
	public void testCustomTypeConverterWithBeanReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
		lbf.setTypeConverter(new CustomTypeConverter(nf));
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", new RuntimeBeanReference("myFloat"));
		ConstructorArgumentValues cav = new ConstructorArgumentValues();
		cav.addIndexedArgumentValue(0, "myName");
		cav.addIndexedArgumentValue(1, "myAge");
		lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, cav, pvs));
		lbf.registerSingleton("myFloat", "1,1");
		TestBean testBean = (TestBean) lbf.getBean("testBean");
		assertEquals("myName", testBean.getName());
		assertEquals(5, testBean.getAge());
		assertTrue(testBean.getMyFloat().floatValue() == 1.1f);
	}
	
	@Test
	public void testRegisterExistingSingletonWithReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.(class)", TestBean.class.getName());
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		p.setProperty("test.spouse(ref)", "singletonObject");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		
		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(TestBean.class, lbf.getType("singletonObject"));
		TestBean testBean = (TestBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, testBean.getSpouse());
		
		Map<?, ?> beanOfTypes = lbf.getBeansOfType(TestBean.class, true, false);
		assertEquals(2, beanOfTypes.size());
		assertTrue(beanOfTypes.containsValue(testBean));
		assertTrue(beanOfTypes.containsValue(singletonObject));
		
		beanOfTypes = lbf.getBeansOfType(null, false, true);
		assertEquals(2, beanOfTypes.size());
	}
	
	@Test
	public void testRegisterExistingSingletonWithNameOverriding() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.(class)", TestBean.class.getName());
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		p.setProperty("test.spouse(ref)", "singletonObject");
		p.setProperty("singletonObject.(class)", com.tutorial.beans.factory.config.PropertiesFactoryBean.class.getName());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		lbf.preInstantiateSingletons();
		
		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(TestBean.class, lbf.getType("singletonObject"));
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, test.getSpouse());
		
		Map<?, ?> beansOfType = lbf.getBeansOfType(TestBean.class, false, true);
		assertEquals(2, beansOfType.size());
		assertTrue(beansOfType.containsValue(test));
		assertTrue(beansOfType.containsValue(singletonObject));
		beansOfType = lbf.getBeansOfType(null, false, true);
		assertEquals(2, beansOfType.size());
	}
	
	@Test
	public void testRegisterExistingSingletonWithAutowire() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Tony");
		pvs.add("age", "48");
		RootBeanDefinition mbd = new RootBeanDefinition(DependenciesBean.class, pvs);
		mbd.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CEHCK_SIMPLE);
		mbd.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
		lbf.registerBeanDefinition("test", mbd);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		
		assertTrue(lbf.containsBean("singletonObject"));
		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(TestBean.class, lbf.getType("singletonObject"));
		assertEquals(0, lbf.getAliases("singletonObject").length);
		DependenciesBean test = (DependenciesBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, test.getSpouse());
	}
	
	@Test
	public void testRegisterExistingSingletonWithAlreadyBound() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		try {
			lbf.registerSingleton("singletonObject", singletonObject);
			fail("should have thrown IllegalStateException");
		} catch(IllegalStateException e) {
			//throw e;
		}
	}
	
	@Test
	public void testReregisterBeanDefinition() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd1 = new RootBeanDefinition(TestBean.class);
		bd1.setScope(RootBeanDefinition.SCOPE_PROTOTYPE); 
		lbf.registerBeanDefinition("testBean", bd1);
		assertTrue(lbf.getBean("testBean") instanceof TestBean);
		RootBeanDefinition bd2 = new RootBeanDefinition(NestedTestBean.class);
		bd2.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		lbf.registerBeanDefinition("testBean", bd2);
		assertTrue(lbf.getBean("testBean" ) instanceof NestedTestBean);
	}
	
	@Test
	public void testArrayPropertyWithAutowiring() throws MalformedURLException {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerSingleton("resource1", new UrlResource("http://localhost:8080"));
		lbf.registerSingleton("resource2", new UrlResource("http://localhost:9090"));
		
		RootBeanDefinition rbd = new RootBeanDefinition(ArrayBean.class, RootBeanDefinition.AUTOWIRE_BY_TYPE);
		lbf.registerBeanDefinition("arrayBean", rbd);
		ArrayBean ab = (ArrayBean) lbf.getBean("arrayBean");
		
		assertEquals(new UrlResource("http://localhost:8080"), ab.getResourceArray()[0]);
		assertEquals(new UrlResource("http://localhost:9090"), ab.getResourceArray()[1]);
	}
	
	@Test
	public void testArrayPropertyWithOptionalAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(ArrayBean.class, RootBeanDefinition.AUTOWIRE_BY_TYPE);
		lbf.registerBeanDefinition("arrayBean", rbd);
		ArrayBean ab = (ArrayBean) lbf.getBean("arrayBean");
		assertNull(ab.getResourceArray());
	}
	
	@Test
	public void testArrayConstructorWithAutowiring() {
		DefaultListableBeanFactory lbf= new DefaultListableBeanFactory();
		lbf.registerSingleton("integer1", new Integer(4));
		lbf.registerSingleton("integer2", new Integer(5));
		
		RootBeanDefinition bd = new RootBeanDefinition(ArrayBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		lbf.registerBeanDefinition("arrayBean", bd);
		ArrayBean ab = (ArrayBean) lbf.getBean("arrayBean");
		assertEquals(new Integer(4), ab.getIntegerArray()[0]);
		assertEquals(new Integer(5), ab.getIntegerArray()[1]);
	}
	
	@Test
	public void testArrayConstructorWithOptionalAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(ArrayBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		lbf.registerBeanDefinition("arrayBean", bd);
		ArrayBean ab = (ArrayBean) lbf.getBean("arrayBean");
		assertNull(ab.getIntegerArray());
	}
	
	@Test
	public void testDoubleArrayConstructorWithAutowiring() throws IllegalStateException, MalformedURLException {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerSingleton("integer1", new Integer(4));
		lbf.registerSingleton("integer2", new Integer(5));
		lbf.registerSingleton("resource1", new UrlResource("http://localhost:8080"));
		lbf.registerSingleton("resource2", new UrlResource("http://localhost:9090"));
		
		RootBeanDefinition bd = new RootBeanDefinition(ArrayBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		lbf.registerBeanDefinition("arrayBean", bd);
		ArrayBean ab = (ArrayBean) lbf.getBean("arrayBean");
		
		assertEquals(new Integer(4), ab.getIntegerArray()[0]);
		assertEquals(new Integer(5), ab.getIntegerArray()[1]);
		assertEquals(new UrlResource("http://localhost:8080"), ab.getResourceArray()[0]);
		assertEquals(new UrlResource("http://localhost:9090"), ab.getResourceArray()[1]);
	}

	@Test
	public void testAutowireWithNoDependencies() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");	
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		// Depends on age , name and spouse (TestBean);
		Object registered = lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
		assertEquals(1, lbf.getBeanDefinitionCount());
		DependenciesBean kerry = (DependenciesBean) registered;
		TestBean rod = (TestBean) lbf.getBean("rod");
		assertSame(rod, kerry.getSpouse());
	}
	
	@Test
	public void testAutowireWithStaticfiedConstructorDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		Object registered = lbf.autowire(ConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
		assertEquals(1, lbf.getBeanDefinitionCount());
		ConstructorDependency kerry = (ConstructorDependency) registered;
		TestBean rod = (TestBean) lbf.getBean("rod");
		assertSame(rod, kerry.spouse);
	}
	
	@Test
	public void testAutowireWithTwoMatchesForConstructorDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("rod", bd);
		RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("rod2", bd2);
		try {
			lbf.autowire(ConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
			fail("should have thrown UnsatisfiedDependencyException");
		} catch(UnsatisfiedDependencyException ex) {
			assertTrue(ex.getMessage().indexOf("rod") != -1);
			assertTrue(ex.getMessage().indexOf("rod2") != -1);
		}
	}
	
	@Test
	public void testAutowireWithUnsatisfiedConstructorDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		try {
			lbf.autowire(UnsatisfiedConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
			fail("Should have unsatisfied constructor dependency on SideEffectBean");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}
	
	@Test
	public void testAutowireConstructor() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spouse", bd);
		ConstructorDependenciesBean bean = (ConstructorDependenciesBean) 
				lbf.autowire(ConstructorDependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
		Object spouse = lbf.getBean("spouse");
		assertTrue(bean.getSpouse1() == spouse);
		assertTrue(BeanFactoryUtils.beanOfType(lbf, TestBean.class) == spouse);
	}
	
	@Test
	public void testAutowireBeanByName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spouse", bd);
		DependenciesBean bean = (DependenciesBean) 
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		assertEquals(spouse, bean.getSpouse());
	}
	
	@Test
	public void testAutowireBeanByNameWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spous", bd);
		try {
			lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
			fail("should have thrown UnsatisfiedDependencyException");
		} catch(UnsatisfiedDependencyException ex) {
			
		}
	}
	
	@Test
	public void testAutowireBeanByNameWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spous", bd);
		DependenciesBean bean = (DependenciesBean) lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		assertNull(bean.getSpouse());
	}
	
	@Test(expected=NoSuchBeanDefinitionException.class)
	public void testGetBeanByTypeWithAmbiguity() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd1 = new RootBeanDefinition(TestBean.class);
		RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("bd1", bd1);
		lbf.registerBeanDefinition("bd2", bd2);
		lbf.getBean(TestBean.class);
	}
	
	@Test
	public void testGetBeanByTypeFilterOutNonAutowireCandidates() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd1 = new RootBeanDefinition(TestBean.class);
		RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class);
		RootBeanDefinition na1 = new RootBeanDefinition(TestBean.class);
		na1.setAutowireCandidate(false);
		
		lbf.registerBeanDefinition("bd1", bd1);
		lbf.registerBeanDefinition("na1", na1);
		TestBean actual = lbf.getBean(TestBean.class);
		assertSame(lbf.getBean("bd1", TestBean.class), actual);
		lbf.registerBeanDefinition("bd2", bd2);
		try {
			lbf.getBean(TestBean.class);
			fail("should thrown Exception");
		} catch(NoSuchBeanDefinitionException e) {
			
		}
	}
	
	@Test
	public void testAutowireBeanByType() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("test", bd);
		DependenciesBean bean = (DependenciesBean) 
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(test, bean.getSpouse());
	}
	
	@Test
	public void testAutowireBeanWithFactoryBeanByType() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(LazyInitFactory.class);
		lbf.registerBeanDefinition("factoryBean", bd);
		LazyInitFactory factoryBean = (LazyInitFactory) lbf.getBean("&factoryBean");
		assertNotNull("The factory bean should have been registered", factoryBean);
		FactoryBeanDependentBean bean = (FactoryBeanDependentBean) lbf.autowire(FactoryBeanDependentBean.class, 
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		assertEquals("The FactoryBeanDependentBean should have been autowired 'by type' with the LazyInitFactory.",
				factoryBean, bean.getFactoryBean());
	}
	
	@Test
	public void testGetTypeForAbstractFactoryBean() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(FactoryBeanThatShouldntBeCalled.class);
		bd.setAbstract(true);
		lbf.registerBeanDefinition("factoryBean", bd);
		assertNull(lbf.getType("factoryBean"));
	}
	
	@Test(expected=TypeMisMatchException.class)
	public void testAutowireBeanWithFactoryBeanByName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(LazyInitFactory.class);
		lbf.registerBeanDefinition("factoryBean", bd);
		LazyInitFactory factoryBean = (LazyInitFactory) lbf.getBean("&factoryBean");
		assertNotNull(factoryBean);
		lbf.autowire(FactoryBeanDependentBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
	}
	
	@Test
	public void testAutowireBeanByTypeWithTwoMatches() {
		DefaultListableBeanFactory lbf= new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("test", bd);
		lbf.registerBeanDefinition("spouse", bd2);
		try {
			lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			fail("Should have thrown UnsatisfiedDependencyException"); 
		} catch(UnsatisfiedDependencyException e) {
			
		}
	}
	
	@Test
	public void testAutowireBeanByTypeWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		try {
			lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}
	
	@Test
	public void testAutowireBeanByTypeWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		DependenciesBean bean = (DependenciesBean)
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		assertNull(bean.getSpouse());
	}
	
	@Test
	public void testAutowireExistingBeanByName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spouse", bd);
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		assertEquals(existingBean.getSpouse(), spouse);
		assertSame(spouse, BeanFactoryUtils.beanOfType(lbf, TestBean.class));
	}
	
	@Test
	public void testAutowireExistingBeanByNameWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spous", bd);
		DependenciesBean existingBean = new DependenciesBean();
		try {
			lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}

	@Test
	public void testAutowireExistingBeanByNameWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spous", bd);
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		assertNull(existingBean.getSpouse());
	}
	
	@Test
	public void testAutowireExistingBeanByType() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("test", bd);
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(existingBean.getSpouse(), test);
	}
	
	@Test
	public void testAutowireExistingBeanByTypeWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		DependenciesBean existingBean = new DependenciesBean();
		try {
			lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}
	
	@Test
	public void testAutowireExistingBeanByTypeWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		assertNull(existingBean.getSpouse());
	}
	
	@Test
	public void testInvalidAutowireMode() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		try {
			lbf.autowireBeanProperties(new TestBean(), AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
		}
	}
	
	@Test
	public void testApplyBeanPropertyValues() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, pvs));
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());
		lbf.applyBeanPropertyValues(tb, "test");
		assertEquals(99, tb.getAge());
	}
	
	@Test
	public void testApplyBeanPropertyValuesWithInCompleteDefinition() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(null, pvs));
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());
		lbf.applyBeanPropertyValues(tb, "test");
		assertEquals(99, tb.getAge());
		assertNull(tb.getBeanFactory());
		assertNull(tb.getSpouse());
	}
	
	@Test
	public void testConfigureBean() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, pvs));
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());
		lbf.configureBean(tb, "test");
		assertEquals(99, tb.getAge());
		assertSame(lbf, tb.getBeanFactory());
		assertNull(tb.getSpouse());
	}
	
	@Test
	public void testConfigureBeanWithAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("spouse", bd);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, RootBeanDefinition.AUTOWIRE_BY_NAME));
		TestBean tb = new TestBean();
		lbf.configureBean(tb, "test");
		assertSame(lbf, tb.getBeanFactory());
		TestBean spouse = (TestBean) lbf.getBean("spouse");
	}
	
	@Test
	public void testExtensiveCircularReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		for(int i = 0; i < 1000; i++) {
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("spouse", new RuntimeBeanReference("bean" + (i < 99 ? i + 1 : 0))));
			RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
			lbf.registerBeanDefinition("bean" + i, bd);
		}
		lbf.preInstantiateSingletons();
		for(int i = 0; i < 1000; i++) {
			TestBean bean = (TestBean) lbf.getBean("bean" + i);
			TestBean otherBean = (TestBean) lbf.getBean("bean" + (i < 99 ? i + 1 : 0));
			assertTrue(bean.getSpouse() == otherBean);
		}
	}
	
	@Test
	public void testCircularReferenceThroughAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		try {
			lbf.preInstantiateSingletons();
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}
	
	@Test
	public void testCircularReferenceThroughFactoryBeanAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyFactoryBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		try {
			lbf.preInstantiateSingletons();
			fail("should have thrown exception"); 
		} catch(UnsatisfiedDependencyException e) {
			throw e;
		}
	}
	
	@Test
	public void testCircularReferenceThroughFactoryBeanTypeCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyFactoryBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		try {
			lbf.getBeansOfType(String.class);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}

	@Test
	public void testAvoidCircularReferenceThroughAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyFactoryBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		lbf.registerBeanDefinition("string",
				new RootBeanDefinition(String.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		lbf.preInstantiateSingletons();
	}
	
	@Test
	public void testBeanDefinitionWithInterface() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ITestBean.class));
		try {
			lbf.getBean("test");
		} catch(BeanCreationException e) {
		}
	}
	
	@Test
	public void testBeanDefinitionWithAbstractClass() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(AbstractBeanFactory.class));
		try {
			lbf.getBean("test");
			
		} catch(BeanCreationException e) {
			
		}
	}
	
	@Test
	public void testPrototypeFactoryBeanNotEagerlyCalled() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(FactoryBeanThatShouldntBeCalled.class));
		lbf.preInstantiateSingletons();
	}

	@Test
	public void testLazyInitFactory() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(LazyInitFactory.class));
		lbf.preInstantiateSingletons();
		LazyInitFactory factory = (LazyInitFactory) lbf.getBean("&test");
		assertFalse(factory.initialized);
	}

	@Test
	public void testSmartInitFactory() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(EagerInitFactory.class));
		lbf.preInstantiateSingletons();
		EagerInitFactory factory = (EagerInitFactory) lbf.getBean("&test");
		assertTrue(factory.initialized);
	}

	@Test
	public void testPrototypeFactoryBeanNotEagerlyCalledInCaseOfBeanClassName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test",
				new RootBeanDefinition(FactoryBeanThatShouldntBeCalled.class.getName(), null, null));
		lbf.preInstantiateSingletons();
	}

	@Test
	public void testPrototypeStringCreatedRepeatedly() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition stringDef = new RootBeanDefinition(String.class);
		stringDef.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		stringDef.getConstructorArgumentValues().addGenericArgumentValue(new TypedStringValue("value"));
		lbf.registerBeanDefinition("string", stringDef);
		String val1 = lbf.getBean("string", String.class);
		String val2 = lbf.getBean("string", String.class);
		assertEquals("value", val1);
		assertEquals("value", val2);
		assertNotSame(val1, val2);
	}
	
	@Test
	public void testPrototypeWithArrayConversionForConstructor() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		List<String> list = new ManagedList<String>();
		list.add("myName");
		list.add("myBeanName");
		RootBeanDefinition bd = new RootBeanDefinition(DerivedTestBean.class);
		bd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		bd.getConstructorArgumentValues().addGenericArgumentValue(list);
		lbf.registerBeanDefinition("test", bd);
		DerivedTestBean tb = (DerivedTestBean) lbf.getBean("test");
		assertEquals("myName", tb.getName());
		assertEquals("myBeanName", tb.getBeanName());
		DerivedTestBean tb2 = (DerivedTestBean) lbf.getBean("test");
		assertTrue(tb != tb2);
		assertEquals("myName", tb2.getName());
		assertEquals("myBeanName", tb2.getBeanName());
	}
	
	@Test
	public void testPrototypeWithArrayConversionForFactoryMethod() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		List<String> list = new ManagedList<String>();
		list.add("myName");
		list.add("myBeanName");
		RootBeanDefinition bd = new RootBeanDefinition(DerivedTestBean.class);
		bd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		bd.setFactoryMethodName("create");
		bd.getConstructorArgumentValues().addGenericArgumentValue(list);
		lbf.registerBeanDefinition("test", bd);
		DerivedTestBean tb = (DerivedTestBean) lbf.getBean("test");
		assertEquals("myName", tb.getName());
		assertEquals("myBeanName", tb.getBeanName());
		DerivedTestBean tb2 = (DerivedTestBean) lbf.getBean("test");
		assertTrue(tb != tb2);
		assertEquals("myName", tb2.getName());
		assertEquals("myBeanName", tb2.getBeanName());
	}
	
	@Test
	public void testPrototypeCreationIsFastEnough() {
		if (factoryLog.isTraceEnabled() || factoryLog.isDebugEnabled()) {
			// Skip this test: Trace logging blows the time limit.
			return;
		}
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(TestBean.class);
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		lbf.registerBeanDefinition("test", rbd);
		StopWatch sw = new StopWatch();
		sw.start("prototype");
		for (int i = 0; i < 100000; i++) {
			lbf.getBean("test");
		}
		sw.stop();
		// System.out.println(sw.getTotalTimeMillis());
		assertTrue("Prototype creation took too long: " + sw.getTotalTimeMillis(), sw.getTotalTimeMillis() < 3000);
	}

	@Test
	public void testPrototypeCreationWithDependencyCheckIsFastEnough() {
		if (factoryLog.isTraceEnabled() || factoryLog.isDebugEnabled()) {
			// Skip this test: Trace logging blows the time limit.
			return;
		}
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(LifecycleBean.class);
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		rbd.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		lbf.registerBeanDefinition("test", rbd);
		lbf.addBeanPostProcessor(new LifecycleBean.PostProcessor());
		StopWatch sw = new StopWatch();
		sw.start("prototype");
		for (int i = 0; i < 100000; i++) {
			lbf.getBean("test");
		}
		sw.stop();
		// System.out.println(sw.getTotalTimeMillis());
		assertTrue("Prototype creation took too long: " + sw.getTotalTimeMillis(), sw.getTotalTimeMillis() < 3000);
	}
	
	@Test
	public void testPrototypeCreationWithResolvedConstructorArgumentsIsFastEnough() {
		if (factoryLog.isTraceEnabled() || factoryLog.isDebugEnabled()) {
			// Skip this test: Trace logging blows the time limit.
			return;
		}
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(TestBean.class);
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		rbd.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("spouse"));
		lbf.registerBeanDefinition("test", rbd);
		lbf.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class));
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		StopWatch sw = new StopWatch();
		sw.start("prototype");
		for (int i = 0; i < 100000; i++) {
			TestBean tb = (TestBean) lbf.getBean("test");
			assertSame(spouse, tb.getSpouse());
		}
		sw.stop();
		// System.out.println(sw.getTotalTimeMillis());
		assertTrue("Prototype creation took too long: " + sw.getTotalTimeMillis(), sw.getTotalTimeMillis() < 4000);
	}
	
	@Test
	public void testPrototypeCreationWithPropertiesIsFastEnough() {
		if (factoryLog.isTraceEnabled() || factoryLog.isDebugEnabled()) {
			// Skip this test: Trace logging blows the time limit.
			return;
		}
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(TestBean.class);
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		rbd.getPropertyValues().add("name", "juergen");
		rbd.getPropertyValues().add("age", "99");
		lbf.registerBeanDefinition("test", rbd);
		StopWatch sw = new StopWatch();
		sw.start("prototype");
		for (int i = 0; i < 100000; i++) {
			TestBean tb = (TestBean) lbf.getBean("test");
			assertEquals("juergen", tb.getName());
			assertEquals(99, tb.getAge());
		}
		sw.stop();
		// System.out.println(sw.getTotalTimeMillis());
		assertTrue("Prototype creation took too long: " + sw.getTotalTimeMillis(), sw.getTotalTimeMillis() < 3000);
	}
	
	@Test
	public void testPrototypeCreationWithResolvedPropertiesIsFastEnough() {
		if (factoryLog.isTraceEnabled() || factoryLog.isDebugEnabled()) {
			// Skip this test: Trace logging blows the time limit.
			return;
		}
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition rbd = new RootBeanDefinition(TestBean.class);
		rbd.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		rbd.getPropertyValues().add("spouse", new RuntimeBeanReference("spouse"));
		lbf.registerBeanDefinition("test", rbd);
		lbf.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class));
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		StopWatch sw = new StopWatch();
		sw.start("prototype");
		for (int i = 0; i < 100000; i++) {
			TestBean tb = (TestBean) lbf.getBean("test");
			assertSame(spouse, tb.getSpouse());
		}
		sw.stop();
		// System.out.println(sw.getTotalTimeMillis());
		assertTrue("Prototype creation took too long: " + sw.getTotalTimeMillis(), sw.getTotalTimeMillis() < 4000);
	}

	@Test
	public void testBeanPostProcessorWithWrappedObjectAndDisposableBean() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(BeanWithDisposableBean.class);
		lbf.registerBeanDefinition("test", bd);
		lbf.addBeanPostProcessor(new BeanPostProcessor() {
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
				return new TestBean();
			}

			public Object postProcessAfterInitialization(Object bean, String beanName) {
				return bean;
			}
		});
		BeanWithDisposableBean.closed = false;
		lbf.preInstantiateSingletons();
		lbf.destroySingletons();
		assertTrue("Destroy method invoked", BeanWithDisposableBean.closed);
	}

	@Test
	public void testBeanPostProcessorWithWrappedObjectAndDestroyMethod() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(BeanWithDestroyMethod.class);
		bd.setDestroyMethodName("close");
		lbf.registerBeanDefinition("test", bd);
		lbf.addBeanPostProcessor(new BeanPostProcessor() {
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
				return new TestBean();
			}

			public Object postProcessAfterInitialization(Object bean, String beanName) {
				return bean;
			}
		});
		BeanWithDestroyMethod.closed = false;
		lbf.preInstantiateSingletons();
		lbf.destroySingletons();
		assertTrue("Destroy method invoked", BeanWithDestroyMethod.closed);
	}
	
	@Test
	public void testFindTypeOfSingletonFactoryMethodOnBeanInstance() {
		findTypeOfPrototypeFactoryMethodOnBeanInstance(true);
	}
	
	@Test
	public void testFindTypeOfPrototypeFactoryMethodOnBeanInstance() {
		findTypeOfPrototypeFactoryMethodOnBeanInstance(false);
	}
	
	private void findTypeOfPrototypeFactoryMethodOnBeanInstance(boolean singleton) {
		String expectedNameFromProperties = "tony";
		String expectedNameFromArgs = "gordon";
		
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition instanceFactoryDefinition = new RootBeanDefinition(BeanWithFactoryMethod.class);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", expectedNameFromProperties);
		instanceFactoryDefinition.setPropertyValues(pvs);
		lbf.registerBeanDefinition("factoryBeanInstance", instanceFactoryDefinition);
		
		RootBeanDefinition factoryMethodDefinitionWithProperties = new RootBeanDefinition();
		factoryMethodDefinitionWithProperties.setFactoryBeanName("factoryBeanInstance");
		factoryMethodDefinitionWithProperties.setFactoryMethodName("create");
		if(!singleton) {
			factoryMethodDefinitionWithProperties.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		}
		lbf.registerBeanDefinition("fmWithProperties", factoryMethodDefinitionWithProperties);
		
		RootBeanDefinition factoryMethodDefinitionGeneric = new RootBeanDefinition();
		factoryMethodDefinitionGeneric.setFactoryBeanName("factoryBeanInstance");
		factoryMethodDefinitionGeneric.setFactoryMethodName("createGeneric");
		if (!singleton) {
			factoryMethodDefinitionGeneric.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		}
		lbf.registerBeanDefinition("fmGeneric", factoryMethodDefinitionGeneric);
		
		RootBeanDefinition factoryMethodDefinitionWithArgs = new RootBeanDefinition();
		factoryMethodDefinitionWithArgs.setFactoryBeanName("factoryBeanInstance");
		factoryMethodDefinitionWithArgs.setFactoryMethodName("createWithArgs");
		ConstructorArgumentValues cvals = new ConstructorArgumentValues();
		cvals.addGenericArgumentValue(expectedNameFromArgs);
		factoryMethodDefinitionWithArgs.setConstructorArgumentValues(cvals);
		if (!singleton) {
			factoryMethodDefinitionWithArgs.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		}
		lbf.registerBeanDefinition("fmWithArgs", factoryMethodDefinitionWithArgs);    
		
		assertEquals(4, lbf.getBeanDefinitionCount());
		List<String> tbNames = Arrays.asList(lbf.getBeanNamesForType(TestBean.class));
		assertTrue(tbNames.contains("fmWithProperties"));
		assertTrue(tbNames.contains("fmWithArgs"));
		assertEquals(2, tbNames.size());
		
		TestBean tb = (TestBean) lbf.getBean("fmWithProperties");
		TestBean second = (TestBean) lbf.getBean("fmWithProperties");
		if(singleton) {
			assertSame(tb, second);
		} else {
			assertNotSame(tb, second);
		}
		assertEquals(expectedNameFromProperties, tb.getName());

		tb = (TestBean) lbf.getBean("fmGeneric");
		second = (TestBean) lbf.getBean("fmGeneric");
		if (singleton) {
			assertSame(tb, second);
		}
		else {
			assertNotSame(tb, second);
		}
		assertEquals(expectedNameFromProperties, tb.getName());

		TestBean tb2 = (TestBean) lbf.getBean("fmWithArgs");
		second = (TestBean) lbf.getBean("fmWithArgs");
		if (singleton) {
			assertSame(tb2, second);
		}
		else {
			assertNotSame(tb2, second);
		}
		assertEquals(expectedNameFromArgs, tb2.getName());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testScopingBeanToUnregisteredScopeResultsInAnException() throws Exception {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBean.class);
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		beanDefinition.setScope("he put himself so low could hardly look me in the face");

		final DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("testBean", beanDefinition);
		factory.getBean("testBean");
	}
	
	@Test
	public void testExplicitScopeInheritanceForChildBeanDefinition() throws Exception {
		String 	theChildScope = "bonanza!";
		RootBeanDefinition parent = new RootBeanDefinition();
		parent.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		
		AbstractBeanDefinition child = BeanDefinitionBuilder.childBeanDefinition("parent").getBeanDefinition();
		child.setBeanClass(TestBean.class);
		child.setScope(theChildScope);
		
		DefaultListableBeanFactory lbf= new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("parent", parent);
		lbf.registerBeanDefinition("child", child);
		
		AbstractBeanDefinition def = (AbstractBeanDefinition) lbf.getBeanDefinition("child");
		assertEquals("Child 'scope' not overriding parent scope (it must )", theChildScope, def.getScope());
	}
	
	@Test
	public void testScopeInheritanceForChildBeanDefinitions() throws Exception {
		RootBeanDefinition parent = new RootBeanDefinition();
		parent.setScope("bonanza!");

		AbstractBeanDefinition child = new ChildBeanDefinition("parent");
		child.setBeanClass(TestBean.class);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("parent", parent);
		factory.registerBeanDefinition("child", child);

		BeanDefinition def = factory.getMergedBeanDefinition("child");
		assertEquals("Child 'scope' not inherited", "bonanza!", def.getScope());
	}
	
	@Test
	public void testFieldSettingWithInstantiationAwarePostProcessorNoShortCircuit() {
		doTestFieldSettingWithInstantiationAwarePostProcessor(false);
	}
	
	@Test
	public void testFieldSettingWithInstantiationAwarePostProcessorWithShortCircuit() {
		doTestFieldSettingWithInstantiationAwarePostProcessor(true);
	}
	
	private void doTestFieldSettingWithInstantiationAwarePostProcessor(final boolean skipPropertyPopulation) {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		int ageSetByPropertyValue = 27;
		bd.getPropertyValues().addPropertyValue(new PropertyValue("age", new Integer(ageSetByPropertyValue)));
		lbf.registerBeanDefinition("test", bd);
		final String nameSetOnField = "nameSetOnField";
		lbf.addBeanPostProcessor(new InstantiationAwareBeanPostProcessorAdapter() {
			@Override
			public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
				TestBean tb = (TestBean) bean;
				try {
					Field f = TestBean.class.getDeclaredField("name");
					f.setAccessible(true);
					f.set(tb, nameSetOnField);
					return !skipPropertyPopulation;
				} catch(Exception e) {
					fail("Unexpected exception :  " + e);
					throw new IllegalStateException();
				}
			}
		});
		lbf.preInstantiateSingletons();
		TestBean tb = (TestBean) lbf.getBean("test");
		assertEquals("Name was set on field by IAPP", nameSetOnField, tb.getName());
		if (!skipPropertyPopulation) {
			assertEquals("Property value still set", ageSetByPropertyValue, tb.getAge());
		}
		else {
			assertEquals("Property value was NOT set and still has default value", 0, tb.getAge());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testInitSecurityAwarePrototypeBean() {
		final DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestSecuredBean.class);
		bd.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
		bd.setInitMethodName("init");
		lbf.registerBeanDefinition("test", bd);
		final Subject subject = new Subject();
		subject.getPrincipals().add(new TestPrincipal("user1"));

		TestSecuredBean bean = (TestSecuredBean) Subject.doAsPrivileged(subject,
				new PrivilegedAction() {
					public Object run() {
						return lbf.getBean("test");
					}
				}, null);
		assertNotNull(bean);
		assertEquals("user1", bean.getUserName());
	}
	
	@Test
	public void testContainsBeanReturnsTrueEvenForAbstractBeanDefinition() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerBeanDefinition("abs",
				BeanDefinitionBuilder.rootBeanDefinition(TestBean.class).setAbstract(true).getBeanDefinition());
		assertThat(bf.containsBean("abs"), is(true));
		assertThat(bf.containsBean("bogus"), is(false));
	}
	
	private static class TestPrincipal implements Principal {

		private String  name;
		
		public TestPrincipal(String name) {
			this.name = name;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(!(obj instanceof TestPrincipal)) {
				return false;
			}
			TestPrincipal p = (TestPrincipal) obj;
			return this.name.equals(p.name);
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override
		public int hashCode() {
			return this.name.hashCode();
		}
		
	}
	
	private static class TestSecuredBean {
		private String userName;
		
		public void init() {
			AccessControlContext acc = AccessController.getContext();
			Subject subject = Subject.getSubject(acc);
			if(subject == null) {
				return;
			}
			setNameFromPrincipal(subject.getPrincipals());
		}

		private void setNameFromPrincipal(Set<Principal> principals) {
			if(principals == null) {
				 return;
			}
			for(Iterator<Principal> it = principals.iterator(); it.hasNext();) {
				Principal p = it.next();
				this.userName = p.getName();
				return;
			}
		}
		public String  getUserName()  {
			return  this.userName;
		}
	}

	public static class BeanWithFactoryMethod {
		private String name;
		
		public void setName(String name) {
			this.name = name;
		}
		
		public TestBean create() {
			TestBean tb = new TestBean();
			tb.setName(this.name);
			return tb;
		}
		
		public TestBean createWithArgs(String arg) {
			TestBean tb = new TestBean();
			tb.setName(arg);
			return tb;
		}
		
		public Object createGeneric() {
			return create();
		}
	}

	public static class BeanWithDestroyMethod {
		private static boolean closed;
		public void close() {
			closed = true;
		}
	}
	
	public static class BeanWithDisposableBean implements DisposableBean {

		private static boolean closed;
		public void destroy() throws Exception {
			closed = true;
		}
		
	}
	
	public static class EagerInitFactory implements SmartFactoryBean<Object> {

		public boolean initialized = false;
		
		public Object getObject() throws Exception {
			this.initialized = true;
			return "";
		}

		public Class<?> getObjectType() {
			return String.class;
		}

		public boolean isSingleton() {
			return true;
		}

		public boolean isPrototype() {
			return false;
		}

		public boolean isEagerInit() {
			return true;
		}
		
	}
	
	public static class ConstructorDependencyFactoryBean implements FactoryBean<Object> {

		public ConstructorDependencyFactoryBean(String dependency) {
		}

		public Object getObject() {
			return "test";
		}

		public Class<?> getObjectType() {
			return String.class;
		}

		public boolean isSingleton() {
			return true;
		}
	}

	public static class ConstructorDependencyBean {

		public ConstructorDependencyBean(ConstructorDependencyBean dependency) {
		}
	}

	public static class FactoryBeanThatShouldntBeCalled implements FactoryBean<Object> {

		public Object getObject() {
			throw new IllegalStateException();
		}

		public Class<?> getObjectType() {
			return null;
		}

		public boolean isSingleton() {
			return false;
		}
	}
	
	private static class FactoryBeanDependentBean {
		private FactoryBean<?> factoryBean;
		
		public final FactoryBean<?> getFactoryBean() {
			return this.factoryBean;
		}
		
		public final void setFactoryBean(final FactoryBean<?> factoryBean) {
			this.factoryBean = factoryBean;
		}
	}
	
	public static class LazyInitFactory implements FactoryBean<Object> {

		public boolean initialized = false;
		
		public Object getObject() throws Exception {
			this.initialized = true;
			return "";
		}

		public Class<?> getObjectType() {
			return String.class;
		}

		public boolean isSingleton() {
			return true;
		}
		
	}
	
	public static class UnsatisfiedConstructorDependency {
		public UnsatisfiedConstructorDependency(TestBean t, SideEffectBean b) {
		}
	}
	
	private static class SideEffectBean {
		private int count;
		public void setCount(int count) {
			this.count = count;
		}
		
		public int getCount() {
			return this.count;
		}
		
		public void doWork() {
			++count;
		}
	}
	
	public static class ConstructorDependency {
		public TestBean spouse;
		
		public ConstructorDependency(TestBean testBean) {
			this.spouse = testBean;
		}
		
		private ConstructorDependency(TestBean spouse, TestBean otherSpouse) {
			throw new IllegalArgumentException("Should never be called");
		}
	}

	private static class CustomTypeConverter implements TypeConverter {
		
		private final NumberFormat numberFormat;
		
		public CustomTypeConverter(NumberFormat format) {
			this.numberFormat = format;
		}

		public <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMisMatchException {
			return convertIfNecessary(value, requiredType, null);
		}

		@SuppressWarnings("unchecked")
		public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParameter)
				throws TypeMisMatchException {
			if(value instanceof String && Float.class.isAssignableFrom(requiredType)) {
				try {
					return (T) new Float(this.numberFormat.parse((String) value).floatValue());
				} catch (ParseException e) {
					throw new TypeMisMatchException(value, requiredType, e);
				}
			} else if(value instanceof String && int.class.isAssignableFrom(requiredType)) {
				return (T) new Integer(5);
			} else {
				return (T) value;
			}
		}
		
	}
	
	public static class ArrayBean {
		
		private Integer[] integerArray;
		
		private Resource[] resourceArray;
		
		public ArrayBean() {}
		
		public ArrayBean(Integer[] integerArray) {
			this.integerArray = integerArray;
		}
		
		public ArrayBean(Integer[] integerArray, Resource[] resourceArray) {
			this.integerArray = integerArray;
			this.resourceArray = resourceArray;
		}
		
		public Integer[] getIntegerArray() {
			return this.integerArray;
		}

		public void setResourceArray(Resource[] resourceArray) {
			this.resourceArray = resourceArray;
		}

		public Resource[] getResourceArray() {
			return this.resourceArray;
		}
		
	}

	public static class TestBeanFactory {
		public static boolean initialized = false;
		public TestBeanFactory() {
			initialized = true;
		}
		public static TestBean createTestBean() {
			return new TestBean();
		}
		public TestBean createTestBeanNonStatic() {
			return new TestBean();
		}
	}
	
	private static class KnowsIfInstantiated  {
		private static boolean instantiated;
		
		public static void clearInstantiationRecord() { 
			instantiated = false;
		}
		
		public static boolean wasInstantiated() {
			return instantiated;
		}

		public KnowsIfInstantiated() {
			instantiated = true;
		}
	}
}
