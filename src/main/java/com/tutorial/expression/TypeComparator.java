package com.tutorial.expression;

/**
 * Instances of a type comparator should be able to compare pairs of objects for equality, the specification of the
 * return value is the same as for {@link Comparable}.
 * 
 * @author Andy Clement
 * @since 3.0
 */
public interface TypeComparator {
	
	/**
	 * Compare two objects.
	 * @param firstObject the first object
	 * @param secondObject the second object
	 * @return 0 if they are equal, <0 if the first is smaller than the second, or >0 if the first is larger than the
	 * second
	 * @throws EvaluationException if a problem occurs during comparison (or they are not comparable)
	 */
	int compare(Object firstObject, Object secondObject) throws EvaluationException;
	
	/**
	 * Return true if the comparator can compare these two objects
	 * @param firstObject the first object
	 * @param secondObject the second object
	 * @return true if the comparator can compare these objects
	 */
	boolean canCompare(Object firstObject, Object secondObject);

}
