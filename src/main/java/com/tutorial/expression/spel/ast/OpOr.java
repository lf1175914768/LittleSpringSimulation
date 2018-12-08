package com.tutorial.expression.spel.ast;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * Represents the boolean OR operation.
 *
 * @author Andy Clement
 * @author Mark Fisher
 * @since 3.0
 */
public class OpOr extends Operator {
	
	public OpOr(int pos, SpelNodeImpl... operands) {
		super("or", pos, operands);
	}

	@Override
	public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		boolean leftValue;
		boolean rightValue;
		try {
			TypedValue typedValue = getLeftOperand().getValueInternal(state);
			assertTypedValueNotNull(typedValue);
			leftValue = (Boolean) state.convertValue(typedValue, TypeDescriptor.valueOf(Boolean.class));
		} catch (SpelEvaluationException e) {
			e.setPosition(getLeftOperand().getStartPosition());
			throw e;
		}
		
		if(leftValue == true) {
			return BooleanTypedValue.TRUE;   // no need to evaluate right operand
		}
		
		try {
			TypedValue typedValue = getRightOperand().getValueInternal(state);
			assertTypedValueNotNull(typedValue);
			rightValue = (Boolean) state.convertValue(typedValue, TypeDescriptor.valueOf(Boolean.class));
		} catch (SpelEvaluationException e) {
			e.setPosition(getRightOperand().getStartPosition());
			throw e;
		}
		return BooleanTypedValue.forValue(leftValue || rightValue);
	}

	private void assertTypedValueNotNull(TypedValue typedValue) {
		if(TypedValue.NULL.equals(typedValue)) {
			throw new SpelEvaluationException(SpelMessage.TYPE_CONVERSION_ERROR, "null", "boolean");
		}
	}

}
