package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午10:20:01
 */
public class Identifier extends SpelNodeImpl {
	
	private final TypedValue id;

	public Identifier(String payLoad, int pos) {
		super(pos);
		this.id = new TypedValue(payLoad); 
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		return this.id;
	}

	@Override
	public String toStringAST() {
		return (String) this.id.getValue();
	}

}
