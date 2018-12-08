package com.tutorial.core.env;

import com.tutorial.core.convert.ConversionException;
import com.tutorial.util.ClassUtils;

/**
 * {@link PropertyResolver} implementation that resolves property values against
 * an underlying set of {@link PropertySources}.
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertySource
 * @see PropertySources
 * @see AbstractEnvironment
 */
public class PropertySourcesPropertyResolver extends AbstractPropertyResolver {

	private final PropertySources propertySources;
	
	/**
	 * Create a new resolver against the given property sources.
	 * @param propertySources the set of {@link PropertySource} objects to use
	 */
	public PropertySourcesPropertyResolver(PropertySources propertySources) {
		this.propertySources = propertySources;
	}
	
	public boolean containsProperty(String key) {
		for(PropertySource<?> propertySource : propertySources) {
			if(propertySource.getProperty(key) != null) {
				return true;
			}
		}
		return false;
	}

	public String getProperty(String key) {
		if(logger.isTraceEnabled()) {
			logger.trace(String.format("getProperty(\"%s\") (implicit targetType [String])", key));
		}
		return this.getProperty(key, String.class);
	}

	public <T> T getProperty(String key, Class<T> targetType) {
		boolean debugEnabled = logger.isDebugEnabled();
		if(logger.isTraceEnabled()) {
			logger.trace(String.format("getProperty(\"%s\", %s)", key, targetType.getSimpleName()));
		}
		
		for(PropertySource<?> propertySource : propertySources) {
			if(debugEnabled) {
				logger.debug(String.format("Searching for key '%s' in [%s]", key, propertySource.getName()));
			}
			Object value;
			if((value = propertySource.getProperty(key)) != null) {
				Class<?> valueType = value.getClass();
				if (debugEnabled) {
					logger.debug(
							String.format("Found key '%s' in [%s] with type [%s] and value '%s'",
									key, propertySource.getName(), valueType.getSimpleName(), value));
				}
				if(!this.conversionService.canConvert(valueType, targetType)) {
					throw new IllegalArgumentException(
							String.format("Cannot convert value [%s] from source type [%s] to target type [%s]",
									value, valueType.getSimpleName(), targetType.getSimpleName()));
				}
				return conversionService.convert(value, targetType);
			}
		}
		if (debugEnabled) {
			logger.debug(String.format("Could not find key '%s' in any property source. Returning [null]", key));
		}
		return null;
	}
	
	public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
		boolean debugEnabled = logger.isDebugEnabled();
		if(logger.isTraceEnabled()) {
			logger.trace(String.format("getPropertyAsClass(\"%s\", %s)", key, targetType.getSimpleName()));
		}
		
		for(PropertySource<?> propertySource : this.propertySources) {
			if(debugEnabled) {
				logger.debug(String.format("Searching for key '%s' in [%s]", key, propertySource.getName()));
			}
			Object value;
			if((value = propertySource.getProperty(key)) != null) {
				if (debugEnabled) {
					logger.debug(
							String.format("Found key '%s' in [%s] with value '%s'", key, propertySource.getName(), value));
				}
				Class<?> clazz; 
				if(value instanceof String) {
					try {
						clazz = ClassUtils.forName((String) value, null);
					} catch (Exception e) {
						throw new ClassConversionException((String)value, targetType, e);
					}
				} else if(value instanceof Class) {
					clazz = (Class<?>) value;
				} else {
					clazz = value.getClass();
				}
				
				if(!targetType.isAssignableFrom(clazz)) {
					throw new ClassConversionException(clazz, targetType);
				}
				@SuppressWarnings("unchecked")
				Class<T> targetClass = (Class<T>) clazz;
				return targetClass;
			}
		}
		if(debugEnabled) {
			logger.debug(String.format("Could not find key '%s' in any property source. Returning [null]", key));
		}
		return null;
	}

	@SuppressWarnings("serial")
	static class ClassConversionException extends ConversionException {
		public ClassConversionException(Class<?> actual, Class<?> expected) {
			super(String.format("Actual type %s is not assignable to expected type %s", actual.getName(), expected.getName()));
		}
		public ClassConversionException(String actual, Class<?> expected, Exception e) {
			super(String.format("Could not find/load class %s during attempt to convert to %s", actual, expected.getSimpleName()), e);
		}
	}

}
