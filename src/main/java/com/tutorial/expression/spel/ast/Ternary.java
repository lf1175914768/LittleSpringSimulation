package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午11:34:36
 */
public class Ternary extends SpelNodeImpl {

	public Ternary(int pos, SpelNodeImpl... operands) {
		super(pos, operands);
	}

	/**
	 * Evaluate the condition and if true evaluate the first alternative, otherwise evaluate the second alternative.
	 * @param state the expression state
	 * @throws EvaluationException if the condition does not evaluate correctly to a boolean or there is a problem
	 * executing the chosen alternative
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		Boolean value = children[0].getValue(expressionState, Boolean.class);
		if(value == null) {
			throw new SpelEvaluationException(getChild(0).getStartPosition(),
					SpelMessage.TYPE_CONVERSION_ERROR, "null", "boolean");
		}
		if(value.booleanValue()) {
			return children[1].getValueInternal(expressionState);
		} else {
			return children[2].getValueInternal(expressionState);
		}
	}

	@Override
	public String toStringAST() {
		return new StringBuilder().append(getChild(0).toStringAST()).append(" ? ").append(getChild(1).toStringAST())
				.append(" : ").append(getChild(2).toStringAST()).toString();
	}

}
