package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import dev.greenhouseteam.greenhouseconfig.api.lang.util.CharBuffer;

class TomlLexer {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("(\\+|-)?[0-9_]+");
    private static final Pattern INTEGER_RADIX_PATTERN = Pattern.compile("(\\+|-)?0(x|o|b)[0-9_]+");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("(\\+|-)?[0-9_]+(\\.[0-9_]+)?((E|e)(\\+|-)?[0-9_]+)?");
    private static final Pattern ZONED_DATE_TIME_PATTERN =
        Pattern.compile("\\d{4}-\\d{2}-\\d{2}(T|t| )\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|z|((\\+|-)\\d{2}(:\\d{2})?))");
    private static final Pattern LOCAL_DATE_TIME_PATTERN =
        Pattern.compile("\\d{4}-\\d{2}-\\d{2}(T|t| )\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?");
    private static final Pattern LOCAL_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern LOCAL_TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?");

    private final CharBuffer buffer;
    private final List<LexError> errors = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int col = 1;

    TomlLexer(Reader reader) {
        this(new CharBuffer(reader));
    }

    TomlLexer(CharBuffer buffer) {
        this.buffer = buffer;
    }

    List<LexError> drainErrors() {
        List<LexError> errorsCopy = new ArrayList<>(errors);
        errors.clear();
        return errorsCopy;
    }

    private boolean isAtEnd() throws IOException {
        return !buffer.isAvailable(1);
    }

    @Nullable
    Token<KeyType> nextKeyToken() throws IOException {
        Token<KeyType> token = null;
        while (token == null && !isAtEnd()) {
            // mark the beginning of the token, cause we shouldn't need anything from before this
            buffer.mark();

            char c = advance();
            token = switch (c) {
                // newlines
                case '\n' -> {
                    // line number is handled by advance
                    yield null;
                }
                // whitespace
                case ' ', '\r', '\t' -> {
                    yield null;
                }
                // single-character tokens
                case '[' -> makeToken(KeyType.LEFT_BRACKET);
                case ']' -> makeToken(KeyType.RIGHT_BRACKET);
                case '.' -> makeToken(KeyType.DOT);
                case '=' -> makeToken(KeyType.EQUALS);
                // comments
                case '#' -> {
                    // remove the space at the beginning of the comment if it exists
                    if (peek() == ' ') advance();
                    // collect the comment chars
                    int strStart = current;
                    while (peek() != '\n' && !isAtEnd()) advance();
                    yield makeToken(KeyType.COMMENT, buffer.substring(strStart, current));
                }
                // literals
                case '"' -> string(KeyType.QUOTED_IDENT);
                case '\'' -> literalString(KeyType.QUOTED_IDENT);
                default -> keyLiteral(c);
            };
        }

        return token;
    }

    private Token<KeyType> keyLiteral(char c) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (isKeyLiteralChar(c)) {
            builder.append(c);
        } else {
            addError("Unexpected character: '" + c + "'");
        }

        while (!isAtEnd() && !isReservedKeyChar(peek())) {
            c = advance();

            if (isKeyLiteralChar(c)) {
                builder.append(c);
            } else {
                addError("Unexpected character: '" + c + "'");
            }
        }

        return makeToken(KeyType.IDENT, builder.toString());
    }

    private static boolean isKeyLiteralChar(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9') || c == '_' || c == '-';
    }

    private static boolean isReservedKeyChar(char c) {
        return c == '\n' || c == ' ' || c == '\r' || c == '\t' || c == '[' || c == ']' || c == '.' || c == '=' ||
            c == '#' || c == '"' || c == '\'';
    }

    @Nullable
    Token<ValueType> nextValueToken() throws IOException {
        Token<ValueType> token = null;
        while (token == null && !isAtEnd()) {
            char c = advance();
            token = switch (c) {
                // newlines mark the end of a value-side token
                case '\n' -> makeToken(ValueType.NEW_LINE);
                // whitespace
                case ' ', '\r', '\t' -> {
                    yield null;
                }
                // single-character tokens
                case '[' -> makeToken(ValueType.LEFT_BRACKET);
                case ']' -> makeToken(ValueType.RIGHT_BRACKET);
                case '{' -> makeToken(ValueType.LEFT_BRACE);
                case '}' -> makeToken(ValueType.RIGHT_BRACE);
                case ',' -> makeToken(ValueType.COMMA);
                case '=' -> makeToken(ValueType.EQUALS);
                // comments
                case '#' -> {
                    // remove the space at the beginning of the comment if it exists
                    if (peek() == ' ') advance();
                    // collect the comment chars
                    int strStart = current;
                    while (peek() != '\n' && !isAtEnd()) advance();
                    yield makeToken(ValueType.COMMENT, buffer.substring(strStart, current));
                }
                // literals
                case '"' -> {
                    if (match("\"\"")) yield multiLineString();
                    else yield string(ValueType.STRING);
                }
                case '\'' -> {
                    if (match("''")) yield multiLineLiteralString();
                    else yield literalString(ValueType.STRING);
                }
                default -> valueLiteral(c);
            };
        }

        return token;
    }

    private Token<ValueType> valueLiteral(char c) throws IOException {
        StringBuilder builder = new StringBuilder();

        if (isValueLiteralChar(c)) {
            builder.append(c);
        } else {
            addError("Unexpected character: '" + c + "'");
        }

        while (!isAtEnd() && isValueLiteralChar(peek())) {
            c = advance();
            builder.append(c);
        }

        String literalStr = builder.toString();

        if (INTEGER_PATTERN.matcher(literalStr).matches()) {
            long value;
            try {
                value = Long.parseLong(literalStr);
            } catch (NumberFormatException e) {
                addError("Error parsing integer.");
                return null;
            }

            return makeToken(ValueType.INTEGER, value);
        } else if (INTEGER_RADIX_PATTERN.matcher(literalStr).matches()) {
            int offset = 2;
            boolean negative = false;
            if (literalStr.startsWith("-")) {
                offset = 3;
                negative = true;
            } else if (literalStr.startsWith("+")) {
                offset = 3;
            }

            int radix = switch (literalStr.charAt(offset - 1)) {
                case 'x' -> 16;
                case 'o' -> 8;
                case 'b' -> 2;
                default -> throw new IllegalStateException("Unexpected value: " + literalStr.charAt(offset - 1));
            };

            long value;
            try {
                value = Long.parseLong(literalStr.substring(offset), radix);
            } catch (NumberFormatException e) {
                addError("Error parsing integer.");
                return null;
            }

            return makeToken(ValueType.INTEGER, value);
        } else if (FLOAT_PATTERN.matcher(literalStr).matches()) {
            double value;
            try {
                value = Double.parseDouble(literalStr);
            } catch (NumberFormatException e) {
                addError("Error parsing float.");
                return null;
            }

            return makeToken(ValueType.FLOAT, value);
        } else if (literalStr.equalsIgnoreCase("true") || literalStr.equalsIgnoreCase("false")) {
            boolean value = Boolean.parseBoolean(literalStr);
            return makeToken(ValueType.BOOLEAN, value);
        } else if (ZONED_DATE_TIME_PATTERN.matcher(literalStr).matches()) {
            ZonedDateTime date;
            try {
                date = ZonedDateTime.parse(literalStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException e) {
                addError("Error parsing zoned date-time.");
                return null;
            }

            return makeToken(ValueType.ZONED_DATE_TIME, date);
        } else if (LOCAL_DATE_TIME_PATTERN.matcher(literalStr).matches()) {
            LocalDateTime date;
            try {
                date = LocalDateTime.parse(literalStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                addError("Error parsing local date-time.");
                return null;
            }

            return makeToken(ValueType.LOCAL_DATE_TIME, date);
        } else if (LOCAL_DATE_PATTERN.matcher(literalStr).matches()) {
            LocalDate date;
            try {
                date = LocalDate.parse(literalStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                addError("Error parsing date.");
                return null;
            }

            return makeToken(ValueType.LOCAL_DATE, date);
        } else if (LOCAL_TIME_PATTERN.matcher(literalStr).matches()) {
            LocalTime time;
            try {
                time = LocalTime.parse(literalStr, DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (DateTimeParseException e) {
                addError("Error parsing time.");
                return null;
            }

            return makeToken(ValueType.LOCAL_TIME, time);
        } else {
            addError("Unrecognized literal.");
            return null;
        }
    }

    private static boolean isValueLiteralChar(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9') || c == '_' || c == '-' ||
            c == '.' || c == ':';
    }

    private <T> Token<T> string(T type) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        while (true) {
            if (isAtEnd()) {
                addError("Unexpected end of string.");
                break;
            }

            char c = advance();
            if (c == '\n') {
                addError("Unexpected newline in single-line string.");
                break;
            } else if (c == '"') break;
            else if (c == '\\') {
                if (escapeSequence('"', stringBuilder)) break;
            } else {
                stringBuilder.append(c);
            }
        }

        return makeToken(type, stringBuilder.toString());
    }

    private <T> Token<T> literalString(T type) throws IOException {
        while (!isAtEnd() && peek() != '\'' && peek() != '\n') {
            advance();
        }

        if (isAtEnd() || peek() == '\n') {
            addError("Unexpected end of string.");
            return makeToken(type, buffer.substring(start + 1, current));
        } else {
            advance();
        }

        return makeToken(type, buffer.substring(start + 1, current - 1));
    }

    private Token<ValueType> multiLineString() throws IOException {
        if (peek() == '\n') advance();

        StringBuilder stringBuilder = new StringBuilder();

        while (true) {
            if (isAtEnd()) {
                addError("Unexpected end of string.");
                break;
            }

            char c = advance();
            if (c == '"' && peek() == '"' && peek(1) == '"') {
                // consume the end of the string
                advance();
                advance();
                break;
            } else if (c == '\\') {
                if (isStringWhitespace(peek())) {
                    while (isStringWhitespace(peek())) advance();
                } else if (escapeSequence('"', stringBuilder)) break;
            } else {
                stringBuilder.append(c);
            }
        }

        return makeToken(ValueType.STRING, stringBuilder.toString());
    }

    private static boolean isStringWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private Token<ValueType> multiLineLiteralString() throws IOException {
        if (peek() == '\n') advance();

        while (!isAtEnd() && !(peek() == '\'' && peek(1) == '\'' && peek(2) == '\'')) {
            advance();
        }

        if (isAtEnd()) {
            addError("Unexpected end of string.");
            return makeToken(ValueType.STRING, buffer.substring(start + 3, current));
        } else {
            advance();
            advance();
            advance();
        }

        return makeToken(ValueType.STRING, buffer.substring(start + 3, current - 3));
    }

    private boolean escapeSequence(char terminator, StringBuilder stringBuilder) throws IOException {
        if (isAtEnd()) {
            addError("Unexpected end of string in middle of escape sequence.");
            return true;
        }

        char next = advance();
        if (next == '\n') {
            addError("Unexpected newline in single-line string.");
            return true;
        }

        if (next == terminator) stringBuilder.append(terminator);
        else if (next == '\\') stringBuilder.append('\\');
        else if (next == 'b') stringBuilder.append('\b');
        else if (next == 't') stringBuilder.append('\t');
        else if (next == 'n') stringBuilder.append('\n');
        else if (next == 'f') stringBuilder.append('\f');
        else if (next == 'r') stringBuilder.append('\r');
        else if (next == 'u') {
            if (buffer.isAvailable(4)) {
                addError("Unexpected end of string in middle of escape sequence.");
                return true;
            }

            String codepointStr =
                new StringBuilder().append(advance()).append(advance()).append(advance()).append(advance())
                    .toString();
            int codepoint;
            try {
                codepoint = Integer.parseInt(codepointStr);
            } catch (NumberFormatException e) {
                addError("Escape sequence '" + codepointStr + "' is not a number.");
                return true;
            }

            stringBuilder.append(Character.toChars(codepoint));
        } else if (next == 'U') {
            if (buffer.isAvailable(8)) {
                addError("Unexpected end of string in middle of escape sequence.");
                return true;
            }

            String codepointStr =
                new StringBuilder().append(advance()).append(advance()).append(advance()).append(advance())
                    .append(advance()).append(advance()).append(advance()).append(advance()).toString();
            int codepoint;
            try {
                codepoint = Integer.parseInt(codepointStr);
            } catch (NumberFormatException e) {
                addError("Escape sequence '" + codepointStr + "' is not a number.");
                return true;
            }

            stringBuilder.append(Character.toChars(codepoint));
        } else {
            addError("Unknown escape sequence '\\" + next + "'");
        }
        return false;
    }

    private char advance() throws IOException {
        col++;
        char c = buffer.advanceOrThrow();
        current++;
        if (c == '\n') {
            col = 1;
            line++;
        }
        return c;
    }

    private boolean match(char expected) throws IOException {
        if (isAtEnd()) return false;
        if (buffer.peek(0) != expected) return false;

        advance();
        return true;
    }

    private boolean match(String expected) throws IOException {
        // check string matches
        int expectedLen = expected.length();
        if (!buffer.isAvailable(expectedLen)) return false;
        for (int i = 0; i < expectedLen; i++) {
            if (buffer.peek(i) != expected.charAt(i)) return false;
        }

        // if string does match, consume it
        for (int i = 0; i < expectedLen; i++) {
            advance();
        }

        return true;
    }

    private char peek() throws IOException {
        return buffer.peek(0, '\0');
    }

    private char peek(int offset) throws IOException {
        return buffer.peek(offset, '\0');
    }

    private <T> Token<T> makeToken(T type) throws IOException {
        return makeToken(type, null);
    }

    private <T> Token<T> makeToken(T type, Object literal) throws IOException {
        String lexeme = buffer.substring(start, buffer.getPos());
        return new Token<>(type, lexeme, literal, line, col, start, buffer.getPos() - start);
    }

    private void addError(String message) throws IOException {
        String lexeme = buffer.substring(start, buffer.getPos());
        errors.add(new LexError(line, col, start, buffer.getPos() - start, lexeme, message));
    }
}
