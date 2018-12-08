package com.tutorial.expression.spel.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.PropertyAccessor;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.ReflectivePropertyAccessor;

/**
 * Represents a simple property or field reference.
 * 
 * @author Andy Clement
 * @author Juergen Hoeller
 * @author Clark Duplichien 
 * @since 3.0
 */
public class PropertyOrFieldReference extends SpelNodeImpl {
	
	private final boolean nullSafe; 
	
	private final String name;
	
	private volatile PropertyAccessor cachedReadAccessor;
	
	private volatile PropertyAccessor cachedWriteAccessor;
	
	public PropertyOrFieldReference(boolean nullSafe, String propertyOrFieldName, int pos) {
		super(pos);
		this.nullSafe = nullSafe;
		this.name = propertyOrFieldName;
	}

	public boolean isNullSafe() {
		return this.nullSafe;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state)
			throws EvaluationException {
		TypedValue result = readProperty(state, this.name);
		
		// Dynamically create the objects if the user has requested that optional behavior
		if(result.getValue() == null && state.getConfiguration().isAutoGrowNullReferences() &&
				nextChildIs(Indexer.class, PropertyOrFieldReference.class)) {
			TypeDescriptor resultDescriptor = result.getTypeDescriptor();
			// Creating lists and maps
			if((resultDescriptor.getType().equals(List.class) || 
					resultDescriptor.getType().equals(Map.class))) {
				// Create a new collection or map ready for the indexer.
				if(resultDescriptor.getType().equals(List.class)) {
					try {
						if(isWritable(state)) {
							List<?> newList = ArrayList.class.newInstance();
							writeProperty(state, this.name, newList);
							result = readProperty(state, this.name);
						}
					} catch (InstantiationException ex) {
						throw new SpelEvaluationException(getStartPosition(), ex,
								SpelMessage.UNABLE_TO_CREATE_LIST_FOR_INDEXING);
					} catch (IllegalAccessException ex) {
						throw new SpelEvaluationException(getStartPosition(), ex,
								SpelMessage.UNABLE_TO_CREATE_LIST_FOR_INDEXING);
					}
				} else {
					try { 
						if (isWritable(state)) {
							Map<?, ?> newMap = HashMap.class.newInstance();
							writeProperty(state, name, newMap);
							result = readProperty(state, this.name);
						}
					}
					catch (InstantiationException ex) {
						throw new SpelEvaluationException(getStartPosition(), ex,
								SpelMessage.UNABLE_TO_CREATE_MAP_FOR_INDEXING);
					}
					catch (IllegalAccessException ex) {
						throw new SpelEvaluationException(getStartPosition(), ex,
								SpelMessage.UNABLE_TO_CREATE_MAP_FOR_INDEXING);
					}
				}
			} else {
				// 'simple' object
				try { 
					if (isWritable(state)) {
						Object newObject  = result.getTypeDescriptor().getType().newInstance();
						writeProperty(state, name, newObject);
						result = readProperty(state, this.name);
					}
				}
				catch (InstantiationException ex) {
					throw new SpelEvaluationException(getStartPosition(), ex,
							SpelMessage.UNABLE_TO_DYNAMICALLY_CREATE_OBJECT, result.getTypeDescriptor().getType());
				}
				catch (IllegalAccessException ex) {
					throw new SpelEvaluationException(getStartPosition(), ex,
							SpelMessage.UNABLE_TO_DYNAMICALLY_CREATE_OBJECT, result.getTypeDescriptor().getType());
				}	
			}
		}
		return result;
	}

	@Override
	public void setValue(ExpressionState state, Object newValue) throws EvaluationException {
		writeProperty(state, this.name, newValue);
	}
	
	@Override
	public String toStringAST() {
		return this.name;
	}

	@Override
	public boolean isWritable(ExpressionState state) 
			throws EvaluationException {
		return isWritableProperty(this.name, state);
	}
	
	/**
	 * Attempt to read the named property from the current context object.
	 * @param state the evaluation state
	 * @param name the name of the property
	 * @return the value of the property
	 * @throws SpelEvaluationException if any problem accessing the property or it cannot be found
	 */
	private TypedValue readProperty(ExpressionState state, String name) throws EvaluationException {
		TypedValue contextObject = state.getActiveContextObject();
		Object targetObject = contextObject.getValue();
		
		if(targetObject == null && this.nullSafe) {
			return TypedValue.NULL;
		} 
		
		PropertyAccessor accessorToUse = this.cachedReadAccessor;
		if(accessorToUse != null) {
			try {
				return accessorToUse.read(state.getEvaluationContext(), targetObject, name);
			} catch (AccessException e) {
				// this is OK - it may have gone stale due to a class change.
				// let's try to get a new one and call it before giving up.
				this.cachedReadAccessor = null;
			}
		}
		
		Class<?> contextObjectClass = getObjectClass(targetObject);
		List<PropertyAccessor> accessorsToTry = getPropertyAccessorsToTry(contextObjectClass, state);
		EvaluationContext eContext = state.getEvaluationContext();
		
		// Go through the accessors that may be able to resolve it, If they are a cacheable accessor then 
		// get the accessor and use it. If they are not cacheable but report they can read the property
		// then ask them to read it
		if(accessorsToTry != null) {
			try {
				for(PropertyAccessor accessor : accessorsToTry) {
					if(accessor.canRead(eContext, targetObject, name)) {
						if(accessor instanceof ReflectivePropertyAccessor) {
							accessor = ((ReflectivePropertyAccessor) accessor).createOptionalAccessor(
									eContext, targetObject, name);
						}
						this.cachedReadAccessor = accessor;
						return accessor.read(eContext, targetObject, name);
					}
				}
			} catch (AccessException ae) {
				throw new SpelEvaluationException(ae, 
						SpelMessage.EXCEPTION_DURING_PROPERTY_READ, name, ae.getMessage());
			}
		}
		if (contextObject.getValue() == null) {
			throw new SpelEvaluationException(
					SpelMessage.PROPERTY_OR_FIELD_NOT_READABLE_ON_NULL, name);
		}
		else {
			throw new SpelEvaluationException(getStartPosition(),
					SpelMessage.PROPERTY_OR_FIELD_NOT_READABLE, name,
					FormatHelper.formatClassNameForMessage(contextObjectClass));
		}
	}

	private void writeProperty(ExpressionState state, String name, Object newValue)
			throws SpelEvaluationException {
		TypedValue contextObject = state.getActiveContextObject();
		EvaluationContext eContext = state.getEvaluationContext();
		
		if(contextObject.getValue() == null && nullSafe) {
			return;
		}
		PropertyAccessor accessorToUse = this.cachedWriteAccessor;
		if(accessorToUse != null) {
			try {
				accessorToUse.write(state.getEvaluationContext(), 
						contextObject.getValue(), name, newValue);
				return;
			} catch (AccessException e) {
				// this is OK - it may have gone stale due to a class change,
				// let's try to get a new one and call it before giving up
				this.cachedWriteAccessor = null;
			}
		}
		
		Class<?> contextObjectClass = getObjectClass(contextObject.getValue());
		
		List<PropertyAccessor> accessorsToTry = getPropertyAccessorsToTry(contextObjectClass, state);
		if(accessorsToTry != null) {
			try {
				for(PropertyAccessor accessor : accessorsToTry) {
					if(accessor.canWrite(eContext, contextObject.getValue(), name)) {
						this.cachedWriteAccessor = accessor;
						accessor.write(eContext, contextObject.getValue(), name, newValue);
						return;
					}
				}
			} catch (AccessException e) {
				throw new SpelEvaluationException(getStartPosition(), e, 
						SpelMessage.EXCEPTION_DURING_PROPERTY_WRITE,
						name, e.getMessage());
			}
		}
		if(contextObject.getValue() == null) {
			throw new SpelEvaluationException(getStartPosition(), 
					SpelMessage.PROPERTY_OR_FIELD_NOT_WRITABLE_ON_NULL, name);
		} else {
			throw new SpelEvaluationException(getStartPosition(), 
					SpelMessage.PROPERTY_OR_FIELD_NOT_WRITABLE, name,
					FormatHelper.formatClassNameForMessage(contextObjectClass));
		}
	}
	
	public boolean isWritableProperty(String name, ExpressionState state)
		 	throws SpelEvaluationException {
		Object contextObject = state.getActiveContextObject().getValue();
		EvaluationContext eContext = state.getEvaluationContext();
		List<PropertyAccessor> resolversToTry = getPropertyAccessorsToTry(getObjectClass(contextObject), state);
		if(resolversToTry != null) {
			for(PropertyAccessor pfResolver : resolversToTry) {
				try {
					if(pfResolver.canWrite(eContext, contextObject, name)) {
						return true;
					}
				} catch (AccessException e) {
					// Let other try
				}
			}
		}
		return false;
	}

	/**
	 * Determines the set of property resolvers that should be used to try and access a property on the specified target
	 * type. The resolvers are considered to be in an ordered list, however in the returned list any that are exact
	 * matches for the input target type (as opposed to 'general' resolvers that could work for any type) are placed at
	 * the start of the list. In addition, there are specific resolvers that exactly name the class in question and
	 * resolvers that name a specific class but it is a supertype of the class we have. These are put at the end of the
	 * specific resolvers set and will be tried after exactly matching accessors but before generic accessors.
	 * @param targetType the type upon which property access is being attempted
	 * @return a list of resolvers that should be tried in order to access the property
	 */
	private List<PropertyAccessor> getPropertyAccessorsToTry(
			Class<?> targetType, ExpressionState state) {
		List<PropertyAccessor> specificAccessors = new ArrayList<PropertyAccessor>();
		List<PropertyAccessor> generalAccessors = new ArrayList<PropertyAccessor>();
		for(PropertyAccessor resolver : state.getPropertyAccessors()) {
			Class<?>[] targets = resolver.getSpecificTargetClasses();
			if(targets == null) {
				generalAccessors.add(resolver);
			} else {
				if(targetType != null) {
					for(Class<?> clazz : targets) {
						if(clazz == targetType) {
							specificAccessors.add(resolver);
							break;
						} else if(clazz.isAssignableFrom(targetType)) {
							generalAccessors.add(resolver);
						}
					}
				}
			}
		}
		List<PropertyAccessor> resolvers = new ArrayList<PropertyAccessor>();
		resolvers.addAll(specificAccessors);
		generalAccessors.removeAll(specificAccessors);
		resolvers.addAll(generalAccessors);
		return resolvers;
	}

}
