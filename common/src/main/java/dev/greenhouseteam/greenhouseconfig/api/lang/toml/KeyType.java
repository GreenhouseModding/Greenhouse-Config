package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

enum KeyType {
    // Single-character tokens
    LEFT_BRACKET,
    RIGHT_BRACKET,
    DOT,
    EQUALS,

    // Literals
    IDENT,
    QUOTED_IDENT,

    // Comments
    COMMENT
}
