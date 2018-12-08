package com.tutorial.core.env;

import static com.tutorial.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static com.tutorial.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;
import static com.tutorial.util.SystemPropertyUtils.VALUE_SEPARATOR;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tutorial.core.convert.support.ConfigurableConversionService;
import com.tutorial.core.convert.support.DefaultConversionService;
import com.tutorial.util.PropertyPlaceHolderHelper;
import com.tutorial.util.PropertyPlaceHolderHelper.PlaceholderResolver;

/**
 * Abstract base class for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @since 3.1
 */
public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

	protected final Log logger = LogFactory.getLog(getClass());
	
	private PropertyPlaceHolderHelper nonStrictHelper;
	private PropertyPlaceHolderHelper strictHelper;
	
	private String placeholderPrefix = PLACEHOLDER_PREFIX;
	private String placeholderSuffix = PLACEHOLDER_SUFFIX;
	private String valueSeparator = VALUE_SEPARATOR;
	
	protected ConfigurableConversionService conversionService = new DefaultConversionService();
	
	private final Set<String> requiredProperties = new LinkedHashSet<String>();

	public ConfigurableConversionService getConversionService() {
		return this.conversionService;
	}

	public void setConversionService(ConfigurableConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value == null ? defaultValue : value;
	}

	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		T value = getProperty(key, targetType);
		return value == null ? defaultValue : value;
	}
	
	public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
		T value = getProperty(key, targetType);
		if (value == null) {
			throw new IllegalStateException(String.format("required key [%s] not found", key));
		}
		return value;
	}

	public String getRequiredProperty(String key) throws IllegalStateException {
		String value = getProperty(key);
		if (value == null) {
			throw new IllegalStateException(String.format("required key [%s] not found", key));
		}
		return value;
	}
	
	public String resolvePlaceHolders(String text) {
		if(nonStrictHelper == null) {
			nonStrictHelper = createPlaceHolderHelper(true);
		}
		return doResolvePlaceHolder(text, nonStrictHelper);
	}
	
	public String resolveRequiredPlaceHolders(String text) throws IllegalStateException {
		if(strictHelper == null) {
			strictHelper = createPlaceHolderHelper(false);
		}
		return doResolvePlaceHolder(text, strictHelper);
	}
	
	/**
	 * {@inheritDoc} The default is "${".
	 * @see com.tutorial.util.SystemPropertyUtils#PLACEHOLDER_PREFIX
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * {@inheritDoc} The default is "}".
	 * @see com.tutorial.util.SystemPropertyUtils#PLACEHOLDER_SUFFIX
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}
	
	/**
	 * {@inheritDoc} The default is ":".
	 * @see com.tutorial.util.SystemPropertyUtils#VALUE_SEPARATOR
	 */
	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	public void setRequiredProperties(String... requiredProperties) {
		for(String key : requiredProperties) {
			this.requiredProperties.add(key);
		}
	}
	
	public void validateRequiredProperties() throws MissingRequiredPropertiesException {
		MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
		for(String key : this.requiredProperties) {
			if(this.getProperty(key) == null) {
				ex.addMissingRequiredProperty(key);
			}
		}
		if(!ex.getMissingRequiredProperties().isEmpty()) {
			throw ex;
		}
	}
	
	private String doResolvePlaceHolder(String text, PropertyPlaceHolderHelper helper) {
		return helper.replacePlaceHolders(text, new PlaceholderResolver() {
			public String resolvePlaceholder(String placeholderName) {
				return getProperty(placeholderName);
			}
		});
	}

	private PropertyPlaceHolderHelper createPlaceHolderHelper(boolean ignoreUnResolvablePlaceHolders) {
		return new PropertyPlaceHolderHelper(this.placeholderPrefix, this.placeholderSuffix, 
				this.valueSeparator, ignoreUnResolvablePlaceHolders);
	}



}
