package com.tutorial.core.io.support;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tutorial.core.env.Environment;
import com.tutorial.core.env.PropertyResolver;
import com.tutorial.core.env.StandardEnvironment;
import com.tutorial.core.io.Resource;

/**
 * Editor for {@link org.springframework.core.io.Resource} arrays, to
 * automatically convert <code>String</code> location patterns
 * (e.g. <code>"file:C:/my*.txt"</code> or <code>"classpath*:myfile.txt"</code>)
 * to <code>Resource</code> array properties. Can also translate a collection
 * or array of location patterns into a merged Resource array.
 *
 * <p>A path may contain <code>${...}</code> placeholders, to be
 * resolved as {@link org.springframework.core.env.Environment} properties:
 * e.g. <code>${user.dir}</code>. Unresolvable placeholders are ignored by default.
 *
 * <p>Delegates to a {@link ResourcePatternResolver},
 * by default using a {@link PathMatchingResourcePatternResolver}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.2
 * @see org.springframework.core.io.Resource
 * @see ResourcePatternResolver
 * @see PathMatchingResourcePatternResolver
 */
public class ResourceArrayPropertyEditor extends PropertyEditorSupport {
	
	private static final Log logger = LogFactory.getLog(ResourceArrayPropertyEditor.class);

	private final ResourcePatternResolver resourcePatternResolver;
	
	private final PropertyResolver propertyResolver;
	
	private final boolean ignoreUnResolvablePlaceholders;
	
	/**
	 * Create a new ResourceArrayPropertyEditor with a default
	 * {@link PathMatchingResourcePatternResolver} and {@link StandardEnvironment}.
	 * @see PathMatchingResourcePatternResolver
	 * @see Environment
	 */
	public ResourceArrayPropertyEditor() {
		this(new PathMatchingResourcePatternResolver(), new StandardEnvironment(), true);
	}
	
	/**
	 * Create a new ResourceArrayPropertyEditor with the given {@link ResourcePatternResolver}
	 * and {@link PropertyResolver} (typically an {@link Environment}).
	 * @param resourcePatternResolver the ResourcePatternResolver to use
	 * @param propertyResolver the PropertyResolver to use
	 */
	public ResourceArrayPropertyEditor(ResourcePatternResolver resourcePatternResolver, PropertyResolver propertyResolver) {
		this(resourcePatternResolver, propertyResolver, true);
	}
	
	/**
	 * Create a new ResourceArrayPropertyEditor with the given {@link ResourcePatternResolver}
	 * and {@link PropertyResolver} (typically an {@link Environment}).
	 * @param resourcePatternResolver the ResourcePatternResolver to use
	 * @param propertyResolver the PropertyResolver to use
	 * @param ignoreUnresolvablePlaceholders whether to ignore unresolvable placeholders
	 * if no corresponding system property could be found
	 */
	public ResourceArrayPropertyEditor(ResourcePatternResolver resourcePattern, 
			PropertyResolver propertyResolver, boolean ignoreUnResolvablePlaceholders) {
		this.resourcePatternResolver = resourcePattern;
		this.propertyResolver = propertyResolver;
		this.ignoreUnResolvablePlaceholders = ignoreUnResolvablePlaceholders;
	}
	
	/**
	 * Treat the given text as a location pattern and convert it to a Resource array.
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		String pattern = resolvePath(text).trim();
		try {
			setValue(this.resourcePatternResolver.getResources(pattern));
		} catch (IOException ex) {
			throw new IllegalArgumentException(
					"Could not resolve resource location pattern [" + pattern + "]: " + ex.getMessage());
		}
	}
	
	/**
	 * Treat the given value as a collection or array and convert it to a Resource array.
	 * Considers String elements as location patterns and takes Resource elements as-is.
	 */
	@Override
	public void setValue(Object value) {
		if(value instanceof Collection || (value instanceof Object[] && !(value instanceof Resource[]))) {
			Collection<?> input = (value instanceof Collection ? (Collection<?>) value : Arrays.asList((Object[]) value));
			List<Resource> merged = new ArrayList<Resource>();
			for(Object element : input) {
				if(element instanceof String) {
					// A location pattern: resolve it into a Resource array.
					// Might point to a single resource or to multiple resources.
					String pattern = resolvePath((String) element).trim();
					try {
						Resource[] resources = this.resourcePatternResolver.getResources(pattern);
						for(Resource resource : resources) {
							if(!merged.contains(resource)) {
								merged.add(resource);
							}
						}
					} catch (IOException ex) {
						// ignore - might be an unresolved placeholder or non-existing base directory
						if (logger.isDebugEnabled()) {
							logger.debug("Could not retrieve resources for pattern '" + pattern + "'", ex);
						}
					}
				} else if(element instanceof Resource) {
					// A resource object : add it into the result.
					Resource resource = (Resource) element;
					if(!merged.contains(resource)) {
						merged.add(resource);
					}
				} else {
					throw new IllegalArgumentException("Cannot convert element [" + element + "] to [" +
							Resource.class.getName() + "]: only location String and Resource object supported");
				}
			}
			super.setValue(merged.toArray(new Resource[merged.size()]));
 		} else {
 			// An arbitrary value: probably a String or a Resource array.
 			// setAsText will be called for a String; a Resource array will be used as-is.
 			super.setValue(value);
 		}
	}

	/**
	 * Resolve the given path, replacing placeholders with
	 * corresponding system property values if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see PropertyResolver#resolvePlaceholders
	 * @see PropertyResolver#resolveRequiredPlaceholders(String)
	 */
	protected String resolvePath(String path) {
		return this.ignoreUnResolvablePlaceholders ? 
				this.propertyResolver.resolvePlaceHolders(path) :
					this.propertyResolver.resolveRequiredPlaceHolders(path);
	}
	
}
