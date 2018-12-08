package com.tutorial.beans;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tutorial.util.Assert;
import com.tutorial.util.ReflectionUtils;
import com.tutorial.util.StringUtils;

/**
 * Decorates a standard {@link BeanInfo} object (likely created created by
 * {@link Introspector#getBeanInfo(Class)}) by including non-void returning setter
 * methods in the collection of {@link #getPropertyDescriptors() property descriptors}.
 * Both regular and
 * <a href="http://download.oracle.com/javase/tutorial/javabeans/properties/indexed.html">
 * indexed properties</a> are fully supported.
 *
 * <p>The wrapped {@code BeanInfo} object is not modified in any way.
 *
 * @author Chris Beams
 * @since 3.1
 * @see CachedIntrospectionResults
 */
public class ExtendedBeanInfo implements BeanInfo {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private final BeanInfo delegate;
	
	private final SortedSet<PropertyDescriptor> propertyDescriptors = 
			new TreeSet<PropertyDescriptor>(new PropertyDescriptorComparator());
	
	/**
	 * Wrap the given delegate {@link BeanInfo} instance and find any non-void returning
	 * setter methods, creating and adding a {@link PropertyDescriptor} for each.
	 *
	 * <p>Note that the wrapped {@code BeanInfo} is modified by this process.
	 *
	 * @see #getPropertyDescriptors()
	 * @throws IntrospectionException if any problems occur creating and adding new {@code PropertyDescriptors}
	 */
	public ExtendedBeanInfo(BeanInfo delegate) throws IntrospectionException {
		this.delegate = delegate;
		
		//PropertyDescriptor instance from the delegate object are never added directly, but always copied 
		// to the local collection of #propertyDescriptors and returned by calls to
		// #getPropertyDescriptors(). this algorithm iterates through all methods (method descriptors)
		// in the wrapped BeanInfo object, copying any existing PropertyDescriptor or creating a new
		// one for any non-standard setter methods found.
		ALL_METHODS:
			for(MethodDescriptor md : delegate.getMethodDescriptors()) {
				Method method = md.getMethod();
				
				// bypass non-getter java.lang.Class methods for efficiency .
				if(ReflectionUtils.isObjectMethod(method) && !method.getName().startsWith("get")) {
					continue ALL_METHODS;
				}
				
				// is the method a NON-INDEXED setter? ignore return type in order to capture non-void signatures
				if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
					String propertyName = propertyNameFor(method);
					if(propertyName.length() == 0) {
						continue ALL_METHODS;
					}
					for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
						Method readMethod = pd.getReadMethod();
						Method writeMethod = pd.getWriteMethod();
						// has the setter already been found by the wrapped BeanInfo?
						if (writeMethod != null
								&& writeMethod.getName().equals(method.getName())) {
							// yes -> copy it, including corresponding getter method (if any -- may be null)
							this.addOrUpdatePropertyDescriptor(pd, propertyName, readMethod, writeMethod);
							continue ALL_METHODS;
						}
						// has a getter corresponding to this setter already been found by the wrapped BeanInfo?
						if (readMethod != null
								&& readMethod.getName().equals(getterMethodNameFor(propertyName))
								&& readMethod.getReturnType().equals(method.getParameterTypes()[0])) {
							this.addOrUpdatePropertyDescriptor(pd, propertyName, readMethod, method);
							continue ALL_METHODS;
						}
					}
					// the setter method was not found by the wrapped BeanInfo -> add a new PropertyDescriptor for it
					// no corresponding getter was detected, so the 'read method' parameter is null.
					this.addOrUpdatePropertyDescriptor(null, propertyName, null, method);
					continue ALL_METHODS;
				}
				
				// is the method an INDEXED setter? ignore return type in order to capture non-void signatures
				if (method.getName().startsWith("set") && method.getParameterTypes().length == 2 && method.getParameterTypes()[0].equals(int.class)) {
					String propertyName = propertyNameFor(method);
					if(propertyName.length() == 0) {
						continue ALL_METHODS;
					}
					DELEGATE_PD:
					for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
						if (!(pd instanceof IndexedPropertyDescriptor)) {
							continue DELEGATE_PD;
						}
						IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
						Method readMethod = ipd.getReadMethod();
						Method writeMethod = ipd.getWriteMethod();
						Method indexedReadMethod = ipd.getIndexedReadMethod();
						Method indexedWriteMethod = ipd.getIndexedWriteMethod();
						// has the setter already been found by the wrapped BeanInfo?
						if (indexedWriteMethod != null
								&& indexedWriteMethod.getName().equals(method.getName())) {
							// yes -> copy it, including corresponding getter method (if any -- may be null)
							this.addOrUpdatePropertyDescriptor(pd, propertyName, readMethod, writeMethod, indexedReadMethod, indexedWriteMethod);
							continue ALL_METHODS;
						}
						// has a getter corresponding to this setter already been found by the wrapped BeanInfo?
						if (indexedReadMethod != null
								&& indexedReadMethod.getName().equals(getterMethodNameFor(propertyName))
								&& indexedReadMethod.getReturnType().equals(method.getParameterTypes()[1])) {
							this.addOrUpdatePropertyDescriptor(pd, propertyName, readMethod, writeMethod, indexedReadMethod, method);
							continue ALL_METHODS;
						}
					}
					// the INDEXED setter method was not found by the wrapped BeanInfo -> add a new PropertyDescriptor
					// for it. no corresponding INDEXED getter was detected, so the 'indexed read method' parameter is null.
					this.addOrUpdatePropertyDescriptor(null, propertyName, null, null, null, method);
					continue ALL_METHODS;
				}
				
				// the method is not a setter, but is it a getter?
				for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
					// have we already copied this read method to a property descriptor locally?
					for (PropertyDescriptor existingPD : this.propertyDescriptors) {
						if (method.equals(pd.getReadMethod())
								&& existingPD.getName().equals(pd.getName())) {
							if (existingPD.getReadMethod() == null) {
								// no -> add it now
								this.addOrUpdatePropertyDescriptor(pd, pd.getName(), method, pd.getWriteMethod());
							}
							// yes -> do not add a duplicate
							continue ALL_METHODS;
						}
					}
					if (method == pd.getReadMethod()
							|| (pd instanceof IndexedPropertyDescriptor && method == ((IndexedPropertyDescriptor) pd).getIndexedReadMethod())) {
						// yes -> copy it, including corresponding setter method (if any -- may be null)
						if (pd instanceof IndexedPropertyDescriptor) {
							this.addOrUpdatePropertyDescriptor(pd, pd.getName(), pd.getReadMethod(), pd.getWriteMethod(), ((IndexedPropertyDescriptor)pd).getIndexedReadMethod(), ((IndexedPropertyDescriptor)pd).getIndexedWriteMethod());
						} else {
							this.addOrUpdatePropertyDescriptor(pd, pd.getName(), pd.getReadMethod(), pd.getWriteMethod());
						}
						continue ALL_METHODS;
					}
				}
			}
	}

	private Object getterMethodNameFor(String propertyName) {
		return "get" + StringUtils.capitalize(propertyName);
	}

	private void addOrUpdatePropertyDescriptor(PropertyDescriptor pd, String propertyName, Method readMethod,
			Method writeMethod) throws IntrospectionException {
		addOrUpdatePropertyDescriptor(pd, propertyName, readMethod, writeMethod, null, null);
	}

	private void addOrUpdatePropertyDescriptor(PropertyDescriptor pd, String propertyName, Method readMethod,
			Method writeMethod, Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {
		Assert.notNull(propertyName, "propertyName may not be null");
		propertyName = (pd == null ? propertyName : pd.getName());
		for (PropertyDescriptor existingPD : this.propertyDescriptors) {  
			if (existingPD.getName().equals(propertyName)) {
				// is there already a descriptor that captures this read method or its corresponding write method?
				if (existingPD.getReadMethod() != null) {
					if (readMethod != null && existingPD.getReadMethod().getReturnType() != readMethod.getReturnType()
							|| writeMethod != null && existingPD.getReadMethod().getReturnType() != writeMethod.getParameterTypes()[0]) {
						// no -> add a new descriptor for it below
						break;
					}
				}
				// update the existing descriptor's read method
				if (readMethod != null) {
					try {
						existingPD.setReadMethod(readMethod);
					} catch (IntrospectionException ex) {
						// there is a conflicting setter method present -> null it out and try again
						existingPD.setWriteMethod(null);
						existingPD.setReadMethod(readMethod);
					}
				}

				// is there already a descriptor that captures this write method or its corresponding read method?
				if (existingPD.getWriteMethod() != null) {
					if (readMethod != null && existingPD.getWriteMethod().getParameterTypes()[0] != readMethod.getReturnType()
							|| writeMethod != null && existingPD.getWriteMethod().getParameterTypes()[0] != writeMethod.getParameterTypes()[0]) {
						// no -> add a new descriptor for it below
						break;
					}
				}
				// update the existing descriptor's write method
				if (writeMethod != null) {
					existingPD.setWriteMethod(writeMethod);
				}

				// is this descriptor indexed?
				if (existingPD instanceof IndexedPropertyDescriptor) {
					IndexedPropertyDescriptor existingIPD = (IndexedPropertyDescriptor) existingPD;

					// is there already a descriptor that captures this indexed read method or its corresponding indexed write method?
					if (existingIPD.getIndexedReadMethod() != null) {
						if (indexedReadMethod != null && existingIPD.getIndexedReadMethod().getReturnType() != indexedReadMethod.getReturnType()
								|| indexedWriteMethod != null && existingIPD.getIndexedReadMethod().getReturnType() != indexedWriteMethod.getParameterTypes()[1]) {
							// no -> add a new descriptor for it below
							break;
						}
					}
					// update the existing descriptor's indexed read method
					try {
						if (indexedReadMethod != null) {
							existingIPD.setIndexedReadMethod(indexedReadMethod);
						}
					} catch (IntrospectionException ex) {
						// there is a conflicting indexed setter method present -> null it out and try again
						existingIPD.setIndexedWriteMethod(null);
						existingIPD.setIndexedReadMethod(indexedReadMethod);
					}

					// is there already a descriptor that captures this indexed write method or its corresponding indexed read method?
					if (existingIPD.getIndexedWriteMethod() != null) {
						if (indexedReadMethod != null && existingIPD.getIndexedWriteMethod().getParameterTypes()[1] != indexedReadMethod.getReturnType()
								|| indexedWriteMethod != null && existingIPD.getIndexedWriteMethod().getParameterTypes()[1] != indexedWriteMethod.getParameterTypes()[1]) {
							// no -> add a new descriptor for it below
							break;
						}
					}
					// update the existing descriptor's indexed write method
					if (indexedWriteMethod != null) {
						existingIPD.setIndexedWriteMethod(indexedWriteMethod);
					}
				}

				// the descriptor has been updated -> return immediately
				return;
			}
		}
		
		// we haven't yet seen read or write methods for this property -> add a new descriptor
		if (pd == null) {
			try {
				if (indexedReadMethod == null && indexedWriteMethod == null) {
					pd = new PropertyDescriptor(propertyName, readMethod, writeMethod);
				}
				else {
					pd = new IndexedPropertyDescriptor(propertyName, readMethod, writeMethod, indexedReadMethod, indexedWriteMethod);
				}
				this.propertyDescriptors.add(pd);
			} catch (IntrospectionException ex) {
				logger.warn(String.format("Could not create new PropertyDescriptor for readMethod [%s] writeMethod [%s] " +
						"indexedReadMethod [%s] indexedWriteMethod [%s] for property [%s]. Reason: %s",
						readMethod, writeMethod, indexedReadMethod, indexedWriteMethod, propertyName, ex.getMessage()));
				// suppress exception and attempt to continue
			}
		}
		else {
			pd.setReadMethod(readMethod);
			try {
				pd.setWriteMethod(writeMethod);
			} catch (IntrospectionException ex) {
				logger.warn(String.format("Could not add write method [%s] for property [%s]. Reason: %s",
						writeMethod, propertyName, ex.getMessage()));
				// fall through -> add property descriptor as best we can
			}
			this.propertyDescriptors.add(pd);
		}
	}

	private String propertyNameFor(Method method) {
		return Introspector.decapitalize(method.getName().substring(3, method.getName().length()));
	}

	public BeanDescriptor getBeanDescriptor() {
		return delegate.getBeanDescriptor();
	}

	public EventSetDescriptor[] getEventSetDescriptors() {
		return delegate.getEventSetDescriptors();
	}

	public int getDefaultEventIndex() {
		return delegate.getDefaultEventIndex();
	}

	/**
	 * Return the set of {@link PropertyDescriptor}s from the wrapped {@link BeanInfo}
	 * object as well as {@code PropertyDescriptor}s for each non-void returning setter
	 * method found during construction.
	 * @see #ExtendedBeanInfo(BeanInfo)
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.propertyDescriptors.toArray(new PropertyDescriptor[this.propertyDescriptors.size()]);
	}

	public int getDefaultPropertyIndex() {
		return delegate.getDefaultPropertyIndex();
	}

	public MethodDescriptor[] getMethodDescriptors() {
		return delegate.getMethodDescriptors();
	}

	public BeanInfo[] getAdditionalBeanInfo() {
		return delegate.getAdditionalBeanInfo();
	}

	public Image getIcon(int iconKind) {
		return delegate.getIcon(iconKind);
	}

	/**
	 * Sorts PropertyDescriptor instances alphanumerically to emulate the behavior of {@link java.beans.BeanInfo#getPropertyDescriptors()}.
	 *
	 * @see ExtendedBeanInfo#propertyDescriptors
	 */
	static final class PropertyDescriptorComparator implements Comparator<PropertyDescriptor> {
		public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
			String left = o1.getName();
			String right = o2.getName();
			for(int i = 0; i < left.length(); i++) {
				if(right.length() == i) {
					return 1;
				}
				int result = left.getBytes()[i] - right.getBytes()[i];
				if(result != 0) {
					return result;
				}
			}
			return left.length() - right.length();
		}
	}
	
}