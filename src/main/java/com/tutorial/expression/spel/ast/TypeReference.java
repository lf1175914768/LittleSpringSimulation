package com.tutorial.expression.spel.ast;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午10:34:28
 */
public class TypeReference extends SpelNodeImpl {

	public TypeReference(int pos, SpelNodeImpl operands) {
		super(pos, operands);
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		String typeName = (String) children[0].getValueInternal(expressionState).getValue();
		if(typeName.indexOf(".") == -1 && Character.isLowerCase(typeName.charAt(0))) {
			TypeCode tc = TypeCode.forName(typeName.toUpperCase());
			if(tc != TypeCode.OBJECT) {
				// it is a primitive type
				return new TypedValue(tc.getType());
			}
		}
		return new TypedValue(expressionState.findType(typeName));
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		sb.append("T(");
		sb.append(getChild(0).toStringAST());
		sb.append(")");
		return sb.toString();
	}

}
