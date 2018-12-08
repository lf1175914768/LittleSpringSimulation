package com.tutorial.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class CollectionFactory {
	
	private static Class<?> navigableSetClass = null;
	
	private static Class<?> navigableMapClass = null;
	
	private static final Set<Class> approximableCollectionTypes = new HashSet<Class>(10);
	
	private static final Set<Class> approximableMapTypes = new HashSet<Class>(6);
	
	static {
		//Standard collection interface.
		approximableCollectionTypes.add(Collection.class);
		approximableCollectionTypes.add(List.class);
		approximableCollectionTypes.add(Set.class);
		approximableCollectionTypes.add(SortedSet.class);
		approximableMapTypes.add(Map.class);
		approximableMapTypes.add(SortedMap.class);
		
		ClassLoader cl = CollectionFactory.class.getClassLoader();
		try {
			navigableSetClass = cl.loadClass("java.util.NavigableSet");
			navigableMapClass = cl.loadClass("java.util.NavigableMap");
			approximableCollectionTypes.add(navigableSetClass);
			approximableMapTypes.add(navigableMapClass);
		} catch (ClassNotFoundException e) {
			// Not running on java6 or above...
		}
		
		// Common concrete collection classes.
		approximableCollectionTypes.add(ArrayList.class);
		approximableCollectionTypes.add(LinkedList.class);
		approximableCollectionTypes.add(HashSet.class);
		approximableCollectionTypes.add(LinkedHashSet.class);
		approximableCollectionTypes.add(TreeSet.class);
		approximableMapTypes.add(HashMap.class);
		approximableMapTypes.add(LinkedHashMap.class);
		approximableMapTypes.add(TreeMap.class);
	}

	/**
	 * Create the most appropriate collection for the given collection type.
	 * <p>Creates an ArrayList, TreeSet or linked Set for a List, SortedSet
	 * or Set, respectively.
	 * @param collectionType the desired type of the target Collection
	 * @param initialCapacity the initial capacity
	 * @return the new Collection instance
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection<Object> createCollection(Class<?> collectionType, int initialCapacity) {
		if(collectionType.isInterface()) {
			if(List.class.equals(collectionType)) {
				return new ArrayList(initialCapacity);
			}
			else if(SortedSet.class.equals(collectionType) || collectionType.equals(navigableSetClass)) {
				return new TreeSet();
			}
			else if(Set.class.equals(collectionType) || Collection.class.equals(collectionType)) {
				return new LinkedHashSet(initialCapacity);
			}
			else {
				throw new IllegalArgumentException("Unsupported Collection interface: " + collectionType.getName());
			}
		} else {
			if (!Collection.class.isAssignableFrom(collectionType)) {
				throw new IllegalArgumentException("Unsupported Collection type: " + collectionType.getName());
			}
			try {
				return (Collection) collectionType.newInstance();
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("Could not instantiate Collection type: " + collectionType.getName());
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<Object, Object> createMap(Class<?> mapType, int initialCapacity) {
		if (mapType.isInterface()) {
			if (Map.class.equals(mapType)) {
				return new LinkedHashMap(initialCapacity);
			}
			else if (SortedMap.class.equals(mapType) || mapType.equals(navigableMapClass)) {
				return new TreeMap();
			}
			else {
				throw new IllegalArgumentException("Unsupported Map interface: " + mapType.getName());
			}
		}
		else {
			if (!Map.class.isAssignableFrom(mapType)) {
				throw new IllegalArgumentException("Unsupported Map type: " + mapType.getName());
			}
			try {
				return (Map) mapType.newInstance();
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("Could not instantiate Map type: " + mapType.getName());
			}
		}
	}

	/**
	 * Determine whether the given map type is an approximable type,
	 * i.e. a type that {@link #createApproximateMap} can approximate.
	 * @param mapType the map type to check
	 * @return <code>true</code> if the type is approximable,
	 * <code>false</code> if it is not
	 */
	public static boolean isApproximableMapType(Class<?> mapType) {
		return (mapType != null && approximableMapTypes.contains(mapType));
	}

	/**
	 * Create the most approximate map for the given map.
	 * <p>Creates a TreeMap or linked Map for a SortedMap or Map, respectively.
	 * @param map the original Map object
	 * @param initialCapacity the initial capacity
	 * @return the new Map instance
	 * @see java.util.TreeMap
	 * @see java.util.LinkedHashMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map createApproximateMap(Map<?, ?> map, int initialCapacity) {
		if(map instanceof SortedMap) {
			return new TreeMap(((SortedMap) map).comparator());
		} else {
			return new LinkedHashMap(initialCapacity);
		}
	}

	/**
	 * Determine whether the given collection type is an approximable type,
	 * i.e. a type that {@link #createApproximateCollection} can approximate.
	 * @param collectionType the collection type to check
	 * @return <code>true</code> if the type is approximable,
	 * <code>false</code> if it is not
	 */
	public static boolean isApproximableCollectionType(Class<?> collectionType) {
		return collectionType != null && approximableCollectionTypes.contains(collectionType);
	}

	/**
	 * Create the most approximate collection for the given collection.
	 * <p>Creates an ArrayList, TreeSet or linked Set for a List, SortedSet
	 * or Set, respectively.
	 * @param collection the original Collection object
	 * @param initialCapacity the initial capacity
	 * @return the new Collection instance
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see java.util.LinkedHashSet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection<?> createApproximateCollection(Collection<?> collection, int initialCapacity) {
		if(collection instanceof LinkedList) {
			return new LinkedList();
		} else if(collection instanceof List) {
			return new ArrayList(initialCapacity);
		} else if(collection instanceof SortedSet) { 
			return new TreeSet(((SortedSet) collection).comparator());
		} else {
			return new LinkedHashSet(initialCapacity);
		}
	}

}
