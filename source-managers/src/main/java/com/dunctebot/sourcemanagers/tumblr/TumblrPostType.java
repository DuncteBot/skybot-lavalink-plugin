package com.dunctebot.sourcemanagers.tumblr;

import java.util.Locale;

public enum TumblrPostType {
    AUDIO,
    VIDEO,
    UNKNOWN;

    public static TumblrPostType fromData(String dataName) {
        for (TumblrPostType value : values()) {
            if (value.name().equals(dataName.toUpperCase(Locale.ROOT))) {
                return value;
            }
        }


        return UNKNOWN;
    }
}
