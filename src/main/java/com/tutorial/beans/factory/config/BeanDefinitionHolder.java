package com.tutorial.beans.factory.config;

import com.tutorial.beans.BeanMetadataElement;
import com.tutorial.util.Assert;
import com.tutorial.util.ObjectUtils;
import com.tutorial.util.StringUtils;

/**
 * Holder for a BeanDefinition with name and aliases.
 *
 * <p>Recognized by AbstractAutowireCapableBeanFactory for inner
 * bean definitions. Registered by DefaultXmlBeanDefinitionParser,
 * which also uses it as general holder for a parsed bean definition.
 *
 * <p>Can also be used for programmatic registration of inner bean
 * definitions. If you don't care about BeanNameAware and the like,
 * registering RootBeanDefinition or ChildBeanDefinition is good enough.
 *
 * @author Liufeng
 * @since 2.12.2017
 * @see com.tutorial.beans.factory.support.AbstractAutowireCapableBeanFactory#resolveValueIfNecessary
 * @see com.tutorial.beans.factory.xml.DefaultXmlBeanDefinitionParser#parseBeanDefinition
 * @see com.tutorial.beans.factory.BeanNameAware
 */
public class BeanDefinitionHolder implements BeanMetadataElement {
	private final BeanDefinition beanDefinition;
	private final String beanName;
	private final String[] aliases;
	
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}
	
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}
	
	/**
	 * Copy constructor: Create a new BeanDefinitionHolder with the
	 * same contents as the given BeanDefinitionHolder instance.
	 * <p>Note: The wrapped BeanDefinition reference is taken as-is;
	 * it is <code>not</code> deeply copied.
	 * @param beanDefinitionHolder the BeanDefinitionHolder to copy
	 */
	public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
		Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
		this.beanName = beanDefinitionHolder.getBeanName();
		this.aliases = beanDefinitionHolder.getAliases();
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public String getBeanName() {
		return beanName;
	}

	public String[] getAliases() {
		return aliases;
	}

	/**
	 * Expose the bean definition's source object.
	 * @see BeanDefinition#getSource()
	 */
	public Object getSource() {
		return this.beanDefinition.getSource();
	}
	
	/**
	 * Determine whether the given candidate name matches the bean name
	 * or the aliases stored in this bean definition.
	 */
	public boolean matchesName(String candidateName) {
		return (candidateName != null &&
				(candidateName.equals(this.beanName) || ObjectUtils.containsElement(this.aliases, candidateName)));
	}
	
	/**
	 * Return a friendly, short description for the bean, stating name and aliases.
	 * @see #getBeanName()
	 * @see #getAliases()
	 */
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bean definition with name '").append(this.beanName).append("'");
		if (this.aliases != null) {
			sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
		}
		return sb.toString();
	}
	
	/**
	 * Return a long description for the bean, including name and aliases
	 * as well as a description of the contained {@link BeanDefinition}.
	 * @see #getShortDescription()
	 * @see #getBeanDefinition()
	 */
	public String getLongDescription() {
		StringBuilder sb = new StringBuilder(getShortDescription());
		sb.append(": ").append(this.beanDefinition);
		return sb.toString();
	}
	
	/**
	 * This implementation returns the long description. Can be overridden
	 * to return the short description or any kind of custom description instead.
	 * @see #getLongDescription()
	 * @see #getShortDescription()
	 */
	@Override
	public String toString() {
		return getLongDescription();
	}
	
	@Override 
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof BeanDefinitionHolder)) {
			return false;
		}
		BeanDefinitionHolder other = (BeanDefinitionHolder) obj;
		return this.beanDefinition.equals(other.getBeanDefinition()) &&
				this.beanName.equals(other.getBeanName()) &&
				 ObjectUtils.nullSafeEquals(this.aliases, other.getAliases());
	}
	
	@Override
	public int hashCode() {
		int hashCode = this.beanDefinition.hashCode();
		hashCode = 29 * hashCode + this.beanName.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
		return hashCode;
	}
}
