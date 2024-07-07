package dev.greenhouseteam.greenhouseconfig.test.lang;

import java.io.IOException;
import java.io.StringReader;

import dev.greenhouseteam.greenhouseconfig.api.lang.util.CharBuffer;

public class CharBufferTests {
    public static void main(String[] args) throws IOException {
        simpleTests();

        System.out.println("Done.");
    }

    private static void simpleTests() throws IOException {
        CharBuffer buf = new CharBuffer(new StringReader("h"));
        assertTrue(buf.isAvailable(1), "Buffer should have 1 available char");
        assertTrue(buf.peek(0) == 'h', "Buffer peek should return 'h'");
        assertTrue(buf.advance() == 'h', "Buffer advance should return 'h'");
        assertTrue(buf.charAt(0) == 'h', "Buffer char at 0 should be 'h'");
    }

    private static void assertTrue(boolean check, String msg) {
        if (!check) throw new AssertionError(msg);
    }
}
