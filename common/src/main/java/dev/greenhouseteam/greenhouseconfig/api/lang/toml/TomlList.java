package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import java.util.List;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A TOML list, holding multiple TOML elements.
 *
 * @param list     the list of TOML elements.
 * @param comments the comments associated with this list.
 */
public record TomlList(List<TomlElement> list, String[] comments) implements TomlElement {
    @Override
    public String[] getComment() {
        return comments;
    }

    @Override
    public List<TomlElement> getValue() {
        return list;
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new TomlList(list, comments);
    }
}
