package dev.greenhouseteam.greenhouseconfig.api.lang.toml;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

/**
 * A base TOML tree element.
 */
public sealed interface TomlElement extends CommentedValue permits TomlList, TomlObject, TomlValue {

    /**
     * {@return the comments associated with this element}
     */
    String[] getComment();

    /**
     * {@return this element's value object}
     */
    Object getValue();
}
