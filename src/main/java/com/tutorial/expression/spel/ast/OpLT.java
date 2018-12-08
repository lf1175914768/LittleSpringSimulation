package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * Implements the less-than operator.
 *
 * @author Andy Clement
 * @since 3.0
 */
public class OpLT extends Operator {

	public OpLT(int pos, SpelNodeImpl... operands) {
		super("<", pos, operands);
	}
	
	@Override
	public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		Object left = getLeftOperand().getValueInternal(state).getValue();
		Object right = getRightOperand().getValueInternal(state).getValue();
		// TODO could leave all of these to the comparator - just seems quicker to do some here
		if (left instanceof Number && right instanceof Number) {
			Number leftNumber = (Number) left;
			Number rightNumber = (Number) right;
			if (leftNumber instanceof Double || rightNumber instanceof Double) {
				return BooleanTypedValue.forValue(leftNumber.doubleValue() < rightNumber.doubleValue());
			} else if (leftNumber instanceof Long || rightNumber instanceof Long) {
				return BooleanTypedValue.forValue(leftNumber.longValue() < rightNumber.longValue());
			} else {
				return BooleanTypedValue.forValue(leftNumber.intValue() < rightNumber.intValue());
			}
		}
		return BooleanTypedValue.forValue(state.getTypeComparator().compare(left, right) < 0);
	}

}
