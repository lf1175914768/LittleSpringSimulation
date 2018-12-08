package com.tutorial.expression;

import com.tutorial.core.convert.TypeDescriptor;

/**
 * Encapsulates an object and a type descriptor that describes it.
 * The type descriptor can hold generic information that would not be
 * accessible through a simple <code>getClass()</code> call on the object.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class TypedValue {
	
	public static final TypedValue NULL = new TypedValue(null);
	
	private final Object value;
	
	private TypeDescriptor typeDescriptor;
	
	/**
	 * Create a TypedValue for a simple object. The type descriptor is inferred
	 * from the object, so no generic information is preserved.
	 * @param value the object value
	 */
	public TypedValue(Object value) {
		this.value = value;
		this.typeDescriptor = null;  // initialized when/if requested
	}
	
	/**
	 * Create a TypedValue for a particular value with a particular type descriptor.
	 * @param value the object value
	 * @param typeDescriptor a type descriptor describing the type of the value
	 */
	public TypedValue(Object value, TypeDescriptor typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	public Object getValue() {
		return this.value;
	}
	
	public TypeDescriptor getTypeDescriptor() {
		if(this.typeDescriptor == null) {
			this.typeDescriptor = TypeDescriptor.forObject(this.value);
		}
		return this.typeDescriptor;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("TypedValue: '").append(this.value).append("' of [").append(getTypeDescriptor() + "]");
		return str.toString();
	}
	
}
