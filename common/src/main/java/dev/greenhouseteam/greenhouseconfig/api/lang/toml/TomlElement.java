package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A base TOML tree element.
 */
public sealed interface TomlElement extends CommentedValue permits TomlArray, TomlTable, TomlValue {

    /**
     * {@return the comments associated with this element}
     */
    String[] getComment();

    /**
     * {@return this element's value object}
     */
    Object getValue();

    /**
     * Makes a copy of this commented value with the given comment.
     *
     * @param comments the comments the resulting value should have attached.
     * @return the new value with comments attached.
     */
    @Override
    TomlElement withComment(String[] comments);
}
