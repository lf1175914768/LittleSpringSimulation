package com.tutorial.context.support;

import java.io.IOException;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanDefinitionStoreException;
import com.tutorial.beans.factory.NoSuchBeanDefinitionException;
import com.tutorial.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.config.ConfigurableListableBeanFactory;
import com.tutorial.beans.factory.support.BeanDefinitionRegistry;
import com.tutorial.beans.factory.support.DefaultListableBeanFactory;
import com.tutorial.context.ApplicationContext;
import com.tutorial.core.io.Resource;
import com.tutorial.core.io.ResourceLoader;
import com.tutorial.core.io.support.ResourcePatternResolver;
import com.tutorial.util.Assert;

/**
 * Generic ApplicationContext implementation that holds a single internal
 * {@link com.tutorial.beans.factory.support.DefaultListableBeanFactory}
 * instance and does not assume a specific bean definition format. Implements
 * the {@link com.tutorial.beans.factory.support.BeanDefinitionRegistry}
 * interface in order to allow for applying any bean definition readers to it.
 *
 * <p>Typical usage is to register a variety of bean definitions via the
 * {@link com.tutorial.beans.factory.support.BeanDefinitionRegistry}
 * interface and then call {@link #refresh()} to initialize those beans
 * with application context semantics (handling
 * {@link com.tutorial.context.ApplicationContextAware}, auto-detecting
 * {@link com.tutorial.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessors},
 * etc).
 *
 * <p>In contrast to other ApplicationContext implementations that create a new
 * internal BeanFactory instance for each refresh, the internal BeanFactory of
 * this context is available right from the start, to be able to register bean
 * definitions on it. {@link #refresh()} may only be called once.
 *
 * <p>Usage example:
 *
 * <pre>
 * GenericApplicationContext ctx = new GenericApplicationContext();
 * XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
 * xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
 * PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(ctx);
 * propReader.loadBeanDefinitions(new ClassPathResource("otherBeans.properties"));
 * ctx.refresh();
 *
 * MyBean myBean = (MyBean) ctx.getBean("myBean");
 * ...</pre>
 *
 * For the typical case of XML bean definitions, simply use
 * {@link ClassPathXmlApplicationContext} or {@link FileSystemXmlApplicationContext},
 * which are easier to set up - but less flexible, since you can just use standard
 * resource locations for XML bean definitions, rather than mixing arbitrary bean
 * definition formats. The equivalent in a web environment is
 * {@link com.tutorial.web.context.support.XmlWebApplicationContext}.
 *
 * <p>For custom application context implementations that are supposed to read
 * special bean definition formats in a refreshable manner, consider deriving
 * from the {@link AbstractRefreshableApplicationContext} base class.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.2
 * @see #registerBeanDefinition
 * @see #refresh()
 * @see com.tutorial.beans.factory.xml.XmlBeanDefinitionReader
 * @see com.tutorial.beans.factory.support.PropertiesBeanDefinitionReader
 */
public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {
	
	private final DefaultListableBeanFactory beanFactory;
	
	private ResourceLoader resourceLoader;
	
	private boolean refreshed = false;

	/**
	 * Create a new GenericApplicationContext.
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext() {
		this.beanFactory = new DefaultListableBeanFactory();
		this.beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
	}
	
	public GenericApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}
	
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		this(beanFactory);
		setParent(parent);
	}
	
	@Override
	public void setParent(ApplicationContext parent) {
		super.setParent(parent);
		this.beanFactory.setParentBeanFactory(parent);
	}
	
	/**
	 * Set a ResourceLoader to use for this context. If set, the context will
	 * delegate all <code>getResource</code> calls to the given ResourceLoader.
	 * If not set, default resource loading will apply.
	 * <p>The main reason to specify a custom ResourceLoader is to resolve
	 * resource paths (without URL prefix) in a specific fashion.
	 * The default behavior is to resolve such paths as class path locations.
	 * To resolve resource paths as file system locations, specify a
	 * FileSystemResourceLoader here.
	 * <p>You can also pass in a full ResourcePatternResolver, which will
	 * be autodetected by the context and used for <code>getResources</code>
	 * calls as well. Else, default resource pattern matching will apply.
	 * @see #getResource
	 * @see com.tutorial.core.io.DefaultResourceLoader
	 * @see com.tutorial.core.io.FileSystemResourceLoader
	 * @see com.tutorial.core.io.support.ResourcePatternResolver
	 * @see #getResources
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	/**
	 * This implementation delegates to this context's ResourceLoader if set,
	 * falling back to the default superclass behavior else.
	 * @see #setResourceLoader
	 */
	@Override
	public Resource getResource(String location) {
		if(this.resourceLoader != null) {
			return this.resourceLoader.getResource(location);
		}
		return super.getResource(location);
	}
	
	/**
	 * This implementation delegates to this context's ResourceLoader if it
	 * implements the ResourcePatternResolver interface, falling back to the
	 * default superclass behavior else.
	 * @see #setResourceLoader
	 */
	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		if(this.resourceLoader instanceof ResourcePatternResolver) {
			return ((ResourcePatternResolver) resourceLoader).getResources(locationPattern);
		}
		return super.getResources(locationPattern);
	}
	
	//--------------------------------------------------------------------------
	//   Implementation of BeanDefinitionRegistry 
	//--------------------------------------------------------------------------

	public void registerAlias(String name, String alias) {
		this.beanFactory.registerAlias(name, alias);
	}

	public void removeAlias(String alias) {
		this.beanFactory.removeAlias(alias);
	}

	public boolean isAlias(String beanName) {
		return this.beanFactory.isAlias(beanName);
	}

	public BeanDefinition getBeanDefinition(String name) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getBeanDefinition(name);
	}

	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) 
				throws BeanDefinitionStoreException {
		this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		this.beanFactory.removeBeanDefinition(beanName);
	}

	public boolean isBeanNameInUse(String beanName) {
		return this.beanFactory.isBeanNameInUse(beanName);
	}

	//---------------------------------------------------------------------
	// Implementations of AbstractApplicationContext's template methods
	//---------------------------------------------------------------------
	
	/**
	 * Do nothing: We hold a single internal BeanFactory and rely on callers
	 * to register beans through our public methods (or the BeanFactory's).
	 * @see #registerBeanDefinition
	 */
	@Override
	protected void refreshBeanFactory() throws IllegalStateException {
		if(this.refreshed) {
			throw new IllegalStateException(
					"GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
		}
		this.beanFactory.setSerializationId(getId());
		this.refreshed = true;
	}
	
	@Override
	protected void cancelRefresh(BeansException ex) {
		this.beanFactory.setSerializationId(null);
		super.cancelRefresh(ex);
	}
	
	/**
	 * Not much to do: We hold a single internal BeanFactory that will never
	 * get released.
	 */
	@Override
	protected final void closeBeanFactory() {
		this.beanFactory.setSerializationId(null);
	}

	/**
	 * Return the single internal BeanFactory held by this context
	 * (as ConfigurableListableBeanFactory).
	 */
	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}
	
	/**
	 * Return the underlying bean factory of this context,
	 * available for registering bean definitions.
	 * <p><b>NOTE:</b> You need to call {@link #refresh()} to initialize the
	 * bean factory and its contained beans with application context semantics
	 * (autodetecting BeanFactoryPostProcessors, etc).
	 * @return the internal bean factory (as DefaultListableBeanFactory)
	 */
	public final DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return this.beanFactory;
	}

}
