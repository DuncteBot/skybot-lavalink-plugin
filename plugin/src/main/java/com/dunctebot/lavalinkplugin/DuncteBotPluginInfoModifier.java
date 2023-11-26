package com.dunctebot.lavalinkplugin;

import com.dunctebot.sourcemanagers.IWillUseIdentifierInstead;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DuncteBotPluginInfoModifier implements AudioPluginInfoModifier {
    @Nullable
    @Override
    public JsonObject modifyAudioTrackPluginInfo(@NotNull AudioTrack track) {
        final String uri;

        if (track instanceof IWillUseIdentifierInstead) {
            uri = track.getInfo().identifier;
        } else {
            uri = track.getInfo().uri;
        }

        return new JsonObject(Map.of(
                "save_uri", JsonElementKt.JsonPrimitive(uri)
        ));
    }
}
