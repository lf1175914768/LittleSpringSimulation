package com.tutorial.beans.factory.support;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tutorial.beans.MutablePropertyValues;
import com.tutorial.beans.factory.FactoryBean;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.config.BeanDefinitionHolder;
import com.tutorial.beans.factory.config.ConstructorArgumentValues;
import com.tutorial.util.Assert;

/**
 * A root bean definition represents the merged bean definition that backs
 * a specific bean in a Spring BeanFactory at runtime. It might have been created
 * from multiple original bean definitions that inherit from each other,
 * typically registered as {@link GenericBeanDefinition GenericBeanDefinitions}.
 * A root bean definition is essentially the 'unified' bean definition view at runtime.
 *
 * <p>Root bean definitions may also be used for registering individual bean definitions
 * in the configuration phase. However, since Spring 2.5, the preferred way to register
 * bean definitions programmatically is the {@link GenericBeanDefinition} class.
 * GenericBeanDefinition has the advantage that it allows to dynamically define
 * parent dependencies, not 'hard-coding' the role as a root bean definition.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

	private static final long serialVersionUID = 1L;
	
	private final Set<Member> externallyManagedConfigMembers = Collections.synchronizedSet(new HashSet<Member>(0));
	
	private final Set<String> externallyManagedInitMethods = Collections.synchronizedSet(new HashSet<String>(0));
	
	private final Set<String> externallyManagedDestroyMethods = Collections.synchronizedSet(new HashSet<String>(0));
	
	private BeanDefinitionHolder decoratedDefinition;
	
	private boolean isFactoryMethodUnique;
	
	/** Package-visible field for caching the resolved constructor or factory method */
	Object resolvedConstructorOrFactoryMethod;
	
	/** Package-visible field that marks the constructor arguments as resolved */
	boolean constructorArgumentsResolved = false;
	
	/** Package-visible field for caching fully resolved constructor arguments */
	Object[] resolvedConstructorArgument;
	
	/** Package-visible field for caching partly prepared constructor arguments */
	Object[] preparedConstructorArguments;
	
	/** Package-visible field that indicates a before-instantiation post-processor having kicked in */
	volatile Boolean beforeInstantiationResolved;
	
	/** Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied */
	boolean postProcessed = false;
	
	final Object constructorArgumentLock = new Object();
	
	final Object postProcessingLock = new Object();
	
	/**
	 * Create a new RootBeanDefinition, to be configured through its bean
	 * properties and configuration methods.
	 * @see #setBeanClass
	 * @see #setBeanClassName
	 * @see #setScope
	 * @see #setAutowireMode
	 * @see #setDependencyCheck
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public RootBeanDefinition() {
		super();
	}
	
	/**
	 * Create a new RootBeanDefinition for a singleton.
	 * @param beanClass the class of the bean to instantiate
	 */
	public RootBeanDefinition(Class<?> beanClass) {
		super();
		setBeanClass(beanClass);
	}
	
	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @deprecated as of Spring 3.0, in favor of {@link #setAutowireMode} usage
	 */
	@Deprecated
	public RootBeanDefinition(Class<?> beanClass, int autowireMode) {
		setBeanClass(beanClass);
		setPropertyValues(null);
		setAutowireMode(autowireMode);
	}
	
	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 */
	public RootBeanDefinition(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if(dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
	}
	
	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param pvs the property values to apply
	 * @deprecated as of Spring 3.0, in favor of {@link #getPropertyValues} usage
	 */
	@Deprecated
	public RootBeanDefinition(Class<?> beanClass, MutablePropertyValues pvs) {
		super(null, pvs);
		setBeanClass(beanClass);
	}
	
	/**
	 * Create a new RootBeanDefinition with the given singleton status,
	 * providing property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param pvs the property values to apply
	 * @param singleton the singleton status of the bean
	 * @deprecated since Spring 2.5, in favor of {@link #setScope}
	 */
	@Deprecated
	public RootBeanDefinition(Class<?> beanClass, MutablePropertyValues pvs, boolean singleton) {
		setBeanClass(beanClass);
		setPropertyValues(pvs);
		setSingleton(singleton);
	}
	
	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClass(beanClass);
	}
	
	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * @param beanClassName the name of the class to instantiate
	 */
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * @param beanClassName the name of the class to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}
	
	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	public RootBeanDefinition(RootBeanDefinition original) {
		this((BeanDefinition) original);
	}
	
	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	RootBeanDefinition(BeanDefinition original) {
		super(original);
		if(original instanceof RootBeanDefinition) {
			RootBeanDefinition originalRbd = (RootBeanDefinition) original;
			this.decoratedDefinition = originalRbd.getDecoratedDefinition();
			this.isFactoryMethodUnique = originalRbd.isFactoryMethodUnique();
		}
	}
	
	/**
	 * Specify a factory method name that refers to a non-overloaded method.
	 */
	public void setUniqueFactoryMethodName(String name) {
		Assert.notNull(name, "Factory method name must not be null");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}
	
	/**
	 * Check whether the given candidate qualifies as a factory method.
	 */
	public boolean isFactoryMethod(Method candidate) {
		return candidate != null && candidate.getName().equals(getFactoryMethodName());
	}
	
	/**
	 * Return the resolved factory method as a Java Method object, if available.
	 * @return the factory method, or <code>null</code> if not found or not resolved yet
	 */
	public Method getResolvedFactoryMethod() {
		synchronized (this.constructorArgumentLock) {
			Object candidate = this.resolvedConstructorOrFactoryMethod;
			return (candidate instanceof Method ? (Method) candidate : null);
		}
	}
	
	public void registerExternallyManagedConfigMember(Member configMember) {
		this.externallyManagedConfigMembers.add(configMember);
	}
	
	public boolean isExternallyManagedConfigMember(Member configMember) {
		return this.externallyManagedConfigMembers.contains(configMember);
	}
	
	public void registerExternallyManagedInitMethod(String initMethod) {
		this.externallyManagedInitMethods.add(initMethod);
	}

	public boolean isExternallyManagedInitMethod(String initMethod) {
		return this.externallyManagedInitMethods.contains(initMethod);
	}

	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		this.externallyManagedDestroyMethods.add(destroyMethod);
	}

	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		return this.externallyManagedDestroyMethods.contains(destroyMethod);
	}

	public String getParentName() {
		return null;
	}

	public void setParentName(String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}
	
	public BeanDefinitionHolder getDecoratedDefinition() {
		return decoratedDefinition;
	}
	
	public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	public boolean isFactoryMethodUnique() {
		return isFactoryMethodUnique;
	}

	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}
	
	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}
}
