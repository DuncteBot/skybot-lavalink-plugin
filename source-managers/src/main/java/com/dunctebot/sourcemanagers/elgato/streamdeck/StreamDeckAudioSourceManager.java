package com.dunctebot.sourcemanagers.elgato.streamdeck;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

// TODO: http vs local file
public class StreamDeckAudioSourceManager extends AbstractDuncteBotHttpSource {
    @Override
    public String getSourceName() {
        return "StreamDeckAudio";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            final var url = new URI(reference.getIdentifier());

            if (url.getPath().toLowerCase(Locale.ROOT).endsWith(".streamdeckaudio")) {
                final var parts = List.of(url.getPath().split("/"));
                final var fileName = parts.get(parts.size() - 1);

                return new StreamDeckAudioTrack(
                        new AudioTrackInfo(
                                fileName,
                                "Elgato",
                                Units.CONTENT_LENGTH_UNKNOWN,
                                fileName,
                                false,
                                url.toString()
                        ),
                        this
                );
            }
        } catch (URISyntaxException ignored) {
            return null;
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // Nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new StreamDeckAudioTrack(trackInfo, this);
    }
}
