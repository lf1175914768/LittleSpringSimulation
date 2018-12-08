package com.tutorial.beans.factory.parsing;

import java.util.Stack;

/**
 * Simple {@link Stack}-based structure for tracking the logical position during
 * a parsing process. {@link Entry entries} are added to the stack at
 * each point during the parse phase in a reader-specific manner.
 *
 * <p>Calling {@link #toString()} will render a tree-style view of the current logical
 * position in the parse phase. This representation is intended for use in
 * error messages.
 *
 * @author Rob Harrop
 * @since 2.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class ParseState {
	
	/**
	 * Tab character used when rendering the tree-style representation.
	 */
	private static final char TAB = '\t';
	
	private final Stack state;
	
	public ParseState() {
		this.state = new Stack();
	}
	
	/**
	 * Create a new <code>ParseState</code> whose {@link Stack} is a {@link Object#clone clone}
	 * of that of the passed in <code>ParseState</code>.
	 */
	private ParseState(ParseState other) {
		this.state = (Stack) other.state.clone();
	}
	
	/**
	 * Add a new {@link Entry} to the {@link Stack}.
	 */
	public void push(Entry entry) {
		this.state.push(entry);
	}
	
	/**
	 * Remove an {@link Entry} from the {@link Stack}.
	 */
	public void pop() {
		this.state.pop();
	}
	
	/**
	 * Return the {@link Entry} currently at the top of the {@link Stack} or
	 * <code>null</code> if the {@link Stack} is empty.
	 */
	public Entry peek() {
		return (Entry) (this.state.empty() ? null : this.state.peek());
	}
	
	/**
	 * Create a new instance of {@link ParseState} which is an independent snapshot
	 * of this instance.
	 */
	public ParseState snapshot() {
		return new ParseState(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.state.size(); i++) {
			if(i > 0) {
				sb.append('\n');
				for(int j = 0; j < i; j++) {
					sb.append(TAB);
				}
				sb.append("-> ");
			}
			sb.append(this.state.get(i));
		}
		return sb.toString();
	}
	
	/**
	 * Marker interface for entries into the {@link ParseState}.
	 */
	public interface Entry {
		
	}

}
