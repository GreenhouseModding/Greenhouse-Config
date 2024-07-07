package dev.greenhouseteam.greenhouseconfig.api.lang.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

/**
 * Utility class for buffering input read from a {@link Reader}.
 * <p>
 * This allows useful operations like looking behaind or looking ahead of the buffer pos.
 */
public class CharBuffer {
    private final Reader reader;
    private final int initialBufferSize;
    private final int maxBufferSize;

    private char[] buffer;
    // the position in the reader that the start of the buffer corresponds to
    private int start = 0;
    // the position in the reader that we are currently at
    private int pos = 0;
    // the current number of meaningful chars in the buffer
    private int len = 0;

    // the farthest position look-behind should *aways* have accessible
    // start <= mark must always be true
    private int mark = 0;

    /**
     * Creates a new buffer with the given reader.
     *
     * @param reader the reader to read from.
     */
    public CharBuffer(Reader reader) {
        this(reader, 64, 8192);
    }

    /**
     * Creates a new buffer with the given reader, initial size, and max size.
     *
     * @param reader            the reader to read from.
     * @param initialBufferSize the initial size of the buffer.
     * @param maxBufferSize     the maximum size of the buffer.
     */
    public CharBuffer(Reader reader, int initialBufferSize, int maxBufferSize) {
        this.reader = reader;
        this.initialBufferSize = initialBufferSize;
        this.maxBufferSize = maxBufferSize;

        buffer = new char[initialBufferSize];
    }

    /**
     * {@return the current position being read at}
     */
    public int getPos() {
        return pos;
    }

    /**
     * {@return the marked position before which nothing can be read}
     */
    public int getMark() {
        return mark;
    }

    /**
     * This marks the current position as farthest position that look-behind should *always* have accessible.
     * <p>
     * Anything prior to the mark may be discarded at any time.
     */
    public void mark() {
        mark = pos;
    }

    /**
     * Checks to see if the given number of chars are available ahead of the current reader position.
     * <p>
     * If the buffer does not have enough available chars, new chars will be added to the buffer.
     *
     * @param count the number of chars to look ahead and check for the existance of.
     * @return whether chars exist the given number of chars ahead.
     * @throws IOException if an error ocurrs while reading from the reader.
     */
    public boolean isAvailable(int count) throws IOException {
        int availPos = ensureRead(pos + count);
        return availPos >= pos + count;
    }

    /**
     * Gets the current char, or {@code -1} if the reader has reached its end, and advances the position to the next
     * char if possible.
     * <p>
     * If the buffer has no more chars available, this refills the buffer from the reader, if possible.
     *
     * @return the next char, or {@code -1} if none are available.
     * @throws IOException if an error ocurrs while reading from the reader.
     */
    public int advance() throws IOException {
        int availPos = ensureRead(pos + 1);
        if (availPos <= pos) return -1;

        return buffer[pos++ - start];
    }

    /**
     * Gets the current char, or {@code fallback} if the reader has reached its end, and advances the position to the
     * next char if possible.
     * <p>
     * If the buffer has no more chars available, this refills the buffer from the reader, if possible.
     *
     * @param fallback the fallback character to return if none are available.
     * @return the next char, or {@code fallback} if none are available.
     * @throws IOException if an error ocurrs while reading from the reader.
     */
    public char advance(char fallback) throws IOException {
        int ch = advance();
        if (ch == -1) return fallback;
        return (char) ch;
    }

    /**
     * Gets the current char and advances the position to the next char.
     * <p>
     * If the buffer has no more chars available, this refills the buffer from the reader.
     *
     * @return the next char.
     * @throws EOFException if the current char is beyond the end of the reader.
     * @throws IOException  if an error ocurrs while reading from the reader.
     */
    public char advanceOrThrow() throws IOException {
        int ch = advance();
        if (ch == -1) throw new EOFException("Attempted to advance beyond the end of the reader");
        return (char) ch;
    }

    /**
     * Peeks ahead the given number of chars, returning the requested char, or {@code -1} if the requested char
     * does not exist.
     * <p>
     * If the buffer does not have enough chars available, this refills the buffer from the reader, if possible.
     *
     * @param offset the number of chars to peek ahead, can be zero to peek at the current char, or negative to peek
     *               behind.
     * @return the char the given number of chars ahead, or {@code -1} if not available.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code offset} results in a position before the current mark.
     */
    public int peek(int offset) throws IOException {
        if (pos + offset < mark)
            throw new IllegalArgumentException("Attempted to peek at chars before the current mark");

        int availPos = ensureRead(pos + offset + 1);
        if (availPos <= pos + offset) return -1;

        return buffer[pos + offset];
    }

    /**
     * Peeks ahead the given number of chars, returning the requested char, or {@code fallback} if the requested char
     * does not exist.
     * <p>
     * If the buffer does not have enough chars available, this refills the buffer from the reader, if possible.
     *
     * @param offset   the number of chars to peek ahead, can be zero to peek at the current char, or negative to peek
     *                 behind.
     * @param fallback the char to return if the requested char is not available.
     * @return the char the given number of chars ahead, or {@code fallback} if not available.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code offset} results in a position before the current mark.
     */
    public char peek(int offset, char fallback) throws IOException {
        int ch = peek(offset);
        if (ch == -1) return fallback;

        return (char) ch;
    }

    /**
     * Peeks a head the given number of chars, returning the requested char.
     * <p>
     * If the buffer does not have enough chars available, this refills the buffer from the reader.
     *
     * @param offset the number of chars to peek ahead, can be zero to peek at the current char, or negative to peek
     *               behind.
     * @return the char the given number of chars ahead.
     * @throws EOFException             if the requested char is beyond the end of the reader.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code offset} results in a position before the current mark.
     */
    public char peekOrThrow(int offset) throws IOException {
        int ch = peek(offset);
        if (ch == -1) throw new EOFException("Attempted to peek beyond the end of the reader");

        return (char) ch;
    }

    /**
     * Gets the char at the requested pos if available, or {@code -1} if not available.
     * <p>
     * If the buffer does not contain the requested char, this refills or extends the buffer from the reader.
     *
     * @param pos the position to get the char at.
     * @return the char at the given position, or {@code -1} if not available.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code pos} is before the current mark.
     */
    public int charAt(int pos) throws IOException {
        if (pos < mark) throw new IllegalArgumentException("Attempted to get a char before the current mark");

        int avail = ensureRead(pos + 1);
        if (avail <= pos) return -1;

        return buffer[pos - start];
    }

    /**
     * Gets the char at the requested pos if available, or {@code fallback} if not available.
     * <p>
     * If the buffer does not contain the requested char, this refills or extends the buffer from the reader.
     *
     * @param pos      the position to get the char at.
     * @param fallback the char to return if the requested char is not available.
     * @return the char at the given position, or {@code fallback} if not available.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code pos} is before the current mark.
     */
    public char charAt(int pos, char fallback) throws IOException {
        int ch = charAt(pos);
        if (ch == -1) return fallback;

        return (char) ch;
    }

    /**
     * Gets the char at the requested pos.
     * <p>
     * If the buffer does not contain the requested char, this refills or extends the buffer from the reader.
     *
     * @param pos the position to get the char at.
     * @return the char at the given position.
     * @throws EOFException             if the requested char is beyond the end of the reader.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code pos} is before the current mark.
     */
    public char charAtOrThrow(int pos) throws IOException {
        int ch = charAt(pos);
        if (ch == -1) throw new EOFException("Attempted to get a char after the end of the reader");

        return (char) ch;
    }

    /**
     * Gets a string from this buffer between the given positions.
     * <p>
     * If the reader encounters an EOF before endPos, when the returned string will be shorter, only contining the
     * available chars. If the reader cannot read enough to reach beginPos, or cannot read anything at all, then an
     * empty string is returned.
     *
     * @param beginPos the reader index of the beginning of the string (inclusive).
     * @param endPos   the reader index of the end of the string (exclusive).
     * @return the string between the given positions.
     * @throws IOException              if an error ocurrs while reading from the reader.
     * @throws IllegalArgumentException if {@code beginPos} is after {@code endPos} or if {@code beginPos} is before
     *                                  the current mark.
     */
    public String substring(int beginPos, int endPos) throws IOException {
        if (beginPos > endPos) throw new IllegalArgumentException("Attempted to get a substring with negative length");
        if (beginPos < mark)
            throw new IllegalArgumentException("Attempted to get a substring starting before the current mark");

        int realEndPos = ensureRead(endPos);
        // not enough could be read to have anything
        if (realEndPos <= beginPos) return "";

        return String.valueOf(buffer, beginPos, Math.min(endPos, realEndPos) - beginPos);
    }

    /**
     * Attempts to make sure the buffer contains chars at least up to the current position.
     *
     * @param requiredPos the position that the buffer should contain chars up to (exclusive).
     * @return the actual position that the buffer has valid chars up to (exclusive).
     * @throws IOException if an error ocurrs while reading from the reader.
     */
    private int ensureRead(int requiredPos) throws IOException {
        ensureCapacity(requiredPos);

        if (start + len < requiredPos) {
            // we need to read more to have chars available
            int read = reader.read(buffer, len, buffer.length - len);

            // returning -1 means no more chars can be read, so the buffer does not change size
            if (read == -1) return start + len;

            len += read;
        }

        return start + len;
    }

    /**
     * Ensures the buffer has enough space to hold chars up to the given position.
     *
     * @param requiredPos the position that the buffer must be able to hold up until (exclusive).
     */
    private void ensureCapacity(int requiredPos) {
        if (requiredPos - start > buffer.length) {
            if (requiredPos - mark <= buffer.length) {
                // we can get enough capacity simply by getting rid of everything before the mark
                final int offset = mark - start;
                System.arraycopy(buffer, offset, buffer, 0, len - offset);
                len -= offset;
                start = mark;
            } else {
                // we need to grow the buffer anyways

                // check for misusage
                if (requiredPos - mark > maxBufferSize) throw new IllegalStateException(
                    "Trying to grow buffer too large! Maybe you forgot to call mark()? Otherwise, try specifying a larger maxBufferSize when this buffer is created.");

                final int requiredLength = requiredPos - mark;
                final int offset = mark - start;

                // find a new buffer length
                int newBufferLength = buffer.length;
                while (newBufferLength < requiredLength) {
                    newBufferLength *= 2;
                }

                // create the new buffer and copy over the needed data from the old buffer
                char[] newBuffer = new char[newBufferLength];
                System.arraycopy(buffer, offset, newBuffer, 0, len - offset);
                len -= offset;
                start = mark;
                buffer = newBuffer;
            }
        }
    }
}
