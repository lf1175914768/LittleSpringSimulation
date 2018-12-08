package com.tutorial.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class CollectionUtils {

	/**
	 * Merge the given Properties instance into the given Map,
	 * copying all properties (key-value pairs) over.
	 * <p>Uses <code>Properties.propertyNames()</code> to even catch
	 * default properties linked into the original Properties instance.
	 * @param props the Properties instance to merge (may be <code>null</code>)
	 * @param map the target Map to merge the properties into
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mergePropertiesIntoMap(Properties props, Map map) {
		if(map == null) {
			throw new IllegalArgumentException("Map must not be null");
		}
		if(props != null) {
			for(Enumeration<?> en = props.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				Object value = props.getProperty(key);
				if(value == null) {
					// potentially a non-String value....
					value = props.get(key);
				}
				map.put(key, value);
			}
		}
	}

	/**
	 * Return the first element in '<code>candidates</code>' that is contained in
	 * '<code>source</code>'. If no element in '<code>candidates</code>' is present in
	 * '<code>source</code>' returns <code>null</code>. Iteration order is
	 * {@link Collection} implementation specific.
	 * @param source the source Collection
	 * @param candidates the candidates to search for
	 * @return the first present object, or <code>null</code> if not found
	 */
	public static Object findFirstMatch(Collection<?> source, Collection<?> candidates) {
		if(isEmpty(source) || isEmpty(candidates)) {
			return null;
		}
		for(Object candidate : candidates) {
			if(source.contains(candidate)) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Return <code>true</code> if the supplied Collection is <code>null</code>
	 * or empty. Otherwise, return <code>false</code>.
	 * @param collection the Collection to check
	 * @return whether the given Collection is empty
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return (collection == null || collection.isEmpty());
	}

}
