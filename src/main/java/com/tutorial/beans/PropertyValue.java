package com.tutorial.beans;

import java.beans.PropertyDescriptor;
import java.io.Serializable;

import com.tutorial.util.Assert;

/**
 * Object to hold information and value for an individual bean property.
 * Using an object here, rather than just storing all properties in
 * a map keyed by property name, allows for more flexibility, and the
 * ability to handle indexed properties etc in an optimized way.
 *
 * <p>Note that the value doesn't need to be the final required type:
 * A {@link BeanWrapper} implementation should handle any necessary conversion,
 * as this object doesn't know anything about the objects it will be applied to.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private Object value;
	private Object source;
	private boolean optional = false;
	private boolean converted = false;
	private Object convertedValue;
	
	/** Package-visible field that indicates whether conversion is necessary */
	volatile Boolean conversionNecessary;

	/** Package-visible field for caching the resolved property path tokens */
	volatile Object resolvedTokens;

	/** Package-visible field for caching the resolved PropertyDescriptor */
	volatile PropertyDescriptor resolvedDescriptor;
	
	/**
	 * Create a new PropertyValue instance.
	 * @param name the name of the property (never <code>null</code>)
	 * @param value the value of the property (possibly before type conversion)
	 */
	public PropertyValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Copy constructor.
	 * @param original the PropertyValue to copy (never <code>null</code>)
	 */
	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.source = original.getSource();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
		copyAttributesFrom(original);
	}
	
	/**
	 * Constructor that exposes a new value for an original value holder.
	 * The original holder will be exposed as source of the new holder.
	 * @param original the PropertyValue to link to (never <code>null</code>)
	 * @param newValue the new value to apply
	 */
	public PropertyValue(PropertyValue original, Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.source = original;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
		copyAttributesFrom(original);
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	public boolean isOptional() {
		return this.optional;
	}
	
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		while(original.source instanceof PropertyValue && original.source != source) {
			original = (PropertyValue) original.source;
		}
		return original;
	}
	
	/**
	 * Return whether this holder contains a converted value already (<code>true</code>),
	 * or whether the value still needs to be converted (<code>false</code>).
	 */
	public synchronized boolean isConverted() {
		return this.converted;
	}

	/**
	 * Set the converted value of the constructor argument,
	 * after processed type conversion.
	 */
	public synchronized void setConvertedValue(Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	/**
	 * Return the converted value of the constructor argument,
	 * after processed type conversion.
	 */
	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other) {
			return true;
		}
		if(!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue another = (PropertyValue) other;
		return this.name.equals(another.getName()) && ((this.value == null && another.value == null) || this.value.equals(another.getValue()));
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + (this.value != null ? this.value.hashCode() : 0);
	}
	
	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}
	
}
