package dev.greenhouseteam.greenhouseconfig.impl.lang.toml;

public enum ValueType {
    // Single-character tokens
    LEFT_BRACKET,
    RIGHT_BRACKET,
    LEFT_BRACE,
    RIGHT_BRACE,
    COMMA,
    EQUALS,
    NEW_LINE,
    
    // Literals
    STRING,
    INTEGER,
    FLOAT,
    BOOLEAN,
    ZONED_DATE_TIME,
    LOCAL_DATE_TIME,
    LOCAL_DATE,
    LOCAL_TIME,
    RAW_LITERAL,

    // Comments
    COMMENT
}
