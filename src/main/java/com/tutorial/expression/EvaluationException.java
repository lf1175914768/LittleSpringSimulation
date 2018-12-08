package com.tutorial.expression;

/**
 * Represent an exception that occurs during expression evaluation.
 * 
 * @author Andy Clement
 * @since 3.0
 */
@SuppressWarnings("serial")
public class EvaluationException extends ExpressionException {
	
	/**
	 * Creates a new expression evaluation exception.
	 * @param position the position in the expression where the problem occurred
	 * @param message description of the problem that occurred
	 */ 
	public EvaluationException(int position, String message) {
		super(position, message);
	}
	
	/**
	 * Creates a new expression evaluation exception.
	 * @param expressionString the expression that could not be evaluated
	 * @param message description of the problem that occurred
	 */
	public EvaluationException(String expressionString, String message) {
		super(expressionString, message);
	}
	
	/**
	 * Creates a new expression evaluation exception.
	 * @param position the position in the expression where the problem occurred
	 * @param message description of the problem that occurred
	 * @param cause the underlying cause of this exception
	 */ 
	public EvaluationException(int position, String message, Throwable cause) {
		super(position, message, cause);
	}
	
	/**
	 * Creates a new expression evaluation exception.
	 * @param message description of the problem that occurred
	 */ 
	public EvaluationException(String message) {
		super(message);
	}

	public EvaluationException(String message, Throwable cause) {
		super(message,cause);
	}

}
