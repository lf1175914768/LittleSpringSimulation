package com.tutorial.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for working with Strings that have placeholder values in them. A placeholder takes the form
 * <code>${name}</code>. Using <code>PropertyPlaceholderHelper</code> these placeholders can be substituted for
 * user-supplied values. <p> Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceHolderHelper {
	
	private static final Log logger = LogFactory.getLog(PropertyPlaceHolderHelper.class);
	
	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<String, String>(4);
	
	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}
	
	private final String placeholderPrefix;
	private final String placeholderSuffix;
	private final String simplePrefix;
	private final String valueSeparator;
	private final boolean ignoreUnResolvablePlaceHolders;

	/**
	 * Creates a new <code>PropertyPlaceholderHelper</code> that uses the supplied prefix and suffix.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should be ignored
	 * (<code>true</code>) or cause an exception (<code>false</code>).
	 */
	public PropertyPlaceHolderHelper(String placeholderPrefix, String placeholderSuffix, String valueSeparator,
			boolean ignoreUnResolvablePlaceHolders) {
		Assert.notNull(placeholderPrefix, "placeholderPrefix must not be null");
		Assert.notNull(placeholderSuffix, "placeholderSuffix must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if(simplePrefixForSuffix != null && 
				this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			this.simplePrefix = simplePrefixForSuffix;
		} else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnResolvablePlaceHolders = ignoreUnResolvablePlaceHolders;
	}

	/**
	 * Replaces all placeholders of format <code>${name}</code> with the value returned from the supplied
	 * {@link PlaceholderResolver}.
	 * @param value the value containing the placeholders to be replaced.
	 * @param placeholderResolver the <code>PlaceholderResolver</code> to use for replacement.
	 * @return the supplied value with placeholders replaced inline.
	 */
	public String replacePlaceHolders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "Argument 'value' must not be null");
		return parseStringValue(value, placeholderResolver, new HashSet<String>());
	}

	protected String parseStringValue(
			String strVal, PlaceholderResolver placeholderResolver, HashSet<String> visitedPlaceholders) {
		StringBuilder buf = new StringBuilder(strVal);
		
		int startIndex = strVal.indexOf(this.placeholderPrefix);
		while(startIndex != -1) {
			int endIndex = findPlaceholderEndIndex(buf, startIndex);
			if(endIndex != -1) {
				String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				if(!visitedPlaceholders.add(placeholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + placeholder + "' in property definitions");
				}
				//Recursive invocation, parsing placeholders contained in the placeholder key.
				placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
				
				// now obtain the value for the fully resolved key..
				String propVal = placeholderResolver.resolvePlaceholder(placeholder);
				if(propVal == null && this.valueSeparator != null) {
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if(separatorIndex != -1) {
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
						if(propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				if(propVal != null) {
					//Recursive invocation, parsing placeholders contained in the 
					//previously resolved placeholder value.
					propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
					buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					if(logger.isTraceEnabled()) {
						logger.trace("Resolved placeholder '" + placeholder + "'");
					}
					startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else if(this.ignoreUnResolvablePlaceHolders) {
					// Proceed with unprocessed value.
					startIndex = buf.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				} else {
					throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "'");
				}
				visitedPlaceholders.remove(placeholder);
			} else {
				startIndex = -1;
			}
		}
		return buf.toString();
	}

	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
		while(index < buf.length()) {
			if(StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
				if(withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				} else {
					return index;
				}
			} else if(StringUtils.substringMatch(buf, index, this.simplePrefix)) {
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			} else {
				index ++;
			}
		}
		return -1;
	}

	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 * @see PropertyPlaceholderHelper
	 */
	public static interface PlaceholderResolver {
		
		/**
		 * Resolves the supplied placeholder name into the replacement value.
		 * @param placeholderName the name of the placeholder to resolve.
		 * @return the replacement value or <code>null</code> if no replacement is to be made.
		 */
		String resolvePlaceholder(String placeholderName);
	}
	
}
