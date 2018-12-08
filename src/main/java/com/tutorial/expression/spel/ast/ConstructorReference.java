package com.tutorial.expression.spel.ast;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.AccessException;
import com.tutorial.expression.ConstructorExecutor;
import com.tutorial.expression.ConstructorResolver;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypeConverter;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.common.ExpressionUtils;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.SpelNode;

/**
 * @author Liufeng
 * Created on 2018年11月17日 上午9:33:46
 */
public class ConstructorReference extends SpelNodeImpl {
	
	private boolean isArrayConstructor = false;
	
	private SpelNodeImpl[] dimensions;
	
	private volatile ConstructorExecutor cachedExecutor;

	/**
	 * Create a constructor reference. The first argument is the type, the rest are the parameters to the constructor
	 * call
	 */
	public ConstructorReference(int pos, SpelNodeImpl... operands) {
		super(pos, operands);
		this.isArrayConstructor = false;
	}
	
	public ConstructorReference(int pos, SpelNodeImpl[] dimensions, SpelNodeImpl... arguments) {
		super(pos, arguments);
		this.isArrayConstructor = true;
		this.dimensions = dimensions;
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		if(this.isArrayConstructor) {
			return createArray(expressionState);
		} else {
			return createNewInstance(expressionState);
		}
	}

	/**
	 * Create a new ordinary object and return it.
	 * @param state the expression state within which this expression is being evaluated
	 * @return the new object
	 * @throws EvaluationException if there is a problem creating the object
	 */
	private TypedValue createNewInstance(ExpressionState expressionState) {
		Object[] arguments = new Object[getChildCount() - 1];
		List<TypeDescriptor> argumentTypes = new ArrayList<TypeDescriptor>(getChildCount() - 1);
		for(int i = 0; i < arguments.length; i++) {
			TypedValue childValue = this.children[i + 1].getValueInternal(expressionState);
			Object value = childValue.getValue();
			arguments[i] = value;
			argumentTypes.add(TypeDescriptor.forObject(value));
		}
		
		ConstructorExecutor executorToUse = this.cachedExecutor;
		if(executorToUse != null) {
			try {
				return executorToUse.execute(expressionState.getEvaluationContext(), arguments);
			} catch (AccessException e) {
				if(e.getCause() instanceof InvocationTargetException) {
					Throwable rootCause = e.getCause().getCause();
					if(rootCause instanceof RuntimeException) {
						throw (RuntimeException) rootCause;
					} else {
						String typeName = (String) this.children[0].getValueInternal(expressionState).getValue();
						throw new SpelEvaluationException(getStartPosition(), rootCause,
								SpelMessage.CONSTRUCTOR_INVOCATION_PROBLEM, typeName, FormatHelper
								.formatMethodForMessage("", argumentTypes));
					} 
				}
				// at this point we know it wasn't a user problem so worth a retry if a better candidate can be found
				this.cachedExecutor = null;
			}
		}
		
		String typeName = (String) this.children[0].getValueInternal(expressionState).getValue();
		executorToUse = findExecutorForConstructor(typeName, argumentTypes, expressionState);
		try {
			this.cachedExecutor = executorToUse;
			return executorToUse.execute(expressionState.getEvaluationContext(), arguments);
		} catch (AccessException ae) {
			throw new SpelEvaluationException(getStartPosition(), ae, SpelMessage.CONSTRUCTOR_INVOCATION_PROBLEM,
					typeName, FormatHelper.formatMethodForMessage("", argumentTypes));
		}
	}

	/**
	 * Go through the list of registered constructor resolvers and see if any can find a constructor that takes the
	 * specified set of arguments.
	 * @param typename the type trying to be constructed
	 * @param argumentTypes the types of the arguments supplied that the constructor must take
	 * @param state the current state of the expression
	 * @return a reusable ConstructorExecutor that can be invoked to run the constructor or null
	 * @throws SpelEvaluationException if there is a problem locating the constructor
	 */
	private ConstructorExecutor findExecutorForConstructor(String typeName, List<TypeDescriptor> argumentTypes,
			ExpressionState state) {
		EvaluationContext eContext = state.getEvaluationContext();
		List<ConstructorResolver> cResolvers = eContext.getConstructorResolvers();
		if(cResolvers != null) {
			for(ConstructorResolver resolver : cResolvers) {
				try {
					ConstructorExecutor cEx = resolver.resolve(eContext, typeName, argumentTypes);
					if(cEx != null) {
						return cEx;
					} 
				} catch (java.rmi.AccessException ex) {
					throw new SpelEvaluationException(getStartPosition(), ex,
							SpelMessage.CONSTRUCTOR_INVOCATION_PROBLEM, typeName,
							FormatHelper.formatMethodForMessage("", argumentTypes));
				}
			}
		}
		throw new SpelEvaluationException(getStartPosition(), SpelMessage.CONSTRUCTOR_NOT_FOUND, typeName, FormatHelper
				.formatMethodForMessage("", argumentTypes));
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		sb.append("new ");

		int index = 0;
		sb.append(getChild(index++).toStringAST());
		sb.append("(");
		for (int i = index; i < getChildCount(); i++) {
			if (i > index)
				sb.append(",");
			sb.append(getChild(i).toStringAST());
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Create an array and return it.
	 * @param state the expression state within which this expression is being evaluated
	 * @return the new array
	 * @throws EvaluationException if there is a problem creating the array
	 */
	private TypedValue createArray(ExpressionState expressionState) {
		Object intendedArrayType = getChild(0).getValue(expressionState);
		if(!(intendedArrayType instanceof String)) {
			throw new SpelEvaluationException(getChild(0).getStartPosition(),
					SpelMessage.TYPE_NAME_EXPECTED_FOR_ARRAY_CONSTRUCTION, FormatHelper
					.formatClassNameForMessage(intendedArrayType.getClass()));
		} 
		String type = (String) intendedArrayType;
		Class<?> componentType;
		TypeCode arrayTypeCode = TypeCode.forName(type);
		if(arrayTypeCode == TypeCode.OBJECT) {
			componentType = expressionState.findType(type);
		} else {
			componentType = arrayTypeCode.getType();
		}
		Object newArray;
		if(!hasInitializer()) {
			for(SpelNodeImpl dimension : this.dimensions) {
				if(dimension == null) {
					throw new SpelEvaluationException(getStartPosition(), SpelMessage.MISSING_ARRAY_DIMENSION);
				}
			} 
			TypeConverter typeConverter = expressionState.getEvaluationContext().getTypeConverter();
			//  Shortcut for 1 dimension
			if(this.dimensions.length == 1) {
				TypedValue o = this.dimensions[0].getTypedValue(expressionState);
				int arraySize = ExpressionUtils.toInt(typeConverter, o);
				newArray = Array.newInstance(componentType, arraySize);
			} else {
				int[] dims = new int[this.dimensions.length];
				for(int d = 0; d < this.dimensions.length; d++) {
					TypedValue o = this.dimensions[d].getTypedValue(expressionState);
					dims[d] = ExpressionUtils.toInt(typeConverter, o);
				}
				newArray = Array.newInstance(componentType, dims);
			}
		} else {
			// There is an initializer
			if (this.dimensions.length > 1) {
				// There is an initializer but this is a multi-dimensional array (e.g. new int[][]{{1,2},{3,4}}) - this
				// is not currently supported
				throw new SpelEvaluationException(getStartPosition(),
						SpelMessage.MULTIDIM_ARRAY_INITIALIZER_NOT_SUPPORTED);
			}
			TypeConverter typeConverter = expressionState.getEvaluationContext().getTypeConverter();
			InlineList initializer = (InlineList) getChild(1);
			// If a dimension was specified, check it matches the initializer length
			if (this.dimensions[0] != null) {
				TypedValue dValue = this.dimensions[0].getTypedValue(expressionState);
				int i = ExpressionUtils.toInt(typeConverter, dValue);
				if (i != initializer.getChildCount()) {
					throw new SpelEvaluationException(getStartPosition(), SpelMessage.INITIALIZER_LENGTH_INCORRECT);
				}
			}
			// Build the array and populate it
			int arraySize = initializer.getChildCount();
			newArray = Array.newInstance(componentType, arraySize);
			if (arrayTypeCode == TypeCode.OBJECT) {
				populateReferenceTypeArray(expressionState, newArray, typeConverter, initializer, componentType);
			}
			else if (arrayTypeCode == TypeCode.INT) {
				populateIntArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.BOOLEAN) {
				populateBooleanArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.CHAR) {
				populateCharArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.LONG) {
				populateLongArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.SHORT) {
				populateShortArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.DOUBLE) {
				populateDoubleArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.FLOAT) {
				populateFloatArray(expressionState, newArray, typeConverter, initializer);
			}
			else if (arrayTypeCode == TypeCode.BYTE) {
				populateByteArray(expressionState, newArray, typeConverter, initializer);
			}
			else {
				throw new IllegalStateException(arrayTypeCode.name());
			}
		}
		return new TypedValue(newArray);
	}
	
	private void populateReferenceTypeArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer, Class<?> componentType) {
		TypeDescriptor toTypeDescriptor = TypeDescriptor.valueOf(componentType);
		Object[] newObjectArray = (Object[]) newArray;
		for (int i = 0; i < newObjectArray.length; i++) {
			SpelNode elementNode = initializer.getChild(i);
			Object arrayEntry = elementNode.getValue(state);
			newObjectArray[i] = typeConverter.convertValue(arrayEntry, TypeDescriptor.forObject(arrayEntry), toTypeDescriptor);
		}
	}

	private void populateByteArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		byte[] newByteArray = (byte[]) newArray;
		for (int i = 0; i < newByteArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newByteArray[i] = ExpressionUtils.toByte(typeConverter, typedValue);
		}
	}

	private void populateFloatArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		float[] newFloatArray = (float[]) newArray;
		for (int i = 0; i < newFloatArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newFloatArray[i] = ExpressionUtils.toFloat(typeConverter, typedValue);
		}
	}

	private void populateDoubleArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		double[] newDoubleArray = (double[]) newArray;
		for (int i = 0; i < newDoubleArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newDoubleArray[i] = ExpressionUtils.toDouble(typeConverter, typedValue);
		}
	}

	private void populateShortArray(ExpressionState state, Object newArray,
			TypeConverter typeConverter, InlineList initializer) {
		short[] newShortArray = (short[]) newArray;
		for (int i = 0; i < newShortArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newShortArray[i] = ExpressionUtils.toShort(typeConverter, typedValue);
		}
	}

	private void populateLongArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		long[] newLongArray = (long[]) newArray;
		for (int i = 0; i < newLongArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newLongArray[i] = ExpressionUtils.toLong(typeConverter, typedValue);
		}
	}

	private void populateCharArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		char[] newCharArray = (char[]) newArray;
		for (int i = 0; i < newCharArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newCharArray[i] = ExpressionUtils.toChar(typeConverter, typedValue);
		}
	}

	private void populateBooleanArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		boolean[] newBooleanArray = (boolean[]) newArray;
		for (int i = 0; i < newBooleanArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newBooleanArray[i] = ExpressionUtils.toBoolean(typeConverter, typedValue);
		}
	}

	private void populateIntArray(ExpressionState state, Object newArray, TypeConverter typeConverter,
			InlineList initializer) {
		int[] newIntArray = (int[]) newArray;
		for (int i = 0; i < newIntArray.length; i++) {
			TypedValue typedValue = initializer.getChild(i).getTypedValue(state);
			newIntArray[i] = ExpressionUtils.toInt(typeConverter, typedValue);
		}
	}

	private boolean hasInitializer() {
		return getChildCount() > 1;
	}

}
