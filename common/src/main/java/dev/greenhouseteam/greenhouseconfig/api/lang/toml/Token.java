package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record Token(Type type, String lexeme, Object literal, int line, int col, int start, int len) {
    enum Type {
        // Single-character tokens
        LEFT_BRACKET,
        RIGHT_BRACKET,
        LEFT_BRACE,
        RIGHT_BRACE,
        COMMA,
        DOT,
        EQUALS,

        // Literals
        IDENT,
        QUOTED_IDENT,
        STRING,
        NUMBER,

        // Keywords
        TRUE,
        FALSE,

        // Comments
        COMMENT,

        EOF
    }
}
