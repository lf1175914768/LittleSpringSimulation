package com.tutorial.expression;

/**
 * @author Liufeng
 * Created on 2018年11月17日 上午11:35:53
 */
public class ExpressionInvocationTargetException extends EvaluationException {

	private static final long serialVersionUID = 8971458530742451727L;

	public ExpressionInvocationTargetException(int position, String message, Throwable cause) {
		super(position, message, cause);
	}

	public ExpressionInvocationTargetException(int position, String message) {
		super(position, message);
	}

	public ExpressionInvocationTargetException(String expressionString, String message) {
		super(expressionString, message);
	}

	public ExpressionInvocationTargetException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExpressionInvocationTargetException(String message) {
		super(message);
	}

}
