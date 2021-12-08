package com.dunctebot.lavalinkplugin;

import lavalink.api.PluginInfo;

public class Plugin implements PluginInfo {
    @Override
    public int getMajor() {
        return 0;
    }

    @Override
    public int getMinor() {
        return 0;
    }

    @Override
    public int getPatch() {
        return 1;
    }

    @Override
    public String getName() {
        return "DuncteBot-lavalink";
    }
}
