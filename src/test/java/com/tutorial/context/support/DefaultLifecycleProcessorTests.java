package com.tutorial.context.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import com.tutorial.beans.DirectFieldAccessor;
import com.tutorial.beans.factory.FactoryBean;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.support.RootBeanDefinition;
import com.tutorial.context.Lifecycle;
import com.tutorial.context.LifecycleProcessor;
import com.tutorial.context.SmartLifecycle;

/**
 * @author Liufeng
 * Created on 2018年12月9日 下午3:29:38
 */
public class DefaultLifecycleProcessorTests {
	
	@Test
	public void defaultLifecycleProcessorInstance() {
		StaticApplicationContext context = new StaticApplicationContext();
		context.refresh();
		Object lifecycleProcessor = new DirectFieldAccessor(context).getPropertyValue("lifecycleProcessor");
		assertNotNull(lifecycleProcessor);
		assertEquals(DefaultLifecycleProcessor.class, lifecycleProcessor.getClass());
	}
	
	@Test
	public void customLifecycleProcessorInstance() {
		BeanDefinition beanDefinition = new RootBeanDefinition(DefaultLifecycleProcessor.class);
		beanDefinition.getPropertyValues().addPropertyValue("timeoutPerShutdownPhase", 1000);
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerBeanDefinition("lifecycleProcessor", beanDefinition);
		context.refresh();
		LifecycleProcessor bean = context.getBean("lifecycleProcessor", LifecycleProcessor.class);
		Object contextLifecycleProcessor = new DirectFieldAccessor(context).getPropertyValue("lifecycleProcessor");
		assertNotNull(contextLifecycleProcessor);
		assertSame(bean, contextLifecycleProcessor);
		assertEquals(1000L, new DirectFieldAccessor(contextLifecycleProcessor).getPropertyValue("timeoutPerShutdownPhase"));
	}
	
	@Test
	public void singleSmartLifecycleAutoStartup() {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<>();
		TestSmartLifecycleBean bean = TestSmartLifecycleBean.forStartupTests(1, startedBeans);
		bean.setAutoStartup(true);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean", bean);
		assertFalse(bean.isRunning());
		context.refresh();
		assertTrue(bean.isRunning());
		context.stop();
		assertFalse(bean.isRunning());
		assertEquals(1, startedBeans.size());
	}
	
	@Test
	public void singleSmartLifecycleAutoStartupWithLazyInit() {
		StaticApplicationContext context = new StaticApplicationContext();
		RootBeanDefinition bd = new RootBeanDefinition(DummySmartLifecycleBean.class);
		bd.setLazyInit(true);
		context.registerBeanDefinition("bean", bd);
		context.refresh();
		DummySmartLifecycleBean bean = context.getBean("bean", DummySmartLifecycleBean.class);
		assertTrue(bean.isRunning());
		context.stop();
		assertFalse(bean.isRunning());
	}
	
	@Test
	public void singleSmartLifecycleAutoStartupWithLazyInitFactoryBean() throws Exception {
		StaticApplicationContext context = new StaticApplicationContext();
		RootBeanDefinition bd = new RootBeanDefinition(DummySmartLifecycleFactoryBean.class);
		bd.setLazyInit(true);
		context.registerBeanDefinition("bean", bd);
		context.refresh();
		DummySmartLifecycleFactoryBean bean = context.getBean("&bean", DummySmartLifecycleFactoryBean.class);
		assertTrue(bean.isRunning());
		context.stop();
		assertFalse(bean.isRunning());
	}
	
	@Test
	public void singleSmartLifecycleWithoutAutoStartup() {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean bean = TestSmartLifecycleBean.forStartupTests(1, startedBeans);
		bean.setAutoStartup(false);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean", bean);
		assertFalse(bean.isRunning());
		context.refresh();
		assertFalse(bean.isRunning());
		assertEquals(0, startedBeans.size());
		context.start();
		assertTrue(bean.isRunning());
		assertEquals(1, startedBeans.size());
		context.stop();
	}
	
	@Test
	public void singleSmartLifecycleAutoStartupWithNonAutoStartupDependency() {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean bean = TestSmartLifecycleBean.forStartupTests(1, startedBeans);
		bean.setAutoStartup(true);
		TestSmartLifecycleBean dependency = TestSmartLifecycleBean.forStartupTests(1, startedBeans);
		dependency.setAutoStartup(false);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean", bean);
		context.getBeanFactory().registerSingleton("dependency", dependency);
		context.getBeanFactory().registerDependentBean("dependency", "bean");
		assertFalse(bean.isRunning());
		assertFalse(dependency.isRunning());
		context.refresh();
		assertTrue(bean.isRunning());
		assertFalse(dependency.isRunning());
		context.stop();
		assertFalse(bean.isRunning());
		assertFalse(dependency.isRunning());
		assertEquals(1, startedBeans.size());
	}
	
	@Test
	public void smartLifecycleGroupStartup() {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<>();
		TestSmartLifecycleBean beanMin = TestSmartLifecycleBean.forStartupTests(Integer.MIN_VALUE, startedBeans);
		TestSmartLifecycleBean bean1 = TestSmartLifecycleBean.forStartupTests(1, startedBeans);
		TestSmartLifecycleBean bean2 = TestSmartLifecycleBean.forStartupTests(2, startedBeans);
		TestSmartLifecycleBean bean3 = TestSmartLifecycleBean.forStartupTests(3, startedBeans);
		TestSmartLifecycleBean beanMax = TestSmartLifecycleBean.forStartupTests(Integer.MAX_VALUE, startedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean3", bean3);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean1", bean1);
		context.getBeanFactory().registerSingleton("beanMin", beanMin);
		context.getBeanFactory().registerSingleton("beanMax", beanMax);
		assertFalse(beanMin.isRunning());
		assertFalse(beanMax.isRunning());
		assertFalse(bean1.isRunning());
		assertFalse(bean2.isRunning());
		assertFalse(bean3.isRunning());
		context.refresh();
		assertTrue(beanMin.isRunning());
		assertTrue(bean1.isRunning());
		assertTrue(bean2.isRunning());
		assertTrue(bean3.isRunning());
		assertTrue(beanMax.isRunning());
		context.stop();
		assertEquals(5, startedBeans.size());
		assertEquals(Integer.MIN_VALUE, getPhase(startedBeans.get(0)));
		assertEquals(1, getPhase(startedBeans.get(1)));
		assertEquals(2, getPhase(startedBeans.get(2)));
		assertEquals(3, getPhase(startedBeans.get(3)));
		assertEquals(Integer.MAX_VALUE, getPhase(startedBeans.get(4)));
	}
	
	@Test
	public void contextRefreshThenStartWithMixedBeans() throws Exception {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestLifecycleBean simpleBean1 = TestLifecycleBean.forStartupTests(startedBeans);
		TestLifecycleBean simpleBean2 = TestLifecycleBean.forStartupTests(startedBeans);
		TestSmartLifecycleBean smartBean1 = TestSmartLifecycleBean.forStartupTests(5, startedBeans);
		TestSmartLifecycleBean smartBean2 = TestSmartLifecycleBean.forStartupTests(-3, startedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("simpleBean1", simpleBean1);
		context.getBeanFactory().registerSingleton("smartBean1", smartBean1);
		context.getBeanFactory().registerSingleton("simpleBean2", simpleBean2);
		context.getBeanFactory().registerSingleton("smartBean2", smartBean2);
		assertFalse(simpleBean1.isRunning());
		assertFalse(simpleBean2.isRunning());
		assertFalse(smartBean1.isRunning());
		assertFalse(smartBean2.isRunning());
		context.refresh();
		assertTrue(smartBean1.isRunning());
		assertTrue(smartBean2.isRunning());
		assertFalse(simpleBean1.isRunning());
		assertFalse(simpleBean2.isRunning());
		assertEquals(2, startedBeans.size());
		assertEquals(-3, getPhase(startedBeans.get(0)));
		assertEquals(5, getPhase(startedBeans.get(1)));
		context.start();
		assertTrue(smartBean1.isRunning());
		assertTrue(smartBean2.isRunning());
		assertTrue(simpleBean1.isRunning());
		assertTrue(simpleBean2.isRunning());
		assertEquals(4, startedBeans.size());
		assertEquals(0, getPhase(startedBeans.get(2)));
		assertEquals(0, getPhase(startedBeans.get(3)));
	}

	@Test
	public void contextRefreshThenStopAndRestartWithMixedBeans() throws Exception {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestLifecycleBean simpleBean1 = TestLifecycleBean.forStartupTests(startedBeans);
		TestLifecycleBean simpleBean2 = TestLifecycleBean.forStartupTests(startedBeans);
		TestSmartLifecycleBean smartBean1 = TestSmartLifecycleBean.forStartupTests(5, startedBeans);
		TestSmartLifecycleBean smartBean2 = TestSmartLifecycleBean.forStartupTests(-3, startedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("simpleBean1", simpleBean1);
		context.getBeanFactory().registerSingleton("smartBean1", smartBean1);
		context.getBeanFactory().registerSingleton("simpleBean2", simpleBean2);
		context.getBeanFactory().registerSingleton("smartBean2", smartBean2);
		assertFalse(simpleBean1.isRunning());
		assertFalse(simpleBean2.isRunning());
		assertFalse(smartBean1.isRunning());
		assertFalse(smartBean2.isRunning());
		context.refresh();
		assertTrue(smartBean1.isRunning());
		assertTrue(smartBean2.isRunning());
		assertFalse(simpleBean1.isRunning());
		assertFalse(simpleBean2.isRunning());
		assertEquals(2, startedBeans.size());
		assertEquals(-3, getPhase(startedBeans.get(0)));
		assertEquals(5, getPhase(startedBeans.get(1)));
		context.stop();
		assertFalse(simpleBean1.isRunning());
		assertFalse(simpleBean2.isRunning());
		assertFalse(smartBean1.isRunning());
		assertFalse(smartBean2.isRunning());
		context.start();
		assertTrue(smartBean1.isRunning());
		assertTrue(smartBean2.isRunning());
		assertTrue(simpleBean1.isRunning());
		assertTrue(simpleBean2.isRunning());
		assertEquals(6, startedBeans.size());
		assertEquals(-3, getPhase(startedBeans.get(2)));
		assertEquals(0, getPhase(startedBeans.get(3)));
		assertEquals(0, getPhase(startedBeans.get(4)));
		assertEquals(5, getPhase(startedBeans.get(5)));
	}
	
	@Test
	public void smartLifecycleGroupShutdown() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean bean1 = TestSmartLifecycleBean.forShutdownTests(1, 300, stoppedBeans);
		TestSmartLifecycleBean bean2 = TestSmartLifecycleBean.forShutdownTests(3, 100, stoppedBeans);
		TestSmartLifecycleBean bean3 = TestSmartLifecycleBean.forShutdownTests(1, 600, stoppedBeans);
		TestSmartLifecycleBean bean4 = TestSmartLifecycleBean.forShutdownTests(2, 400, stoppedBeans);
		TestSmartLifecycleBean bean5 = TestSmartLifecycleBean.forShutdownTests(2, 700, stoppedBeans);
		TestSmartLifecycleBean bean6 = TestSmartLifecycleBean.forShutdownTests(Integer.MAX_VALUE, 200, stoppedBeans);
		TestSmartLifecycleBean bean7 = TestSmartLifecycleBean.forShutdownTests(3, 200, stoppedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean1", bean1);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean3", bean3);
		context.getBeanFactory().registerSingleton("bean4", bean4);
		context.getBeanFactory().registerSingleton("bean5", bean5);
		context.getBeanFactory().registerSingleton("bean6", bean6);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.refresh();
		context.stop();
		assertEquals(Integer.MAX_VALUE, getPhase(stoppedBeans.get(0)));
		assertEquals(3, getPhase(stoppedBeans.get(1)));
		assertEquals(3, getPhase(stoppedBeans.get(2)));
		assertEquals(2, getPhase(stoppedBeans.get(3)));
		assertEquals(2, getPhase(stoppedBeans.get(4)));
		assertEquals(1, getPhase(stoppedBeans.get(5)));
		assertEquals(1, getPhase(stoppedBeans.get(6)));
	}

	@Test
	public void singleSmartLifecycleShutdown() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean bean = TestSmartLifecycleBean.forShutdownTests(99, 300, stoppedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean", bean);
		context.refresh();
		assertTrue(bean.isRunning());
		context.stop();
		assertEquals(1, stoppedBeans.size());
		assertFalse(bean.isRunning());
		assertEquals(bean, stoppedBeans.get(0));
	}

	@Test
	public void singleLifecycleShutdown() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		Lifecycle bean = new TestLifecycleBean(null, stoppedBeans); 
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean", bean);
		context.refresh();
		assertFalse(bean.isRunning());
		bean.start();
		assertTrue(bean.isRunning());
		context.stop();
		assertEquals(1, stoppedBeans.size());
		assertFalse(bean.isRunning());
		assertEquals(bean, stoppedBeans.get(0));
	}

	@Test
	public void mixedShutdown() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		Lifecycle bean1 = TestLifecycleBean.forShutdownTests(stoppedBeans);
		Lifecycle bean2 = TestSmartLifecycleBean.forShutdownTests(500, 200, stoppedBeans);
		Lifecycle bean3 = TestSmartLifecycleBean.forShutdownTests(Integer.MAX_VALUE, 100, stoppedBeans);
		Lifecycle bean4 = TestLifecycleBean.forShutdownTests(stoppedBeans);
		Lifecycle bean5 = TestSmartLifecycleBean.forShutdownTests(1, 200, stoppedBeans);
		Lifecycle bean6 = TestSmartLifecycleBean.forShutdownTests(-1, 100, stoppedBeans);
		Lifecycle bean7 = TestSmartLifecycleBean.forShutdownTests(Integer.MIN_VALUE, 300, stoppedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("bean1", bean1);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean3", bean3);
		context.getBeanFactory().registerSingleton("bean4", bean4);
		context.getBeanFactory().registerSingleton("bean5", bean5);
		context.getBeanFactory().registerSingleton("bean6", bean6);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.refresh();
		assertTrue(bean2.isRunning());
		assertTrue(bean3.isRunning());
		assertTrue(bean5.isRunning());
		assertTrue(bean6.isRunning());
		assertTrue(bean7.isRunning());
		assertFalse(bean1.isRunning());
		assertFalse(bean4.isRunning());
		bean1.start();
		bean4.start();
		assertTrue(bean1.isRunning());
		assertTrue(bean4.isRunning());
		context.stop();
		assertFalse(bean1.isRunning());
		assertFalse(bean2.isRunning());
		assertFalse(bean3.isRunning());
		assertFalse(bean4.isRunning());
		assertFalse(bean5.isRunning());
		assertFalse(bean6.isRunning());
		assertFalse(bean7.isRunning());
		assertEquals(7, stoppedBeans.size());
		assertEquals(Integer.MAX_VALUE, getPhase(stoppedBeans.get(0)));
		assertEquals(500, getPhase(stoppedBeans.get(1)));
		assertEquals(1, getPhase(stoppedBeans.get(2)));
		assertEquals(0, getPhase(stoppedBeans.get(3)));
		assertEquals(0, getPhase(stoppedBeans.get(4)));
		assertEquals(-1, getPhase(stoppedBeans.get(5)));
		assertEquals(Integer.MIN_VALUE, getPhase(stoppedBeans.get(6)));
	}

	@Test
	public void dependencyStartedFirstEvenIfItsPhaseIsHigher() throws Exception {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean beanMin = TestSmartLifecycleBean.forStartupTests(Integer.MIN_VALUE, startedBeans);
		TestSmartLifecycleBean bean2 = TestSmartLifecycleBean.forStartupTests(2, startedBeans);
		TestSmartLifecycleBean bean99 = TestSmartLifecycleBean.forStartupTests(99, startedBeans);
		TestSmartLifecycleBean beanMax = TestSmartLifecycleBean.forStartupTests(Integer.MAX_VALUE, startedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("beanMin", beanMin);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean99", bean99);
		context.getBeanFactory().registerSingleton("beanMax", beanMax);
		context.getBeanFactory().registerDependentBean("bean99", "bean2");
		context.refresh();
		assertTrue(beanMin.isRunning());
		assertTrue(bean2.isRunning());
		assertTrue(bean99.isRunning());
		assertTrue(beanMax.isRunning());
		assertEquals(4, startedBeans.size());
		assertEquals(Integer.MIN_VALUE, getPhase(startedBeans.get(0)));
		assertEquals(99, getPhase(startedBeans.get(1)));
		assertEquals(bean99, startedBeans.get(1));
		assertEquals(2, getPhase(startedBeans.get(2)));
		assertEquals(bean2, startedBeans.get(2));
		assertEquals(Integer.MAX_VALUE, getPhase(startedBeans.get(3)));
		context.stop();
	}

	@Test
	public void dependentShutdownFirstEvenIfItsPhaseIsLower() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean beanMin = TestSmartLifecycleBean.forShutdownTests(Integer.MIN_VALUE, 100, stoppedBeans);
		TestSmartLifecycleBean bean1 = TestSmartLifecycleBean.forShutdownTests(1, 200, stoppedBeans);
		TestSmartLifecycleBean bean99 = TestSmartLifecycleBean.forShutdownTests(99, 100, stoppedBeans);
		TestSmartLifecycleBean bean2 = TestSmartLifecycleBean.forShutdownTests(2, 300, stoppedBeans);
		TestSmartLifecycleBean bean7 = TestSmartLifecycleBean.forShutdownTests(7, 400, stoppedBeans);
		TestSmartLifecycleBean beanMax = TestSmartLifecycleBean.forShutdownTests(Integer.MAX_VALUE, 400, stoppedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("beanMin", beanMin);
		context.getBeanFactory().registerSingleton("bean1", bean1);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.getBeanFactory().registerSingleton("bean99", bean99);
		context.getBeanFactory().registerSingleton("beanMax", beanMax);
		context.getBeanFactory().registerDependentBean("bean99", "bean2");
		context.refresh();
		assertTrue(beanMin.isRunning());
		assertTrue(bean1.isRunning());
		assertTrue(bean2.isRunning());
		assertTrue(bean7.isRunning());
		assertTrue(bean99.isRunning());
		assertTrue(beanMax.isRunning());
		context.stop();
		assertFalse(beanMin.isRunning());
		assertFalse(bean1.isRunning());
		assertFalse(bean2.isRunning());
		assertFalse(bean7.isRunning());
		assertFalse(bean99.isRunning());
		assertFalse(beanMax.isRunning());
		assertEquals(6, stoppedBeans.size());
		assertEquals(Integer.MAX_VALUE, getPhase(stoppedBeans.get(0)));
		assertEquals(2, getPhase(stoppedBeans.get(1)));
		assertEquals(bean2, stoppedBeans.get(1));
		assertEquals(99, getPhase(stoppedBeans.get(2)));
		assertEquals(bean99, stoppedBeans.get(2));
		assertEquals(7, getPhase(stoppedBeans.get(3)));
		assertEquals(1, getPhase(stoppedBeans.get(4)));
		assertEquals(Integer.MIN_VALUE, getPhase(stoppedBeans.get(5)));
	}

	@Test
	public void dependencyStartedFirstAndIsSmartLifecycle() throws Exception {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean beanNegative = TestSmartLifecycleBean.forStartupTests(-99, startedBeans);
		TestSmartLifecycleBean bean99 = TestSmartLifecycleBean.forStartupTests(99, startedBeans);
		TestSmartLifecycleBean bean7 = TestSmartLifecycleBean.forStartupTests(7, startedBeans);
		TestLifecycleBean simpleBean = TestLifecycleBean.forStartupTests(startedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("beanNegative", beanNegative);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.getBeanFactory().registerSingleton("bean99", bean99);
		context.getBeanFactory().registerSingleton("simpleBean", simpleBean);
		context.getBeanFactory().registerDependentBean("bean7", "simpleBean");
		context.refresh();
		context.stop();
		startedBeans.clear();
		// clean start so that simpleBean is included
		context.start();
		assertTrue(beanNegative.isRunning());
		assertTrue(bean99.isRunning());
		assertTrue(bean7.isRunning());
		assertTrue(simpleBean.isRunning());
		assertEquals(4, startedBeans.size());
		assertEquals(-99, getPhase(startedBeans.get(0)));
		assertEquals(7, getPhase(startedBeans.get(1)));
		assertEquals(0, getPhase(startedBeans.get(2)));
		assertEquals(99, getPhase(startedBeans.get(3)));
		context.stop();
	}

	@Test
	public void dependentShutdownFirstAndIsSmartLifecycle() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean beanMin = TestSmartLifecycleBean.forShutdownTests(Integer.MIN_VALUE, 400, stoppedBeans);
		TestSmartLifecycleBean beanNegative = TestSmartLifecycleBean.forShutdownTests(-99, 100, stoppedBeans);
		TestSmartLifecycleBean bean1 = TestSmartLifecycleBean.forShutdownTests(1, 200, stoppedBeans);
		TestSmartLifecycleBean bean2 = TestSmartLifecycleBean.forShutdownTests(2, 300, stoppedBeans);
		TestSmartLifecycleBean bean7 = TestSmartLifecycleBean.forShutdownTests(7, 400, stoppedBeans);
		TestLifecycleBean simpleBean = TestLifecycleBean.forShutdownTests(stoppedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("beanMin", beanMin);
		context.getBeanFactory().registerSingleton("beanNegative", beanNegative);
		context.getBeanFactory().registerSingleton("bean1", bean1);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.getBeanFactory().registerSingleton("simpleBean", simpleBean);
		context.getBeanFactory().registerDependentBean("simpleBean", "beanNegative");
		context.refresh();
		assertTrue(beanMin.isRunning());
		assertTrue(beanNegative.isRunning());
		assertTrue(bean1.isRunning());
		assertTrue(bean2.isRunning());
		assertTrue(bean7.isRunning());
		// should start since it's a dependency of an auto-started bean
		assertTrue(simpleBean.isRunning());
		context.stop();
		assertFalse(beanMin.isRunning());
		assertFalse(beanNegative.isRunning());
		assertFalse(bean1.isRunning());
		assertFalse(bean2.isRunning());
		assertFalse(bean7.isRunning());
		assertFalse(simpleBean.isRunning());
		assertEquals(6, stoppedBeans.size());
		assertEquals(7, getPhase(stoppedBeans.get(0)));
		assertEquals(2, getPhase(stoppedBeans.get(1)));
		assertEquals(1, getPhase(stoppedBeans.get(2)));
		assertEquals(-99, getPhase(stoppedBeans.get(3)));
		assertEquals(0, getPhase(stoppedBeans.get(4)));
		assertEquals(Integer.MIN_VALUE, getPhase(stoppedBeans.get(5)));
	}

	@Test
	public void dependencyStartedFirstButNotSmartLifecycle() throws Exception {
		CopyOnWriteArrayList<Lifecycle> startedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean beanMin = TestSmartLifecycleBean.forStartupTests(Integer.MIN_VALUE, startedBeans);
		TestSmartLifecycleBean bean7 = TestSmartLifecycleBean.forStartupTests(7, startedBeans);
		TestLifecycleBean simpleBean = TestLifecycleBean.forStartupTests(startedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("beanMin", beanMin);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.getBeanFactory().registerSingleton("simpleBean", simpleBean);
		context.getBeanFactory().registerDependentBean("simpleBean", "beanMin");
		context.refresh();
		assertTrue(beanMin.isRunning());
		assertTrue(bean7.isRunning());
		assertTrue(simpleBean.isRunning());
		assertEquals(3, startedBeans.size());
		assertEquals(0, getPhase(startedBeans.get(0)));
		assertEquals(Integer.MIN_VALUE, getPhase(startedBeans.get(1)));
		assertEquals(7, getPhase(startedBeans.get(2)));
		context.stop();
	}

	@Test
	public void dependentShutdownFirstButNotSmartLifecycle() throws Exception {
		CopyOnWriteArrayList<Lifecycle> stoppedBeans = new CopyOnWriteArrayList<Lifecycle>();
		TestSmartLifecycleBean bean1 = TestSmartLifecycleBean.forShutdownTests(1, 200, stoppedBeans);
		TestLifecycleBean simpleBean = TestLifecycleBean.forShutdownTests(stoppedBeans);
		TestSmartLifecycleBean bean2 = TestSmartLifecycleBean.forShutdownTests(2, 300, stoppedBeans);
		TestSmartLifecycleBean bean7 = TestSmartLifecycleBean.forShutdownTests(7, 400, stoppedBeans);
		TestSmartLifecycleBean beanMin = TestSmartLifecycleBean.forShutdownTests(Integer.MIN_VALUE, 400, stoppedBeans);
		StaticApplicationContext context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("beanMin", beanMin);
		context.getBeanFactory().registerSingleton("bean1", bean1);
		context.getBeanFactory().registerSingleton("bean2", bean2);
		context.getBeanFactory().registerSingleton("bean7", bean7);
		context.getBeanFactory().registerSingleton("simpleBean", simpleBean);
		context.getBeanFactory().registerDependentBean("bean2", "simpleBean");
		context.refresh();
		assertTrue(beanMin.isRunning());
		assertTrue(bean1.isRunning());
		assertTrue(bean2.isRunning());
		assertTrue(bean7.isRunning());
		assertFalse(simpleBean.isRunning());
		simpleBean.start();
		assertTrue(simpleBean.isRunning());
		context.stop();
		assertFalse(beanMin.isRunning());
		assertFalse(bean1.isRunning());
		assertFalse(bean2.isRunning());
		assertFalse(bean7.isRunning());
		assertFalse(simpleBean.isRunning());
		assertEquals(5, stoppedBeans.size());
		assertEquals(7, getPhase(stoppedBeans.get(0)));
		assertEquals(0, getPhase(stoppedBeans.get(1)));
		assertEquals(2, getPhase(stoppedBeans.get(2)));
		assertEquals(1, getPhase(stoppedBeans.get(3)));
		assertEquals(Integer.MIN_VALUE, getPhase(stoppedBeans.get(4)));
	}
	
	private Object getPhase(Lifecycle lifecycle) {
		return (lifecycle instanceof SmartLifecycle ? 
				((SmartLifecycle) lifecycle).getPhase() : 0);
	}

	private static class TestLifecycleBean implements Lifecycle {

		private volatile boolean isrunning;
		private final CopyOnWriteArrayList<Lifecycle> stoppedBeans;
		private final CopyOnWriteArrayList<Lifecycle> startedBeans;
		
		static TestLifecycleBean forStartupTests(CopyOnWriteArrayList<Lifecycle> startedBeans) {
			return new TestLifecycleBean(startedBeans, null);
		}
		
		static TestLifecycleBean forShutdownTests(CopyOnWriteArrayList<Lifecycle> stoppedBeans) {
			return new TestLifecycleBean(null, stoppedBeans);
		}
		
		private TestLifecycleBean(CopyOnWriteArrayList<Lifecycle> startedBeans, CopyOnWriteArrayList<Lifecycle> stoppedBeans) {
			this.startedBeans = startedBeans;
			this.stoppedBeans = stoppedBeans;
		}
		
		@Override
		public void start() {
			if(this.startedBeans != null) {
				this.startedBeans.add(this);
			}
			this.isrunning = true;
		}

		@Override
		public void stop() {
			if(this.stoppedBeans != null) {
				this.stoppedBeans.add(this);
			}
			this.isrunning = false;
		}

		@Override
		public boolean isRunning() {
			return this.isrunning;
		}
		
	}
	
	private static class TestSmartLifecycleBean extends TestLifecycleBean implements SmartLifecycle {

		private final int phase;
		private final int shutdownDelay;
		private volatile boolean autoStartup = true;
		
		static TestSmartLifecycleBean forStartupTests(int phase, CopyOnWriteArrayList<Lifecycle> startedBeans) {
			return new TestSmartLifecycleBean(phase, 0, startedBeans, null);
		}

		static TestSmartLifecycleBean forShutdownTests(int phase, int shutdownDelay, CopyOnWriteArrayList<Lifecycle> stoppedBeans) {
			return new TestSmartLifecycleBean(phase, shutdownDelay, null, stoppedBeans);
		}
		
		private TestSmartLifecycleBean(int phase, int shutdownDelay, CopyOnWriteArrayList<Lifecycle> startedBeans, 
				CopyOnWriteArrayList<Lifecycle> stoppedBeans) {
			super(startedBeans, stoppedBeans);
			this.phase = phase;
			this.shutdownDelay = shutdownDelay;
		}
		
		@Override
		public int getPhase() {
			return phase;
		}

		@Override
		public boolean isAutoStartup() {
			return this.autoStartup;
		}
		
		public void setAutoStartup(boolean autoStartup) {
			this.autoStartup = autoStartup;
		}

		@Override
		public void stop(final Runnable callback) {
			stop();
			final int delay = this.shutdownDelay;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						// ignore
					} finally {
						callback.run();
					}
				}
			}).start();
		}
		
	}
	
	public static class DummySmartLifecycleBean implements SmartLifecycle {

		private boolean running = false;
		@Override
		public void start() {
			this.running = true;
		}

		@Override
		public void stop() {
			this.running = false;
		}

		@Override
		public boolean isRunning() {
			return running;
		}

		@Override
		public int getPhase() {
			return 0;
		}

		@Override
		public boolean isAutoStartup() {
			return true;
		}

		@Override
		public void stop(Runnable callback) {
			this.running = false;
			callback.run();
		}
		
	}
	public static class DummySmartLifecycleFactoryBean implements FactoryBean, SmartLifecycle {

		public boolean running = false;

		DummySmartLifecycleBean bean = new DummySmartLifecycleBean();

		public Object getObject() throws Exception {
			return this.bean;
		}

		public Class getObjectType() {
			return DummySmartLifecycleBean.class;
		}

		public boolean isSingleton() {
			return true;
		}

		public boolean isAutoStartup() {
			return true;
		}

		public void stop(Runnable callback) {
			this.running = false;
			callback.run();
		}

		public void start() {
			this.running = true;
		}

		public void stop() {
			this.running = false;
		}

		public boolean isRunning() {
			return this.running;
		}

		public int getPhase() {
			return 0;
		}
	}
}
