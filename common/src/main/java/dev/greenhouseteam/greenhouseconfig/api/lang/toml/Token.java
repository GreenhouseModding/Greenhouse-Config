package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

record Token() {
    
    enum Type {
        // Single-character tokens
        LEFT_BRACKET, RIGHT_BRACKET, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, 
    }
}
