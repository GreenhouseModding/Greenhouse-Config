package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

/**
 * Parses a string into a TOML value tree.
 */
public class TomlParser {
    private final TomlLexer lexer;

    /**
     * Creates a new TOML parser.
     *
     * @param tomlSource the toml source code to parse.
     */
    public TomlParser(String tomlSource) {
        lexer = new TomlLexer(tomlSource);
    }
}
