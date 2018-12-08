package com.tutorial.expression.spel;

import com.tutorial.expression.EvaluationException;

/**
 * Root exception for Spring EL related exceptions. Rather than holding a hard coded string indicating the problem, it
 * records a message key and the inserts for the message. See {@link SpelMessage} for the list of all possible messages
 * that can occur.
 * 
 * @author Andy Clement
 * @since 3.0
 */
@SuppressWarnings("serial")
public class SpelEvaluationException extends EvaluationException {
	
	private SpelMessage message;
	private Object[] inserts;

	public SpelEvaluationException(SpelMessage message, Object... inserts) {
		super(message.formatMessage(0, inserts)); // TODO poor position information, can the callers not really supply something?
		this.message = message;
		this.inserts = inserts;
	}

	public SpelEvaluationException(int position, SpelMessage message, Object... inserts) {
		super(position, message.formatMessage(position, inserts)); 
		this.message = message;
		this.inserts = inserts;
	}

	public SpelEvaluationException(int position, Throwable cause,
			SpelMessage message, Object... inserts) {
		super(position,message.formatMessage(position,inserts),cause);
		this.message = message;
		this.inserts = inserts;
	}

	public SpelEvaluationException(Throwable cause, SpelMessage message, Object... inserts) {
		super(message.formatMessage(0,inserts),cause);
		this.message = message;
		this.inserts = inserts;
	}

	/**
	 * @return a formatted message with inserts applied
	 */
	@Override
	public String getMessage() {
		if (message != null)
			return message.formatMessage(position, inserts);
		else
			return super.getMessage();
	}

	/**
	 * @return the message code
	 */
	public SpelMessage getMessageCode() {
		return this.message;
	}

	/**
	 * Set the position in the related expression which gave rise to this exception.
	 * 
	 * @param position the position in the expression that gave rise to the exception
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the message inserts
	 */
	public Object[] getInserts() {
		return inserts;
	}

}
