package com.tutorial.expression.spel.ast;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * Represents the boolean AND operation.
 *
 * @author Andy Clement
 * @author Mark Fisher
 * @since 3.0
 */
public class OpAnd extends Operator {
	
	public OpAnd(int pos, SpelNodeImpl... operands) {
		super("and", pos, operands);
	}
	
	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		boolean leftValue;
		boolean rightValue;

		try {
			TypedValue typedValue = getLeftOperand().getValueInternal(state);
			this.assertTypedValueNotNull(typedValue);
			leftValue = (Boolean)state.convertValue(typedValue, TypeDescriptor.valueOf(Boolean.class));
		}
		catch (SpelEvaluationException ee) {
			ee.setPosition(getLeftOperand().getStartPosition());
			throw ee;
		}

		if (leftValue == false) {
			return BooleanTypedValue.forValue(false); // no need to evaluate right operand
		}

		try {
			TypedValue typedValue = getRightOperand().getValueInternal(state);
			this.assertTypedValueNotNull(typedValue);
			rightValue = (Boolean)state.convertValue(typedValue, TypeDescriptor.valueOf(Boolean.class));
		}
		catch (SpelEvaluationException ee) {
			ee.setPosition(getRightOperand().getStartPosition());
			throw ee;
		}

		return /* leftValue && */BooleanTypedValue.forValue(rightValue);
	}

	private void assertTypedValueNotNull(TypedValue typedValue) {
		if (TypedValue.NULL.equals(typedValue)) {
			throw new SpelEvaluationException(SpelMessage.TYPE_CONVERSION_ERROR, "null", "boolean");
		}
	}
}
