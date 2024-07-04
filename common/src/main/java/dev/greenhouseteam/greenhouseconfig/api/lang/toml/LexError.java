package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record LexError(int line, int col, int start, int len, String offending, String message) {
}
