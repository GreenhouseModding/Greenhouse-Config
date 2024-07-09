package dev.greenhouseteam.greenhouseconfig.impl.lang.toml;

public record LexError(int line, int col, int start, int len, String offending, String message) {
}
