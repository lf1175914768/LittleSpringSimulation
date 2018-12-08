package com.tutorial.core.io;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import com.tutorial.core.env.PropertyResolver;
import com.tutorial.core.env.StandardEnvironment;
import com.tutorial.util.Assert;
import com.tutorial.util.StringUtils;

/**
 * {@link java.beans.PropertyEditor Editor} for {@link Resource}
 * descriptors, to automatically convert {@code String} locations
 * e.g. {@code file:C:/myfile.txt} or {@code classpath:myfile.txt} to
 * {@code Resource} properties instead of using a {@code String} location property.
 *
 * <p>The path may contain <code>${...}</code> placeholders, to be
 * resolved as {@link org.springframework.core.env.Environment} properties:
 * e.g. <code>${user.dir}</code>. Unresolvable placeholders are ignored by default.
 *
 * <p>Delegates to a {@link ResourceLoader} to do the heavy lifting,
 * by default using a {@link DefaultResourceLoader}.
 *
 * @author Juergen Hoeller
 * @author Dave Syer
 * @author Chris Beams
 * @since 28.12.2003
 * @see Resource
 * @see ResourceLoader
 * @see DefaultResourceLoader
 * @see PropertyResolver#resolvePlaceholders
 */
public class ResourceEditor extends PropertyEditorSupport {
	
	private final PropertyResolver propertyResolver;
	
	private final boolean ignoreUnresolvablePlaceholders;
	
	private final ResourceLoader resourceLoader; 
	
	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using a {@link DefaultResourceLoader} and {@link StandardEnvironment}.
	 */
	public ResourceEditor() {
		this(new DefaultResourceLoader(), new StandardEnvironment());
	}
	
	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader} and {@link PropertyResolver}.
	 * @param resourceLoader the <code>ResourceLoader</code> to use
	 * @param propertyResolver the <code>PropertyResolver</code> to use
	 */
	public ResourceEditor(ResourceLoader resourceLoader, PropertyResolver propertyResolver) {
		this(resourceLoader, propertyResolver, true);
	}
	
	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader}.
	 * @param resourceLoader the <code>ResourceLoader</code> to use
	 * @param propertyResolver the <code>PropertyResolver</code> to use
	 * @param ignoreUnresolvablePlaceholders whether to ignore unresolvable placeholders
	 * if no corresponding property could be found in the given <code>propertyResolver</code>
	 */
	public ResourceEditor(ResourceLoader loader, PropertyResolver propertyResolver, boolean ignoreUnresolvablePlaceholders) {
		Assert.notNull(loader, "ResourceLoader must not be null");
		Assert.notNull(propertyResolver, "PropertyResolver must not be null");
		this.resourceLoader = loader;
		this.propertyResolver = propertyResolver;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(StringUtils.hasText(text)) {
			String locationToUse = resolvePath(text).trim();
			setValue(this.resourceLoader.getResource(locationToUse));
		} else { 
			setValue(null);
		}
	}

	/**
	 * Resolve the given path, replacing placeholders with corresponding
	 * property values from the <code>environment</code> if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see PropertyResolver#resolvePlaceholders
	 * @see PropertyResolver#resolveRequiredPlaceholders
	 */
	protected String resolvePath(String path) {
		return this.ignoreUnresolvablePlaceholders ? 
				this.propertyResolver.resolvePlaceHolders(path) :
					this.propertyResolver.resolveRequiredPlaceHolders(path);
	}
	
	@Override
	public String getAsText() {
		Resource value = (Resource) getValue();
		try {
			return value != null ? value.getURL().toExternalForm() : "";
		} catch (IOException e) {
			return null;
		}
	}
}
