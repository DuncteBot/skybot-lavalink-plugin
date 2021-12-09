package com.dunctebot.lavalinkplugin;

import lavalink.api.PluginInfo;

public class Plugin implements PluginInfo {
    @Override
    public int getMajor() {
        return 0xFF1;
    }

    @Override
    public int getMinor() {
        return 0xFF2;
    }

    @Override
    public int getPatch() {
        return 0xFF3;
    }

    @Override
    public String getName() {
        return "DuncteBot-lavalink";
    }
}
