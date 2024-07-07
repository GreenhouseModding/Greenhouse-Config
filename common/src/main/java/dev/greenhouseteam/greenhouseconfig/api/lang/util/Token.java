package dev.greenhouseteam.greenhouseconfig.api.lang.util;

/**
 * Represents a token from a lexer.
 *
 * @param type    the token type.
 * @param lexeme  the token's lexeme.
 * @param literal the token's literal value if any.
 * @param line    the line the token was found on.
 * @param col     the column the token was found on.
 * @param start   the starting index of the token in the file.
 * @param len     the length of the token.
 * @param <T>     the type of the token's type.
 */
public record Token<T>(T type, String lexeme, Object literal, int line, int col, int start, int len) {
}
