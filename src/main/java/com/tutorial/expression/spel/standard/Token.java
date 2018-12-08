package com.tutorial.expression.spel.standard;

/**
 * Holder for a kind of token, the associated data and its position in the input data stream (start/end).
 * 
 * @author Andy Clement
 * @since 3.0
 */
class Token {
	
	TokenKind kind;
	
	String data;
	
	int startpos;
	
	int endpos;
	
	/**
	 * Constructor for use when there is no particular data for the token (eg. TRUE or '+')
	 * @param startpos the exact start
	 * @param endpos the index to the last character
	 */
	Token(TokenKind tokenKind, int startPos, int endPos) {
		this.kind = tokenKind;
		this.startpos = startPos;
		this.endpos = endPos;
	}
	
	Token(TokenKind tokenKind, char[] tokenData, int startPos, int endPos) {
		this(tokenKind, startPos, endPos);
		this.data = new String(tokenData);
	}
	
	public TokenKind getKind() {
		return this.kind;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("[").append(kind.toString());
		if(kind.hasPayload()) {
			s.append(":").append(data);
		}
		s.append("]");
		s.append("(").append(startpos).append(",").append(endpos).append(")");
		return s.toString();
	}
	
	public boolean isIdentifier() {
		return this.kind == TokenKind.IDENTIFIER;
	}
	
	public boolean isNumericRelationalOperator() {
		return kind==TokenKind.GT || kind==TokenKind.GE || kind==TokenKind.LT ||
				kind==TokenKind.LE || kind==TokenKind.EQ || kind==TokenKind.NE;
	}

	public String stringValue() {
		return data;
	}

	public Token asInstanceOfToken() {
		return new Token(TokenKind.INSTANCEOF,startpos,endpos);
	}

	public Token asMatchesToken() {
		return new Token(TokenKind.MATCHES,startpos,endpos);
	}

	public Token asBetweenToken() {
		return new Token(TokenKind.BETWEEN,startpos,endpos);
	}

}
