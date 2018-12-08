package com.tutorial.expression.spel.ast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.support.BooleanTypedValue;

public class OperatorMatches extends Operator {
	
	public OperatorMatches(int pos, SpelNodeImpl... operands) {
		super("matches", pos, operands);
	}

	/**
	 * Check the first operand matches the regex specified as the second operand.
	 * @param state the expression state
	 * @return true if the first operand matches the regex specified as the second operand, otherwise false
	 * @throws EvaluationException if there is a problem evaluating the expression (e.g. the regex is invalid)
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState state) 
			throws EvaluationException {
		SpelNodeImpl leftOp = getLeftOperand();
		SpelNodeImpl rightOp = getRightOperand();
		Object left = leftOp.getValue(state, String.class);
		Object right = getRightOperand().getValueInternal(state).getValue();
		try {
			if(!(left instanceof String)) {
				throw new SpelEvaluationException(leftOp.getStartPosition(),
						SpelMessage.INVALID_FIRST_OPERAND_FOR_MATCHES_OPERATOR, left);
			}
			if(!(right instanceof String)) {
				throw new SpelEvaluationException(rightOp.getStartPosition(),
						SpelMessage.INVALID_SECOND_OPERAND_FOR_MATCHES_OPERATOR, right);
			}
			Pattern pattern = Pattern.compile((String) right);
			Matcher matcher = pattern.matcher((String) left);
			return BooleanTypedValue.forValue(matcher.matches());
		} catch (PatternSyntaxException e) {
			throw new SpelEvaluationException(rightOp.getStartPosition(), e, SpelMessage.INVALID_PATTERN, right);
		}
	}

}
