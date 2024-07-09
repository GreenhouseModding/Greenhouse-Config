package dev.greenhouseteam.greenhouseconfig.impl.lang.toml;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlArray;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlBoolean;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlElement;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlFloat;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlInteger;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlLocalDate;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlLocalDateTime;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlLocalTime;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlOffsetDateTime;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlString;
import dev.greenhouseteam.greenhouseconfig.api.lang.toml.TomlTable;
import dev.greenhouseteam.greenhouseconfig.api.lang.util.Token;
import dev.greenhouseteam.greenhouseconfig.api.lang.util.TokenBuffer;

/**
 * Parses a string into a TOML value tree.
 */
public class TomlParser {
    private final TomlLexer lexer;
    private final TokenBuffer<KeyType> keyBuf;
    private final TokenBuffer<ValueType> valBuf;

    /**
     * Creates a new TOML parser.
     *
     * @param reader the reader to read TOML source code from.
     */
    public TomlParser(Reader reader) {
        lexer = new TomlLexer(reader);
        keyBuf = new TokenBuffer<>(lexer::nextKeyToken);
        valBuf = new TokenBuffer<>(lexer::nextValueToken);
    }

    /**
     * Consumes the reader and parser to produce a parsed {@link TomlFileDocument}.
     *
     * @return the parsed {@link TomlFileDocument}.
     * @throws IOException if an IO error ocurrs while reading the toml file.
     */
    public TomlFileDocument parse() throws IOException {
        return document();
    }

    private TomlFileDocument document() throws IOException {
        List<TomlFileElement> elements = new ArrayList<>();

        TomlFileElement element;
        while ((element = element()) != null) elements.add(element);

        return new TomlFileDocument(elements);
    }

    private TomlFileElement element() throws IOException {
        List<String> comments = new ArrayList<>();

        while (keyBuf.peekType(0) == KeyType.COMMENT) {
            var comment = keyBuf.advance();
            comments.add(String.valueOf(comment.literal()));
        }

        return switch (keyBuf.peekType(0)) {
            case LEFT_BRACKET -> tableHeaderElem(comments.toArray(String[]::new));
            case IDENT, QUOTED_IDENT -> valueElem(comments.toArray(String[]::new));
            case null -> null;
            default -> {
                Token<KeyType> peek = keyBuf.peek(0);
                throw new IOException(
                    "Unexpected token type " + peek.type() + " (" + peek.lexeme() + ") on line " + peek.line() +
                        " column " + peek.col() +
                        ". Expected one of: [LEFT_BRACKET, IDENT, QUOTED_IDENT, COMMENT, EOF]");
            }
        };
    }

    private TomlFileTableHeader tableHeaderElem(String[] comments) throws IOException {
        // consume the bracket token
        Token<KeyType> startingBrace = keyBuf.advance();

        boolean array = false;
        if (keyBuf.peekType(0) == KeyType.LEFT_BRACKET) {
            array = true;
            keyBuf.advance();
        }

        String[] path = path();
        if (path.length == 0)
            throw new IOException("Encountered table header with empty path on line: " + startingBrace.line());

        keyBuf.expect(KeyType.RIGHT_BRACKET);
        if (array) keyBuf.expect(KeyType.RIGHT_BRACKET);

        return new TomlFileTableHeader(path, array, comments);
    }

    private TomlFileValue valueElem(String[] comments) throws IOException {
        String[] path = path();

        keyBuf.expect(KeyType.EQUALS);

        TomlElement value = value(false);

        return new TomlFileValue(path, value, comments);
    }

    private String[] path() throws IOException {
        List<String> elements = new ArrayList<>();
        while (true) {
            KeyType type = keyBuf.peekType(0);
            if (type != KeyType.IDENT && type != KeyType.QUOTED_IDENT && type != KeyType.DOT)
                return elements.toArray(String[]::new);

            Token<KeyType> token = keyBuf.advance();
            // should never be null
            if (token.type() != KeyType.DOT) {
                elements.add(Objects.requireNonNull(token.literal(), "Encountered token with null literal: " + token)
                    .toString());
            }
        }
    }

    private TomlElement value(boolean inline) throws IOException {
        return switch (valBuf.peekType(0)) {
            case LEFT_BRACKET -> arrayValue(inline);
            case LEFT_BRACE -> tableValue(inline);
            case STRING -> stringValue(inline);
            case INTEGER -> integerValue(inline);
            case FLOAT -> floatValue(inline);
            case BOOLEAN -> booleanValue(inline);
            case ZONED_DATE_TIME -> offsetDateTimeValue(inline);
            case LOCAL_DATE_TIME -> localDateTimeValue(inline);
            case LOCAL_DATE -> localDateValue(inline);
            case LOCAL_TIME -> localTimeValue(inline);
            default -> {
                Token<ValueType> token = valBuf.peek(0);
                if (token == null) throw new EOFException(
                    "Unexpected end of file. Expected one of: [LEFT_BRACKET, LEFT_BRACE, STRING, INTEGER, FLOAT, BOOLEAN, ZONED_DATE_TIME, LOCAL_DATE_TIME, LOCAL_DATE, LOCAL_TIME]");
                throw new IOException(
                    "Unexpected token type: " + token.type() + " (" + token.lexeme() + ") on line " + token.line() +
                        " column " + token.col() +
                        ". Expected one of: [LEFT_BRACKET, LEFT_BRACE, STRING, INTEGER, FLOAT, BOOLEAN, ZONED_DATE_TIME, LOCAL_DATE_TIME, LOCAL_DATE, LOCAL_TIME]");
            }
        };
    }

    private TomlArray arrayValue(boolean inline) throws IOException {
        valBuf.advance();
        List<TomlElement> elements = new ArrayList<>();

        String[] firstLineComents = valueComment();

        // vacuum up newlines inside arrays
        while (valBuf.peekType(0) == ValueType.NEW_LINE) valBuf.advance();

        String[] nextComments = valueMultiComment();

        while (switch (valBuf.peekType(0)) {
            case LEFT_BRACKET, LEFT_BRACE, STRING, INTEGER, FLOAT, BOOLEAN, ZONED_DATE_TIME, LOCAL_DATE_TIME,
                 LOCAL_DATE, LOCAL_TIME, COMMENT -> true;
            default -> false;
        }) {
            TomlElement value = value(true);

            elements.add(value.withComment(concat(nextComments, value.getComment())));

            // consume the comma if it exists
            if (valBuf.peekType(0) == ValueType.COMMA) valBuf.advance();

            // vacuum up newlines inside arrays
            while (valBuf.peekType(0) == ValueType.NEW_LINE) valBuf.advance();

            // i guess a comma could technically exist here???
            if (valBuf.peekType(0) == ValueType.COMMA) valBuf.advance();

            nextComments = valueMultiComment();

            // i guess a comma could technically be here too?????
            if (valBuf.peekType(0) == ValueType.COMMA) valBuf.advance();
        }

        valBuf.expect(ValueType.RIGHT_BRACKET);

        String[] finalComment = valueComment();

        if (!inline) expectValueEnd();

        String[] fullComments = concat(concat(firstLineComents, nextComments), finalComment);

        return new TomlArray(elements, fullComments);
    }

    private TomlTable tableValue(boolean inline) throws IOException {
        Token<ValueType> advance = valBuf.advance();
        Map<String, TomlElement> tableElements = new LinkedHashMap<>();

        if (valBuf.peekType(0) == ValueType.RAW_LITERAL) {
            do {
                // value tokens parse '.' chars as part of a single string instead of as their own token
                // (helps with number parsing)
                String[] path = valuePath();

                valBuf.expect(ValueType.EQUALS);

                TomlElement elem = value(true);

                insert(elem, tableElements, path, 0);
            } while (valBuf.expect(ValueType.COMMA, ValueType.RIGHT_BRACE).type() == ValueType.COMMA);
        }

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlTable(tableElements, new String[0]);
    }

    private void insert(TomlElement elem, Map<String, TomlElement> root, String[] path, int pathIndex)
        throws IOException {
        String key = path[pathIndex];

        if (path.length > pathIndex + 1) {
            TomlElement child = root.computeIfAbsent(key, k -> new TomlTable(new LinkedHashMap<>(), new String[0]));
            if (!(child instanceof TomlTable childTable))
                throw new IOException("Tried to define child of " + key + " but it was already defined as " + child);

            insert(elem, childTable.map(), path, pathIndex + 1);
        } else {
            root.put(key, elem);
        }
    }

    private TomlString stringValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof String str))
            throw new IllegalStateException("Encountered STRING token with non-string literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlString(str, comments);
    }

    private TomlInteger integerValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof Long l))
            throw new IllegalStateException("Encountered INTEGER token with non-long literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlInteger(l, comments);
    }

    private TomlFloat floatValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof Double d))
            throw new IllegalStateException("Encountered FLOAT token with non-double literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlFloat(d, comments);
    }

    private TomlBoolean booleanValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof Boolean b))
            throw new IllegalStateException("Encountered BOOLEAN token with non-boolean literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlBoolean(b, comments);
    }

    private TomlOffsetDateTime offsetDateTimeValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof ZonedDateTime dateTime))
            throw new IllegalStateException("Encountered ZONED_DATE_TIME token with non-ZonedDateTime literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlOffsetDateTime(dateTime, comments);
    }

    private TomlLocalDateTime localDateTimeValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof LocalDateTime dateTime))
            throw new IllegalStateException("Encountered LOCAL_DATE_TIME token with non-LocalDateTime literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlLocalDateTime(dateTime, comments);
    }

    private TomlLocalDate localDateValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof LocalDate dateTime))
            throw new IllegalStateException("Encountered LOCAL_DATE token with non-LocalDate literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlLocalDate(dateTime, comments);
    }

    private TomlLocalTime localTimeValue(boolean inline) throws IOException {
        Token<ValueType> token = valBuf.advance();
        if (!(token.literal() instanceof LocalTime dateTime))
            throw new IllegalStateException("Encountered LOCAL_TIME token with non-LocalTime literal");

        String[] comments = valueComment();

        if (!inline) expectValueEnd();

        return new TomlLocalTime(dateTime, comments);
    }

    private String[] valueComment() throws IOException {
        if (valBuf.peekType(0) == ValueType.COMMENT) {
            Token<ValueType> commentToken = valBuf.advance();
            return new String[]{String.valueOf(commentToken.literal())};
        }
        return new String[0];
    }

    private String[] valueMultiComment() throws IOException {
        List<String> comments = new ArrayList<>();
        while (valBuf.peekType(0) == ValueType.COMMENT) {
            var comment = valBuf.advance();
            comments.add(String.valueOf(comment.literal()));

            // vacuum up newlines inside multi-line value comments
            while (valBuf.peekType(0) == ValueType.NEW_LINE) valBuf.advance();
        }

        return comments.toArray(String[]::new);
    }

    private String[] valuePath() throws IOException {
        Token<ValueType> string = valBuf.expect(ValueType.RAW_LITERAL);
        if (!(string.literal() instanceof String str))
            throw new IllegalStateException("Encountered RAW_LITERAL token with non-string literal");

        return str.split("\\.");
    }

    private void expectValueEnd() throws IOException {
        Token<ValueType> token = valBuf.advance();

        if (token != null && token.type() != ValueType.NEW_LINE) throw new IOException(
            "Unexpected token type: " + token.type() + " (" + token.lexeme() + ") on line " + token.line() +
                " column " + token.col() + ". Expected one of: [NEW_LINE, EOF]");
    }

    private <T> T[] concat(T[] a, T[] b) {
        if (a.length == 0) return b;
        if (b.length == 0) return a;

        T[] c = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }
}
