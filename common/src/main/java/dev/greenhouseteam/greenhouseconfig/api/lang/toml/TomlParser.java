package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.io.Reader;

/**
 * Parses a string into a TOML value tree.
 */
public class TomlParser {
    private final TomlLexer lexer;

    /**
     * Creates a new TOML parser.
     *
     * @param reader the reader to read TOML source code from.
     */
    public TomlParser(Reader reader) {
        lexer = new TomlLexer(reader);
    }
}
