package dev.greenhouseteam.greenhouseconfig.api.lang.util;

import java.io.IOException;

/**
 * Describes a supplier of tokens.
 *
 * @param <T> the token type of this supplier.
 */
public interface TokenSupplier<T> {
    /**
     * {@return the next token}
     *
     * @throws IOException if an IO error ocurrs while reading the next token.
     */
    Token<T> nextToken() throws IOException;
}
