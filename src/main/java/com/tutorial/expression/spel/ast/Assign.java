package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午11:16:16
 */
public class Assign extends SpelNodeImpl {

	public Assign(int pos, SpelNodeImpl... operands) {
		super(pos, operands);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		TypedValue newValue = children[1].getValueInternal(expressionState);
		getChild(0).setValue(expressionState, newValue.getValue());
		return newValue;
	}

	@Override
	public String toStringAST() {
		return new StringBuilder().append(getChild(0).toStringAST()).append("=").append(getChild(1).toStringAST())
				.toString();
	}

}
