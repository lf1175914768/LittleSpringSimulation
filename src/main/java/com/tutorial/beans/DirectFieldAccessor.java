package com.tutorial.beans;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.tutorial.core.MethodParameter;
import com.tutorial.core.convert.ConversionException;
import com.tutorial.core.convert.ConverterNotFoundException;
import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.util.Assert;
import com.tutorial.util.ReflectionUtils;

/**
 * {@link PropertyAccessor} implementation that directly accesses instance fields.
 * Allows for direct binding to fields instead of going through JavaBean setters.
 *
 * <p>This implementation just supports fields in the actual target object.
 * It is not able to traverse nested fields.
 *
 * <p>A DirectFieldAccessor's default for the "extractOldValueForEditor" setting
 * is "true", since a field can always be read without side effects.
 * 
 * @author Liufeng
 * Created on 2018年12月9日 下午5:24:44
 */
public class DirectFieldAccessor extends AbstractPropertyAccessor {
	
	private final Object target;
	
	private final Map<String, Field> fieldMap = new HashMap<String, Field>();
	
	private final TypeConverterDelegate typeConverterDelegate;
	
	/**
	 * Create a new DirectFieldAccessor for the given target object.
	 * @param target the target object to access
	 */
	public DirectFieldAccessor(final Object target) {
		Assert.notNull(target, "Target object must not be null");
		this.target = target;
		ReflectionUtils.doWithFields(this.target.getClass(), new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if(fieldMap.containsKey(field.getName())) {
					// ignore superclass declarations of fields already found in a subclass
				} else {
					fieldMap.put(field.getName(), field);
				}
			}
		});
		this.typeConverterDelegate = new TypeConverterDelegate(this, target);
		registerDefaultEditors();
		setExtractOldValueForEditor(true);
	}

	@Override
	public boolean isReadableProperty(String propertyName) {
		return this.fieldMap.containsKey(propertyName);
	}

	@Override
	public boolean isWritableProperty(String propertyName) {
		return this.fieldMap.containsKey(propertyName);
	}

	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
		Field field = this.fieldMap.get(propertyName);
		if(field != null) {
			return new TypeDescriptor(field);
		}
		return null;
	}
	
	@Override
	public Class<?> getPropertyType(String propertyName) {
		Field field = this.fieldMap.get(propertyName);
		if(field != null) {
			return field.getClass();
		}
		return null;
	}

	@Override
	public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParameter)
			throws TypeMisMatchException {
		try {
			return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParameter);
		} catch (IllegalArgumentException ex) {
			throw new TypeMisMatchException(value, requiredType, ex);
		} catch (IllegalStateException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		}
	}

	@Override
	public Object getPropertyValue(String propertyName) throws BeansException {
		Field field = this.fieldMap.get(propertyName);
		if(field == null) {
			throw new NotReadablePropertyException(
					this.target.getClass(), propertyName, "Field '" + propertyName + "' does not exist");
		}
		try {
			ReflectionUtils.makeAccessible(field);
			return field.get(target);
		} catch (IllegalAccessException ex) {
			throw new InvalidPropertyException(this.target.getClass(), propertyName, "Field is not accessible", ex);
		}
	}

	@Override
	public void setPropertyValue(String propertyName, Object newValue) throws BeansException {
		Field field = this.fieldMap.get(propertyName);
		if(field == null) {
			throw new NotReadablePropertyException(
					this.target.getClass(), propertyName, "Field '" + propertyName + "' does not exist");
		} 
		Object oldValue = null;
		try {
			ReflectionUtils.makeAccessible(field);
			oldValue = field.get(target);
			Object convertedValue = this.typeConverterDelegate.convertIfNecessary(
					field.getName(), oldValue, newValue, field.getType(), new TypeDescriptor(field));
			field.set(this.target, convertedValue);
		} catch (ConverterNotFoundException e) {
			PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, field.getType(), e);
		} catch (ConversionException e)  {
			PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
			throw new TypeMisMatchException(pce, field.getType(), e);
		} catch (IllegalStateException e) {
			PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, field.getType(), e);
		} catch (IllegalArgumentException e) {
			PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
			throw new TypeMisMatchException(pce, field.getType(), e);
		} catch (IllegalAccessException e) {
			throw new InvalidPropertyException(this.target.getClass(), propertyName, "Field is not accessible", e);
		}
	}

}
