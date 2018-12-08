package com.tutorial.beans.factory.support;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.tutorial.beans.BeanMetadataAttributeAccessor;
import com.tutorial.beans.MutablePropertyValues;
import com.tutorial.beans.factory.config.AutowireCapableBeanFactory;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.config.ConstructorArgumentValues;
import com.tutorial.core.io.DescriptiveResource;
import com.tutorial.core.io.Resource;
import com.tutorial.util.Assert;
import com.tutorial.util.ClassUtils;
import com.tutorial.util.ObjectUtils;
import com.tutorial.util.StringUtils;

public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor 
				implements BeanDefinition, Cloneable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Constant for the default scope name: "", equivalent to singleton status
	 * but to be overridden from a parent bean definition (if applicable).
	 */
	public static final String SCOPE_DEFAULT = "";

	public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE; 
	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;
	
	public static final int DEPENDENCY_CHECK_NONE = 0;
	public static final int DEPENDENCY_CHECK_OBJECTS = 1;
	public static final int DEPENDENCY_CEHCK_SIMPLE = 2;
	public static final int DEPENDENCY_CHECK_ALL = 3;
	
	/**
	 * Constant that indicates the container should attempt to infer the {@link
	 * #setDestroyMethodName destroy method name} for a bean as opposed to explicit
	 * specification of a method name. The value {@value} is specifically designed to
	 * include characters otherwise illegal in a method name, ensuring no possibility of
	 * collisions with a legitimately named methods having the same name.
	 */
	public static final String INFER_METHOD = "(inferred)";
	
	private volatile Object beanClass;
	
	private boolean lazyInit = false;
	private boolean abstractFlag = false;
	private boolean singleton = true;
	private boolean prototype = false;	
	private boolean autowireCandidate = true;
	private boolean primary = false;
	private boolean nonPublicAccessAllowed = true;
	private boolean lenientConstructorResolution = true;
	private boolean enforceInitMethod = true;
	private boolean enforceDestroyMethod = true;
	private boolean synthetic = false;
	
	private ConstructorArgumentValues constructorArgumentValues;
	private MutablePropertyValues propertyValues;
	private MethodOverrides methodOverrides = new MethodOverrides();
	private String initMethodName;
	private String destroyMethodName; 
	private String factoryMethodName;
	private String factoryBeanName;
	private String scope = SCOPE_DEFAULT;
	private String description;
	
	private final Map<String, AutowireCandidateQualifier> qualifiers =
			new LinkedHashMap<String, AutowireCandidateQualifier>(0);
	
	private int autowireMode = AUTOWIRE_NO;
	private int dependencyCheck = DEPENDENCY_CHECK_NONE;
	private int role = BeanDefinition.ROLE_APPLICATION;
	
	private String[] dependsOn;
	
	private Resource resource;
	
	protected AbstractBeanDefinition() {
		this(null, null);
	}

	/**
	 * Create a new AbstractBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	protected AbstractBeanDefinition(BeanDefinition original) {
		setParentName(original.getParentName());
		setBeanClassName(original.getBeanClassName());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setLazyInit(original.isLazyInit());
		setRole(original.getRole());
		setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
		setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
		setSource(original.getSource());
		copyAttributesFrom(original);

		if (original instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			if (originalAbd.hasBeanClass()) {
				setBeanClass(originalAbd.getBeanClass());
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependencyCheck(originalAbd.getDependencyCheck());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			copyQualifiersFrom(originalAbd);
			setPrimary(originalAbd.isPrimary());
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
			setInitMethodName(originalAbd.getInitMethodName());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodName(originalAbd.getDestroyMethodName());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			setSynthetic(originalAbd.isSynthetic());
			setResource(originalAbd.getResource());
		}
		else {
			setResourceDescription(original.getResourceDescription());
		}
	}
	
	/**
	 * Create a new AbstractBeanDefinition with the given
	 * constructor argument values and property values.
	 */
	protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		setConstructorArgumentValues(cargs);
		setPropertyValues(pvs);
	}

	/**
	 * Override settings in this bean definition (assumably a copied parent
	 * from a parent-child inheritance relationship) from the given bean
	 * definition (assumably the child).
	 * <ul>
	 * <li>Will override beanClass if specified in the given bean definition.
	 * <li>Will always take <code>abstract</code>, <code>scope</code>,
	 * <code>lazyInit</code>, <code>autowireMode</code>, <code>dependencyCheck</code>,
	 * and <code>dependsOn</code> from the given bean definition.
	 * <li>Will add <code>constructorArgumentValues</code>, <code>propertyValues</code>,
	 * <code>methodOverrides</code> from the given bean definition to existing ones.
	 * <li>Will override <code>factoryBeanName</code>, <code>factoryMethodName</code>,
	 * <code>initMethodName</code>, and <code>destroyMethodName</code> if specified
	 * in the given bean definition.
	 * </ul>
	 */
	public void overrideFrom(BeanDefinition other) {
		if(StringUtils.hasLength(other.getBeanClassName())) {
			setBeanClassName(other.getBeanClassName());
		}
		if(StringUtils.hasLength(other.getFactoryBeanName())) {
			setFactoryBeanName(other.getFactoryBeanName());
		}
		if(StringUtils.hasLength(other.getFactoryMethodName())) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		if(StringUtils.hasLength(other.getScope())) {
			setScope(other.getScope());
		}
		setAbstract(other.isAbstract());
		setLazyInit(other.isLazyInit());
		setRole(other.getRole());
		
		getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
		getPropertyValues().addPropertyValues(other.getPropertyValues());
		setSource(other.getSource());
		copyAttributesFrom(other);
		
		if (other instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
			if (otherAbd.hasBeanClass()) {
				setBeanClass(otherAbd.getBeanClass());
			}
			setAutowireCandidate(otherAbd.isAutowireCandidate());
			setAutowireMode(otherAbd.getAutowireMode());
			copyQualifiersFrom(otherAbd);
			setPrimary(otherAbd.isPrimary());
			setDependencyCheck(otherAbd.getDependencyCheck());
			setDependsOn(otherAbd.getDependsOn());
			setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
			if (StringUtils.hasLength(otherAbd.getInitMethodName())) {
				setInitMethodName(otherAbd.getInitMethodName());
				setEnforceInitMethod(otherAbd.isEnforceInitMethod());
			}
			if (StringUtils.hasLength(otherAbd.getDestroyMethodName())) {
				setDestroyMethodName(otherAbd.getDestroyMethodName());
				setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
			}
			getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
			setSynthetic(otherAbd.isSynthetic());
			setResource(otherAbd.getResource());
		}
		else {
			setResourceDescription(other.getResourceDescription());
		}
	}
	
	/**
	 * Return whether this definition specifies a bean class.
	 */
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}
	
	/**
	 * Specify the class for this bean.
	 */
	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}
	
	/**
	 * Return the class of the wrapped bean, if already resolved.
	 * @return the bean class, or <code>null</code> if none defined
	 * @throws IllegalStateException if the bean definition does not define a bean class,
	 * or a specified bean class name has not been resolved into an actual Class
	 */
	public Class<?> getBeanClass() throws IllegalStateException {
		Object beanClassObject = this.beanClass;
		if (beanClassObject == null) {
			throw new IllegalStateException("No bean class specified on bean definition");
		}
		if (!(beanClassObject instanceof Class)) {
			throw new IllegalStateException(
					"Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
		}
		return (Class<?>) beanClassObject;
	}
	
	public void setBeanClassName(String beanClassName) {
		this.beanClass = beanClassName;
	}
	
	public String getBeanClassName() {
		if(this.beanClass instanceof Class) {
			return ((Class<?>) this.beanClass).getName();
		} 
		else {
			return (String) this.beanClass;
		}
	}
	
	/**
	 * Determine the class of the wrapped bean, resolving it from a
	 * specified class name if necessary. Will also reload a specified
	 * Class from its name when called with the bean class already resolved.
	 * @param classLoader the ClassLoader to use for resolving a (potential) class name
	 * @return the resolved bean class
	 * @throws ClassNotFoundException if the class name could be resolved
	 */
	public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
		String className = getBeanClassName();
		if(className == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
		this.beanClass = resolvedClass;
		return resolvedClass;
	}
	
	/**
	 * Set the name of the target scope for the bean.
	 * <p>Default is singleton status, although this is only applied once
	 * a bean definition becomes active in the containing factory. A bean
	 * definition may eventually inherit its scope from a parent bean definitionFor this
	 * reason, the default scope name is empty (empty String), with
	 * singleton status being assumed until a resolved scope will be set.
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	public void setScope(String scope) {
		this.scope = scope;
		this.singleton = SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
		this.prototype = SCOPE_PROTOTYPE.equals(scope);
	}
	
	/**
	 * Return the name of the target scope for the bean.
	 */
	public String getScope() {
		return this.scope;
	}
	
	/**
	 * Return whether this a <b>Singleton</b>, with a single shared instance
	 * returned from all calls.
	 * @see #SCOPE_SINGLETON
	 */
	public boolean isSingleton() {
		return this.singleton;
	}

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * @see #SCOPE_PROTOTYPE
	 */
	public boolean isPrototype() {
		return this.prototype;
	}
	
	/**
	 * Set if this bean is "abstract", i.e. not meant to be instantiated itself but
	 * rather just serving as parent for concrete child bean definitions.
	 * <p>Default is "false". Specify true to tell the bean factory to not try to
	 * instantiate that particular bean in any case.
	 */
	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}

	/**
	 * Return whether this bean is "abstract", i.e. not meant to be instantiated
	 * itself but rather just serving as parent for concrete child bean definitions.
	 */
	public boolean isAbstract() {
		return this.abstractFlag;
	}

	/**
	 * Set whether this bean should be lazily initialized.
	 * <p>If <code>false</code>, the bean will get instantiated on startup by bean
	 * factories that perform eager initialization of singletons.
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}


	/**
	 * Set the autowire mode. This determines whether any automagical detection
	 * and setting of bean references will happen. Default is AUTOWIRE_NO,
	 * which means there's no autowire.
	 * @param autowireMode the autowire mode to set.
	 * Must be one of the constants defined in this class.
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 */
	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}
	
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	/**
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized first.
	 * <p>Note that dependencies are normally expressed through bean properties or
	 * constructor arguments. This property should just be necessary for other kinds
	 * of dependencies like statics (*ugh*) or database preparation on startup.
	 */
	public void setDependsOn(String[] dependsOn) {
		this.dependsOn = dependsOn;
	}

	/**
	 * Return the bean names that this bean depends on.
	 */
	public String[] getDependsOn() {
		return this.dependsOn;
	}
	
	/**
	 * Set whether this bean is a candidate for getting autowired into some other bean.
	 */
	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}

	/**
	 * Return whether this bean is a candidate for getting autowired into some other bean.
	 */
	public boolean isAutowireCandidate() {
		return this.autowireCandidate;
	}

	/**
	 * Set whether this bean is a primary autowire candidate.
	 * If this value is true for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 */
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * Return whether this bean is a primary autowire candidate.
	 * If this value is true for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 */
	public boolean isPrimary() {
		return this.primary;
	}
	
	/**
	 * Register a qualifier to be used for autowire candidate resolution,
	 * keyed by the qualifier's type name.
	 * @see AutowireCandidateQualifier#getTypeName()
	 */
	public void addQualifier(AutowireCandidateQualifier qualifier) {
		this.qualifiers.put(qualifier.getTypeName(), qualifier);
	}

	/**
	 * Return whether this bean has the specified qualifier.
	 */
	public boolean hasQualifier(String typeName) {
		return this.qualifiers.keySet().contains(typeName);
	}
	
	/**
	 * Return the qualifier mapped to the provided type name.
	 */
	public AutowireCandidateQualifier getQualifier(String typeName) {
		return this.qualifiers.get(typeName);
	}

	/**
	 * Return all registered qualifiers.
	 * @return the Set of {@link AutowireCandidateQualifier} objects.
	 */
	public Set<AutowireCandidateQualifier> getQualifiers() {
		return new LinkedHashSet<AutowireCandidateQualifier>(this.qualifiers.values());
	}

	/**
	 * Copy the qualifiers from the supplied AbstractBeanDefinition to this bean definition.
	 * @param source the AbstractBeanDefinition to copy from
	 */
	public void copyQualifiersFrom(AbstractBeanDefinition source) {
		Assert.notNull(source, "Source must not be null");
		this.qualifiers.putAll(source.qualifiers);
	}
	
	/**
	 * Specify whether to allow access to non-public constructors and methods,
	 * for the case of externalized metadata pointing to those.
	 * <p>This applies to constructor resolution, factory method resolution,
	 * and also init/destroy methods. Bean property accessors have to be public
	 * in any case and are not affected by this setting.
	 * <p>Note that annotation-driven configuration will still access non-public
	 * members as far as they have been annotated. This setting applies to
	 * externalized metadata in this bean definition only.
	 */
	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}

	/**
	 * Return whether to allow access to non-public constructors and methods.
	 */
	public boolean isNonPublicAccessAllowed() {
		return this.nonPublicAccessAllowed;
	}

	/**
	 * Specify whether to resolve constructors in lenient mode (<code>true</code>,
	 * which is the default) or to switch to strict resolution (throwing an exception
	 * in case of ambigious constructors that all match when converting the arguments,
	 * whereas lenient mode would use the one with the 'closest' type matches).
	 */
	public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
		this.lenientConstructorResolution = lenientConstructorResolution;
	}

	/**
	 * Return whether to resolve constructors in lenient mode or in strict mode.
	 */
	public boolean isLenientConstructorResolution() {
		return this.lenientConstructorResolution;
	}
	
	/**
	 * Specify constructor argument values for this bean.
	 */
	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues =
				(constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
	}

	/**
	 * Return constructor argument values for this bean (never <code>null</code>).
	 */
	public ConstructorArgumentValues getConstructorArgumentValues() {
		return this.constructorArgumentValues;
	}

	/**
	 * Return if there are constructor argument values defined for this bean.
	 */
	public boolean hasConstructorArgumentValues() {
		return !this.constructorArgumentValues.isEmpty();
	}

	/**
	 * Specify property values for this bean, if any.
	 */
	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
	}

	/**
	 * Return property values for this bean (never <code>null</code>).
	 */
	public MutablePropertyValues getPropertyValues() {
		return this.propertyValues;
	}

	/**
	 * Specify method overrides for the bean, if any.
	 */
	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = (methodOverrides != null ? methodOverrides : new MethodOverrides());
	}

	/**
	 * Return information about methods to be overridden by the IoC
	 * container. This will be empty if there are no method overrides.
	 * Never returns null.
	 */
	public MethodOverrides getMethodOverrides() {
		return this.methodOverrides;
	}


	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	public String getFactoryMethodName() {
		return this.factoryMethodName;
	}

	/**
	 * Set the name of the initializer method. The default is <code>null</code>
	 * in which case there is no initializer method.
	 */
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	/**
	 * Return the name of the initializer method.
	 */
	public String getInitMethodName() {
		return this.initMethodName;
	}

	/**
	 * Specify whether or not the configured init method is the default.
	 * Default value is <code>false</code>.
	 * @see #setInitMethodName
	 */
	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}

	/**
	 * Indicate whether the configured init method is the default.
	 * @see #getInitMethodName()
	 */
	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}

	/**
	 * Set the name of the destroy method. The default is <code>null</code>
	 * in which case there is no destroy method.
	 */
	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	/**
	 * Return the name of the destroy method.
	 */
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

	/**
	 * Specify whether or not the configured destroy method is the default.
	 * Default value is <code>false</code>.
	 * @see #setDestroyMethodName
	 */
	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}

	/**
	 * Indicate whether the configured destroy method is the default.
	 * @see #getDestroyMethodName
	 */
	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
	}


	/**
	 * Set whether this bean definition is 'synthetic', that is, not defined
	 * by the application itself (for example, an infrastructure bean such
	 * as a helper for auto-proxying, created through <code>&ltaop:config&gt;</code>).
	 */
	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	/**
	 * Return whether this bean definition is 'synthetic', that is,
	 * not defined by the application itself.
	 */
	public boolean isSynthetic() {
		return this.synthetic;
	}

	/**
	 * Set the role hint for this <code>BeanDefinition</code>.
	 */
	public void setRole(int role) {
		this.role = role;
	}

	/**
	 * Return the role hint for this <code>BeanDefinition</code>.
	 */
	public int getRole() {
		return this.role;
	}


	/**
	 * Set a human-readable description of this bean definition.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the resource that this bean definition came from
	 * (for the purpose of showing context in case of errors).
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Return the resource that this bean definition came from.
	 */
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * Set a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 */
	public void setResourceDescription(String resourceDescription) {
		this.resource = new DescriptiveResource(resourceDescription);
	}

	public String getResourceDescription() {
		return (this.resource != null ? this.resource.getDescription() : null);
	}

	/**
	 * Set the originating (e.g. decorated) BeanDefinition, if any.
	 */
	public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
		this.resource = new BeanDefinitionResource(originatingBd);
	}

	public BeanDefinition getOriginatingBeanDefinition() {
		return (this.resource instanceof BeanDefinitionResource ?
				((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
	}

	public int getAutowireMode() {
		return autowireMode;
	}
	
	/**
	 * Set the dependency check code.
	 * @param dependencyCheck the code to set.
	 * Must be one of the four constants defined in this class.
	 * @see #DEPENDENCY_CHECK_NONE
	 * @see #DEPENDENCY_CHECK_OBJECTS
	 * @see #DEPENDENCY_CHECK_SIMPLE
	 * @see #DEPENDENCY_CHECK_ALL
	 */
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * Return the dependency check code.
	 */
	public int getDependencyCheck() {
		return this.dependencyCheck;
	}
	
	/**
	 * Return the resolved autowire code,
	 * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public int getResolvedAutowireMode() {
		if(this.autowireMode == AUTOWIRE_AUTODETECT) {
			Constructor<?>[] constructors = getBeanClass().getConstructors();
			for(int i = 0; i < constructors.length; i++) {
				if(constructors[i].getParameterTypes().length == 0) {
					return AUTOWIRE_BY_TYPE;
				}
			}
			return AUTOWIRE_CONSTRUCTOR;
		} 
		else {
			return this.autowireMode;
		}
	}
	
	/**
	 * 验证bean定义的有效性
	 * @throws BeanDefinitionValidationException
	 */
	public void validate() throws BeanDefinitionValidationException {
		if(!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
			throw new  BeanDefinitionValidationException(
				    "Cannot combine static factory method with method overrides: " +
				    "the static factory method must create the instance");
		}
		if(hasBeanClass()) {
			prepareMethodOverrides();
		}
	}

	/**
	 * Validate and prepare the method overrides defined for this bean.
	 * Checks for existence of a method with the specified name.
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	public void prepareMethodOverrides() throws BeanDefinitionValidationException {
		MethodOverrides methodOverrides = getMethodOverrides();
		if(!methodOverrides.isEmpty()) {
			for(MethodOverride mo : methodOverrides.getOverrides()) {
				prepareMethodOverride(mo);
			}
		}
	}

	/**
	 * Validate and prepare the given method override.
	 * Checks for existence of a method with the specified name,
	 * marking it as not overloaded if none found.
	 * @param mo the MethodOverride object to validate
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	protected void prepareMethodOverride(MethodOverride mo)  throws BeanDefinitionValidationException {
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if(count == 0) {
			throw new BeanDefinitionValidationException(
					"Invalid method override: no method with name '" + mo.getMethodName() +
					"' on class [" + getBeanClassName() + "]");
		}
		else if(count == 1) {
			// Mark override as not overloaded, to avoid the overhead of arg type checking.
			mo.setOverloaded(false);
		}
	}
	
	/**
	 * Public declaration of Object's <code>clone()</code> method.
	 * Delegates to {@link #cloneBeanDefinition()}.
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return cloneBeanDefinition();
	}

	/**
	 * Clone this bean definition.
	 * To be implemented by concrete subclasses.
	 * @return the cloned bean definition object
	 */
	public abstract AbstractBeanDefinition cloneBeanDefinition();

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeanDefinition)) {
			return false;
		}

		AbstractBeanDefinition that = (AbstractBeanDefinition) other;

		if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
		if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
		if (this.abstractFlag != that.abstractFlag) return false;
		if (this.lazyInit != that.lazyInit) return false;

		if (this.autowireMode != that.autowireMode) return false;
		if (this.dependencyCheck != that.dependencyCheck) return false;
		if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
		if (this.autowireCandidate != that.autowireCandidate) return false;
		if (!ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers)) return false;
		if (this.primary != that.primary) return false;

		if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
		if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
		if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides)) return false;

		if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
		if (this.enforceInitMethod != that.enforceInitMethod) return false;
		if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
		if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

		if (this.synthetic != that.synthetic) return false;
		if (this.role != that.role) return false;

		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
		hashCode = 29 * hashCode + super.hashCode();
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class [");
		sb.append(getBeanClassName()).append("]");
		sb.append("; scope=").append(this.scope);
		sb.append("; abstract=").append(this.abstractFlag);
		sb.append("; lazyInit=").append(this.lazyInit);
		sb.append("; autowireMode=").append(this.autowireMode);
		sb.append("; dependencyCheck=").append(this.dependencyCheck);
		sb.append("; autowireCandidate=").append(this.autowireCandidate);
		sb.append("; primary=").append(this.primary);
		sb.append("; factoryBeanName=").append(this.factoryBeanName);
		sb.append("; factoryMethodName=").append(this.factoryMethodName);
		sb.append("; initMethodName=").append(this.initMethodName);
		sb.append("; destroyMethodName=").append(this.destroyMethodName);
		if (this.resource != null) {
			sb.append("; defined in ").append(this.resource.getDescription());
		}
		return sb.toString();
	}

}