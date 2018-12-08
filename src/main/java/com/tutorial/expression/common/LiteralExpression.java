package com.tutorial.expression.common;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Expression;

/**
 * A very simple hardcoded implementation of the Expression interface that represents a string literal.
 * It is used with CompositeStringExpression when representing a template expression which is made up
 * of pieces - some being real expressions to be handled by an EL implementation like Spel, and some
 * being just textual elements.
 *
 * @author Andy Clement
 * @since 3.0
 */
public class LiteralExpression implements Expression {
	
	private final String literalValue;
	
	public LiteralExpression(String literalValue) {
		this.literalValue = literalValue;
	}

	public Object getValue() throws EvaluationException {
		return this.literalValue;
	}

	public Object getValue(Object rootObject) throws EvaluationException {
		return this.literalValue;
	}

	public <T> T getValue(Class<T> desiredResultType) throws EvaluationException {
		Object value = getValue();
		return ExpressionUtils.convert(null, value, desiredResultType);
	}

	public <T> T getValue(Object rootObject, Class<T> desiredResultType) throws EvaluationException {
		Object value = getValue(rootObject);
		return ExpressionUtils.convert(null, value, desiredResultType);
	}

	public Object getValue(EvaluationContext context) throws EvaluationException {
		return this.literalValue;
	}

	public Object getValue(EvaluationContext context, Object rootObject) throws EvaluationException {
		return this.literalValue;
	}

	public <T> T getValue(EvaluationContext context, Class<T> desiredResultType) throws EvaluationException {
		Object value = getValue(context);
		return ExpressionUtils.convert(context, value, desiredResultType);
	}

	public <T> T getValue(EvaluationContext context, Object rootObject, Class<T> desiredResultType)
			throws EvaluationException {
		Object value = getValue(context, rootObject);
		return ExpressionUtils.convert(null, value, desiredResultType);
	}

	public Class<?> getValueType() throws EvaluationException {
		return String.class;
	}

	public Class<?> getValueType(Object rootObject) throws EvaluationException {
		return String.class;
	}

	public Class<?> getValueType(EvaluationContext context) throws EvaluationException {
		return String.class;
	}

	public Class<?> getValueType(EvaluationContext context, Object rootObject) throws EvaluationException {
		return String.class;
	}

	public TypeDescriptor getValueTypeDescriptor() throws EvaluationException {
		return TypeDescriptor.valueOf(String.class);
	}

	public TypeDescriptor getValueTypeDescriptor(Object rootObject) throws EvaluationException {
		return TypeDescriptor.valueOf(String.class);
	}

	public TypeDescriptor getValueTypeDescriptor(EvaluationContext context) throws EvaluationException {
		return TypeDescriptor.valueOf(String.class);
	}

	public TypeDescriptor getValueTypeDescriptor(EvaluationContext context, Object rootObject)
			throws EvaluationException {
		return TypeDescriptor.valueOf(String.class);
	}

	public boolean isWritable(EvaluationContext context) throws EvaluationException {
		return false;
	}

	public boolean isWritable(EvaluationContext context, Object rootObject) throws EvaluationException {
		return false;
	}

	public boolean isWritable(Object rootObject) throws EvaluationException {
		return false;
	}

	public void setValue(EvaluationContext context, Object value) throws EvaluationException {
		throw new EvaluationException(literalValue, "Cannot call setValue() on a LiteralExpression");
	}

	public void setValue(Object rootObject, Object value) throws EvaluationException {
		throw new EvaluationException(literalValue, "Cannot call setValue() on a LiteralExpression");
	}

	public void setValue(EvaluationContext context, Object rootObject, Object value) throws EvaluationException {
		throw new EvaluationException(literalValue, "Cannot call setValue() on a LiteralExpression");
	}

	public final String getExpressionString() {
		return this.literalValue;
	}

}
