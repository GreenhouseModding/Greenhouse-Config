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
        switch (c) {
            // newlines
            case '\n' -> {
                // line number is handled by advance
            }
            // whitespace
            case ' ', '\r', '\t' -> {
            }
            // single-character tokens
            case '[' -> addToken(Token.Type.LEFT_BRACKET);
            case ']' -> addToken(Token.Type.RIGHT_BRACKET);
            case '{' -> addToken(Token.Type.LEFT_BRACE);
            case '}' -> addToken(Token.Type.RIGHT_BRACE);
            case ',' -> addToken(Token.Type.COMMA);
            case '.' -> addToken(Token.Type.DOT);
            case '=' -> addToken(Token.Type.EQUALS);
            // comments
            case '#' -> {
                // remove the space at the beginning of the comment if it exists
                if (peek() == ' ') advance();
                // collect the comment chars
                int strStart = current;
                while (peek() != '\n' && !isAtEnd()) advance();
                addToken(Token.Type.COMMENT, source.substring(strStart, current));
            }
            // literals
            case '"' -> {
                if (match("\"\"")) multiLineString();
                else string();
            }
            default -> {
                // TODO: other literals
                addError("Unexpected character. Expected one of '[', ']', '{', '}', ',', '.', '='.");
            }
        }
    }

    private void string() {
        // TODO: handle escapes & newlines
        while (peek() != '"' && !isAtEnd()) advance();

        if (isAtEnd()) addError("Unexpected end of string");

        // consume the closing quote
        advance();

        // trim surrouncing quotes
        String value = source.substring(start + 1, current - 1);
        addToken(Token.Type.STRING, value);
    }

    private void multiLineString() {
        // TODO
    }

    private char advance() {
        col++;
        char c = source.charAt(current++);
        if (c == '\n') {
            col = 1;
            line++;
        }
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        advance();
        return true;
    }

    private boolean match(String expected) {
        if (isAtEnd()) return false;

        // check string matches
        int expectedLen = expected.length();
        if (source.length() < current + expectedLen) return false;
        for (int i = 0; i < expectedLen; i++) {
            if (source.charAt(current + i) != expected.charAt(i)) return false;
        }

        // if string does match, consume it
        for (int i = 0; i < expectedLen; i++) {
            advance();
        }

        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peek(int offset) {
        if (current + offset >= source.length()) return '\0';
        return source.charAt(current + offset);
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
