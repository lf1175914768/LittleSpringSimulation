package com.tutorial.expression.spel.ast;

import java.util.List;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypeComparator;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.BooleanTypedValue;

/**
 * Represents the between operator. The left operand to between must be a single value and the right operand must be a
 * list - this operator returns true if the left operand is between (using the registered comparator) the two elements
 * in the list. The definition of between being inclusive follows the SQL BETWEEN definition.
 * 
 * @author Andy Clement
 * @since 3.0
 */
public class OperatorBetween extends Operator {
	
	public OperatorBetween(int pos, SpelNodeImpl... operands) {
		super("between", pos, operands);
	}

	/**
	 * Returns a boolean based on whether a value is in the range expressed. The first operand is any value whilst the
	 * second is a list of two values - those two values being the bounds allowed for the first operand (inclusive).
	 * @param state the expression state
	 * @return true if the left operand is in the range specified, false otherwise
	 * @throws EvaluationException if there is a problem evaluating the expression
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		Object left = getLeftOperand().getValueInternal(state).getValue();
		Object right = getRightOperand().getValueInternal(state).getValue();
		if(!(right instanceof List) || ((List<?>) right).size() != 2) {
			throw new SpelEvaluationException(getRightOperand().getStartPosition(),
					SpelMessage.BETWEEN_RIGHT_OPERAND_MUST_BE_TWO_ELEMENT_LIST);
		}
		List<?> l = (List<?>) right;
		Object low = l.get(0);
		Object high = l.get(1);
		TypeComparator comparator = state.getTypeComparator();
		try {
			return BooleanTypedValue.forValue((comparator.compare(left, low) >= 0 
					&& comparator.compare(left, high) <= 0));
		} catch (SpelEvaluationException ex) {
			ex.setPosition(getStartPosition());
			throw ex;
		}
	}

}
