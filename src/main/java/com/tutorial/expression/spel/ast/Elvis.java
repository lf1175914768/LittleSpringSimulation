package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午11:28:46
 */
public class Elvis extends SpelNodeImpl {

	public Elvis(int pos, SpelNodeImpl... operands) {
		super(pos, operands);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		TypedValue value = children[0].getValueInternal(expressionState);
		if(value.getValue() != null && 
				!((value.getValue() instanceof String) && 
						((String) value.getValue()).length() == 0)) 
			return value;
		else 
			return children[1].getValueInternal(expressionState);
	}

	@Override
	public String toStringAST() {
		return new StringBuilder().append(getChild(0).toStringAST()).append(" ?: ").
				append(getChild(1).toStringAST()).toString();
	}

}
