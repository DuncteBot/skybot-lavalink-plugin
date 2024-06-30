package com.dunctebot.lavalinkplugin;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public record SourceManagerInfo(BooleanSupplier configSupplier, Supplier<AudioSourceManager> supplier) {
}
