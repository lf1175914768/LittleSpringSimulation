package com.tutorial.expression.spel.standard;

public enum TokenKind {
	// ordered by priority - operands first
		LITERAL_INT, LITERAL_LONG, LITERAL_HEXINT, LITERAL_HEXLONG, LITERAL_STRING, LITERAL_REAL, LITERAL_REAL_FLOAT, 
		LPAREN("("), RPAREN(")"), COMMA(","), IDENTIFIER,
		COLON(":"),HASH("#"),RSQUARE("]"), LSQUARE("["), 
		LCURLY("{"),RCURLY("}"),
		DOT("."), PLUS("+"), STAR("*"),  MINUS("-"), SELECT_FIRST("^["), SELECT_LAST("$["), QMARK("?"), PROJECT("!["),
		DIV("/"), GE(">="), GT(">"), LE("<="), LT("<"), EQ("=="), NE("!="),
		MOD("%"), NOT("!"), ASSIGN("="), INSTANCEOF("instanceof"), MATCHES("matches"), BETWEEN("between"),
		SELECT("?["),   POWER("^"),
		ELVIS("?:"), SAFE_NAVI("?."), BEAN_REF("@")
		;
	
	char[] tokenChars;
	private boolean hasPayload;
	
	private TokenKind(String tokenString) {
		tokenChars = tokenString.toCharArray();
		this.hasPayload = tokenString.length() == 0;
	}
	
	private TokenKind() {
		this("");
	}
	
	@Override
	public String toString() {
		return this.name() + (tokenChars.length != 0 ? "(" + new String(tokenChars) + ")" : "");
	}

	public boolean hasPayload() {
		return hasPayload;
	}
	
	public int getLength() {
		return tokenChars.length;
	}
}
