package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record LexError(int line, int start, int len, String offending, String message) {
}
