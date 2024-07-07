package dev.greenhouseteam.greenhouseconfig.api.lang.util;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

/**
 * Utility class for buffering tokens from a lexer.
 *
 * @param <T> the type of token types this buffer handles.
 */
public class TokenBuffer<T> {
    private final List<Token<T>> tokens = new ArrayList<>();
    private final TokenSupplier<T> tokenSupplier;
    private final Predicate<Token<T>> isEof;

    private int pos = 0;

    /**
     * Creates a new {@link TokenBuffer}.
     * <p>
     * This assumes that a null token is an EOF token.
     *
     * @param tokenSupplier the lexer method that supplies this buffer's type of tokens.
     */
    public TokenBuffer(TokenSupplier<T> tokenSupplier) {
        this(tokenSupplier, token -> token == null);
    }

    /**
     * Creates a new {@link TokenBuffer}.
     *
     * @param tokenSupplier the lexer method that supplies this buffer's type of tokens.
     * @param isEof         a predicate for checking if the given token is an EOF token.
     */
    public TokenBuffer(TokenSupplier<T> tokenSupplier, Predicate<Token<T>> isEof) {
        this.tokenSupplier = tokenSupplier;
        this.isEof = isEof;
    }

    /**
     * {@return the current position of the token stream}
     */
    public int getPos() {
        return pos;
    }

    /**
     * Checks if the given number of tokens are available beyond the current position in the stream.
     *
     * @param count the number of tokens to check for.
     * @return if the stream contains the given number of tokens beyond the current position.
     * @throws IOException if an IO error ocurrs while reading tokens.
     */
    public boolean isAvailable(int count) throws IOException {
        ensureRead(pos + count);
        return tokens.size() >= pos + count;
    }

    /**
     * Gets the token at the current position, or {@code null} if not available, and advances the position.
     *
     * @return the current token, or {@code null} if the token stream has reached its end.
     * @throws IOException if an IO error ocurrs while reading tokens.
     */
    public @Nullable Token<T> advance() throws IOException {
        ensureRead(pos + 1);
        if (tokens.size() < pos + 1) return null;
        return tokens.get(pos++);
    }

    /**
     * Gets the type of the token at the current position, or {@code null} if not available, and advances the position.
     *
     * @return the type of the current token, or {@code null} if the token stream has reached its end.
     * @throws IOException if an IO error ocurrs while reading tokens.
     */
    public @Nullable T advanceType() throws IOException {
        Token<T> token = advance();
        if (token == null) return null;
        return token.type();
    }

    /**
     * Peeks at the given token without advancing the stream position.
     *
     * @param offset the offset by which to peek at, can be zero to peek at the current token, or negative to peek
     *               behind.
     * @return the peeked token, or {@code null} if the stream ends before the requested token.
     * @throws IOException if an IO error ocurrs while reading tokens.
     */
    public @Nullable Token<T> peek(int offset) throws IOException {
        ensureRead(pos + offset + 1);
        if (tokens.size() > pos + offset) return tokens.get(pos + offset);
        return null;
    }

    /**
     * Peeks at the type of the given token without advancing the stream position.
     *
     * @param offset the offset by which to peek at, can be zero to peek at the current token type, or negative to peek
     *               behind.
     * @return the peeked token's type, or {@code null} if the stream ends before the requested token.
     * @throws IOException if an IO error ocurrs while reading tokens.
     */
    public @Nullable T peekType(int offset) throws IOException {
        Token<T> token = peek(offset);
        if (token == null) return null;
        return token.type();
    }

    /**
     * Gets the current token and advances the stream position, throwing an error if the token does not exist or is of
     * an unexpectd type.
     *
     * @param expectedTypes the expected token types.
     * @return the token of one of the expected token types.
     * @throws EOFException if no more tokens are available in the stream.
     * @throws IOException  if an unexpected token is the next token in the stream, or if an IO error ocurrs while
     *                      reading the next token.
     */
    public Token<T> expect(T... expectedTypes) throws IOException {
        Token<T> token = advance();
        if (token == null)
            throw new EOFException("Unexpected end of stream. Expected one of: " + Arrays.toString(expectedTypes));

        for (T ty : expectedTypes) {
            if (token.type() == ty) return token;
        }

        throw new IOException(
            "Unexpected token: " + token.type() + " (" + token.lexeme() + ") on line " + token.line() + ", column " +
                token.col() + ". Expected one of: " + Arrays.toString(expectedTypes));
    }

    private void ensureRead(int requiredPos) throws IOException {
        while (tokens.size() < requiredPos) {
            Token<T> next = tokenSupplier.nextToken();
            if (isEof.test(next)) return;
            tokens.add(next);
        }
    }
}
