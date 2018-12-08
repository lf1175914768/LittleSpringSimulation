package com.tutorial.expression.spel.ast;

import com.tutorial.expression.AccessException;
import com.tutorial.expression.BeanResolver;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;

/**
 * Represents a bean reference to a type, for example "@foo" or "@'foo.bar'"
 * 
 * @author Liufeng
 * Created on 2018年11月12日 下午8:47:37
 */
public class BeanReference extends SpelNodeImpl {
	
	private String beanName;

	public BeanReference(int pos, String beanName) {
		super(pos);
		this.beanName = beanName;
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		BeanResolver beanResolver = state.getEvaluationContext().getBeanResolver();
		if(beanResolver == null) {
			throw new SpelEvaluationException(getStartPosition(), 
					SpelMessage.NO_BEAN_RESOLVER_REGISTERED, beanName);
		}
		try {
			TypedValue bean = new TypedValue(beanResolver.resolve(state.getEvaluationContext(), beanName));
			return bean;
		} catch (AccessException e) {
			throw new SpelEvaluationException(getStartPosition(), e, 
					SpelMessage.EXCEPTION_DURING_BEAN_RESOLUTION, beanName, e.getMessage());
		}
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		if(beanName.indexOf('.') == -1) {
			sb.append(beanName);
		} else {
			sb.append("'").append(beanName).append("'");
		}
		return sb.toString();
	}

}
