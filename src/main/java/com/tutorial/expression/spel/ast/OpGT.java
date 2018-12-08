package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * Implements greater-than operator.
 *
 * @author Andy Clement
 * @since 3.0
 */
public class OpGT extends Operator {
	
	public OpGT(int pos, SpelNodeImpl... operands) {
		super(">", pos, operands);
	}

	@Override
	public BooleanTypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		Object left = getLeftOperand().getValueInternal(expressionState).getValue();
		Object right = getRightOperand().getValueInternal(expressionState).getValue();
		if(left instanceof Number && right instanceof Number) {
			Number leftNumber = (Number) left;
			Number rightNumber = (Number) right;
			if(leftNumber instanceof Double || rightNumber instanceof Double) {
				return BooleanTypedValue.forValue(leftNumber.doubleValue() > rightNumber.doubleValue());
			} else if(leftNumber instanceof Long || rightNumber instanceof Long) {
			 	return BooleanTypedValue.forValue(leftNumber.longValue() > rightNumber.longValue());
			} else {
				return BooleanTypedValue.forValue(leftNumber.intValue() > rightNumber.intValue());
			}
		}
		return BooleanTypedValue.forValue(expressionState.getTypeComparator().compare(left, right) > 0);
	}

}
