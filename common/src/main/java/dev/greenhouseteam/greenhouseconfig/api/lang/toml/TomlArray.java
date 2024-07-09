package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.util.Arrays;
import java.util.List;

/**
 * A TOML list, holding multiple TOML elements.
 *
 * @param list     the list of TOML elements.
 * @param comments the comments associated with this list.
 */
public record TomlArray(List<TomlElement> list, String[] comments) implements TomlElement {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public List<TomlElement> getValue() {
        return list;
    }

    @Override
    public TomlArray withComment(String[] comments) {
        return new TomlArray(list, comments);
    }

    @Override
    public String toString() {
        return "TomlArray{" + list + ", " + Arrays.toString(comments) + "}";
    }
}
