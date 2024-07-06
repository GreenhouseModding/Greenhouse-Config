package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record Token<T>(T type, String lexeme, Object literal, int line, int col, int start, int len) {
}
