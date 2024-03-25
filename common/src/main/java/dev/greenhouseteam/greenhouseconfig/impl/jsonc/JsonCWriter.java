package dev.greenhouseteam.greenhouseconfig.impl.jsonc;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class JsonCWriter implements Closeable, Flushable {
    private final Writer writer;
    private int indentationAmount;
    private int currentId = 0;
    private final int[] objectCount = new int[32];
    @Nullable
    private List<String> comments;
    @Nullable
    private String key;

    public JsonCWriter(Writer writer) {
        this.writer = writer;
        Arrays.fill(objectCount, 0);
    }

    public String toString() {
        return writer.toString();
    }

    public void beginArray() throws IOException {
        writeSeparatorIfApplicable();
        writeComments();
        if (writeKey())
            this.writer.append("[");
        else{
            appendIndentation();
            this.writer.append("[");
        }
        this.writer.append("\n");
        ++objectCount[this.currentId];
        ++this.currentId;
        ++this.indentationAmount;
    }

    public void endArray() throws IOException {
        if (this.objectCount[this.currentId] > 0) {
            this.writer.append("\n");
        }
        this.indentationAmount -= 1;
        this.objectCount[this.currentId] = 0;
        --this.currentId;
        if (this.currentId > 0) {
            ++objectCount[this.currentId];
        }
        appendIndentation();
        this.writer.append("]");
    }

    public void beginObject() throws IOException {
        writeSeparatorIfApplicable();
        writeComments();
        if (writeKey())
            this.writer.append("{");
        else {
            appendIndentation();
            this.writer.append("{");
        }
        this.writer.append("\n");
        ++objectCount[this.currentId];
        ++this.currentId;
        ++this.indentationAmount;
    }

    public void endObject() throws IOException {
        if (this.objectCount[this.currentId] > 0) {
            this.writer.append("\n");
        }
        this.indentationAmount -= 1;
        this.objectCount[this.currentId] = 0;
        --this.currentId;
        if (this.currentId > 0) {
            ++objectCount[this.currentId];
        }
        appendIndentation();
        this.writer.append("}");
    }

    public void comments(List<String> comments) {
        this.comments = comments;
    }

    public void key(String key) {
        this.key = key;
    }

    public void writeValue(Number number) throws IOException {
        writeSeparatorIfApplicable();
        writeComments();
        if (writeKey())
            this.writer.append(number.toString());
        else{
            appendIndentation();
            appendNumberValue(number);
        }
        ++objectCount[this.currentId];
    }

    public void writeValue(boolean bool) throws IOException {
        this.writeValue(bool ? "true" : "false");
    }

    public void writeValue(String string) throws IOException {
        writeSeparatorIfApplicable();
        writeComments();
        if (writeKey())
            appendStringValue(string);
        else {
            appendIndentation();
            appendStringValue(string);
        }
        ++objectCount[this.currentId];
    }

    private void writeSeparatorIfApplicable() throws IOException {
        if (this.objectCount[this.currentId] > 0) {
            writer.append(",").append("\n");
        }
    }

    private void writeComments() throws IOException {
        if (comments == null) return;
        for (String comment : comments) {
            this.appendIndentation();
            this.writer.append("// ").append(comment).append("\n");
        }
        this.comments = null;
    }

    private boolean writeKey() throws IOException {
        if (key == null) {
            return false;
        }
        appendIndentation();
        this.writer.append("\"").append(key).append("\": ");
        this.key = null;
        return true;
    }

    private void appendIndentation() throws IOException {
        this.writer.append("  ".repeat(Math.max(0, indentationAmount)));
    }

    private void appendNumberValue(Number value) throws IOException {
        this.writer.append(value.toString());
        ++objectCount[this.currentId];
    }

    private void appendStringValue(String value) throws IOException {
        this.writer.append("\"").append(value).append("\"");
        ++objectCount[this.currentId];
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    @Override
    public void flush() throws IOException {
        this.writer.flush();
    }
}
