package com.tutorial.beans;

import com.tutorial.util.Assert;
import com.tutorial.util.ObjectUtils;

/**
 * Holder for a key-value style attribute that is part of a bean definition.
 * Keeps track of the definition source in addition to the key-value pair.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class BeanMetadataAttribute implements BeanMetadataElement {

	private final String name;

	private final Object value;
	
	private Object source;
	
	/**
	 * Create a new AttributeValue instance.
	 * @param name the name of the attribute (never <code>null</code>)
	 * @param value the value of the attribute (possibly before type conversion)
	 */
	public BeanMetadataAttribute(String name, Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(Object source) {
		this.source = source;
	}
	
	public Object getSource() {
		return this.source;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanMetadataAttribute)) {
			return false;
		}
		BeanMetadataAttribute otherMa = (BeanMetadataAttribute) other;
		return (this.name.equals(otherMa.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherMa.getValue()) &&
				ObjectUtils.nullSafeEquals(this.source, otherMa.getSource()));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}

	@Override
	public String toString() {
		return "metadata attribute '" + this.name + "'";
	}
	
}
