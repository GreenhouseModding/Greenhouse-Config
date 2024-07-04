package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.util.ArrayList;
import java.util.List;

class TomlLexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final List<LexError> errors = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int col = 1;

    TomlLexer(String source) {
        this.source = source;
    }

    List<Token> getTokens() {
        return tokens;
    }

    List<LexError> getErrors() {
        return errors;
    }

    void scanTokens() {
        while (!isAtEnd()) {
            // reset everything for the next token
            start = current;
            scanToken();
        }

        tokens.add(new Token(Token.Type.EOF, "", null, line, col, source.length(), 0));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        // TODO: manage line & col
        switch(c) {
            case '[' -> addToken(Token.Type.LEFT_BRACKET);
            case ']' -> addToken(Token.Type.RIGHT_BRACKET);
            case '{' -> addToken(Token.Type.LEFT_BRACE);
            case '}' -> addToken(Token.Type.RIGHT_BRACE);
            case ',' -> addToken(Token.Type.COMMA);
            case '.' -> addToken(Token.Type.DOT);
            case '=' -> addToken(Token.Type.EQUALS);
            default -> addError("Unexpected character. Expected one of '[', ']', '{', '}', ',', '.', '='.");
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(Token.Type type) {
        addToken(type, null);
    }

    private void addToken(Token.Type type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line, col, start, current - start));
    }
    
    private void addError(String message) {
        String lexeme = source.substring(start, current);
        errors.add(new LexError(line, col, start, current - start, lexeme, message));
    }
}
