package com.tutorial.beans;

/**
 * Utility methods for classes that perform bean property access
 * according to the {@link PropertyAccessor} interface.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 */
public abstract class PropertyAccessorUtils {

	/**
	 * Determine whether the given registered path matches the given property path,
	 * either indicating the property itself or an indexed element of the property.
	 * @param propertyPath the property path (typically without index)
	 * @param registeredPath the registered path (potentially with index)
	 * @return whether the paths match
	 */
	public static boolean matchesProperty(String registeredPath, String propertyPath) {
		if(!registeredPath.startsWith(propertyPath)) {
			return false;
		}
		if(registeredPath.length() == propertyPath.length()) {
			return true;
		} 
		if(registeredPath.charAt(propertyPath.length()) != PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
			return false;
		}
		return registeredPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR, propertyPath.length() + 1) ==
				registeredPath.length() - 1;
	}

	/**
	 * Return the actual property name for the given property path.
	 * @param propertyPath the property path to determine the property name
	 * for (can include property keys, for example for specifying a map entry)
	 * @return the actual property name, without any key elements
	 */
	public static String getPropertyName(String propertyPath) {
		int separatorIndex = (propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX) ? 
				propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) : -1);
		return separatorIndex != -1 ? propertyPath.substring(0, separatorIndex) : propertyPath;
	}

	/**
	 * Determine the first nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @return the index of the nested property separator, or -1 if none
	 */
	public static int getFirstNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, false);
	}

	/**
	 * Determine the first nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @return the index of the nested property separator, or -1 if none
	 */
	public static int getLastNestedPropertySeparatorIndex(String nestedPath) {
		return getNestedPropertySeparatorIndex(nestedPath, true);
	}
	
	/**
	 * Determine the first (or last) nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @param last whether to return the last separator rather than the first
	 * @return the index of the nested property separator, or -1 if none
	 */
	private static int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
		boolean inKey = false;
		int length = propertyPath.length();
		int i = (last ? length - 1 : 0);
		while(last ? i >= 0 : i < length) {
			switch(propertyPath.charAt(i)) {
			case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
			case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR: 
				inKey = !inKey;
				break;
			case PropertyAccessor.NESTED_PROPERTY_SEPERATOR_CHAR:
				if(!inKey) return i;
			}
			if(last) {
				i--;
			} else {
				i++;
			}
		}
		return -1;
	}

	/**
	 * Check whether the given property path indicates an indexed or nested property.
	 * @param propertyPath the property path to check
	 * @return whether the path indicates an indexed or nested property
	 */
	public static boolean isNestedOrIndexedProperty(String propertyPath) {
		if(propertyPath == null) {
			return false;
		}
		for(int i = 0; i < propertyPath.length(); i++) {
			char ch = propertyPath.charAt(i);
			if(ch == PropertyAccessor.NESTED_PROPERTY_SEPERATOR_CHAR ||
					ch == PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
				return true;
			}
		}
		return false;
	}
	
}
 