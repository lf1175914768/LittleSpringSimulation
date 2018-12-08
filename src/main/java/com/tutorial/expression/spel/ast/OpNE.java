package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.support.BooleanTypedValue;

public class OpNE extends Operator {

	public OpNE(int pos, SpelNodeImpl... operands) {
		super("!=", pos, operands);
	}

	@Override
	public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		Object left = getLeftOperand().getValueInternal(state).getValue();
		Object right = getRightOperand().getValueInternal(state).getValue();
		if (left instanceof Number && right instanceof Number) {
			Number op1 = (Number) left;
			Number op2 = (Number) right;
			if (op1 instanceof Double || op2 instanceof Double) {
				return BooleanTypedValue.forValue(op1.doubleValue() != op2.doubleValue());
			} else if (op1 instanceof Long || op2 instanceof Long) {
				return BooleanTypedValue.forValue(op1.longValue() != op2.longValue());
			} else {
				return BooleanTypedValue.forValue(op1.intValue() != op2.intValue());
			}
		}

		if (left!=null && (left instanceof Comparable)) {
			return BooleanTypedValue.forValue(state.getTypeComparator().compare(left, right) != 0);
		} else {
			return BooleanTypedValue.forValue(left!=right);
		}
	}

}
