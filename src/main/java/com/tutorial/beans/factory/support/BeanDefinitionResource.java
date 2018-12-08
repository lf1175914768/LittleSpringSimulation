package com.tutorial.beans.factory.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.core.io.AbstractResource;
import com.tutorial.util.Assert;

/**
 * Descriptive {@link com.tutorial.core.io.Resource} wrapper for
 * a {@link com.tutorial.beans.factory.config.BeanDefinition}.
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see com.tutorial.core.io.DescriptiveResource
 */
public class BeanDefinitionResource extends AbstractResource {
	
	private final BeanDefinition beanDefinition;
	
	/**
	 * Create a new BeanDefinitionResource.
	 * @param beanDefinition the BeanDefinition objectto wrap
	 */
	public BeanDefinitionResource(BeanDefinition beanDefinition) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		this.beanDefinition = beanDefinition;
	}
	
	/**
	 * Return the wrapped BeanDefinition object.
	 */
	public final BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}
	
	@Override
	public boolean exists() {
		return false;
	}
	
	@Override
	public boolean isReadable() {
		return false;
	}
	
	public InputStream getInputStream() throws IOException {
		throw new FileNotFoundException(
				"Resource cannot be opened because it points to " + getDescription());
	}

	public String getDescription() {
		return "BeanDefinition defined in " + this.beanDefinition.getResourceDescription();
	}
	
	/**
	 * This implementation compares the underlying BeanDefinition.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
			(obj instanceof BeanDefinitionResource &&
						((BeanDefinitionResource) obj).beanDefinition.equals(this.beanDefinition)));
	}

	/**
	 * This implementation returns the hash code of the underlying BeanDefinition.
	 */
	@Override
	public int hashCode() {
		return this.beanDefinition.hashCode();
	}

}
