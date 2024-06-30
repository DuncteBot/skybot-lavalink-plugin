package com.dunctebot.sourcemanagers.elgato.streamdeck;

import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider;

import java.io.IOException;
import java.util.List;

// TODO: can I inject a custom probe into LP?
// See: MediaContainerRegistry
public class ElgatoInputStream extends SeekableInputStream {
    private static final byte XOR_VAL = 0x5E;

    private final SeekableInputStream inputStream;

    public ElgatoInputStream(SeekableInputStream inputStream) {
        super(inputStream.getContentLength(), inputStream.getMaxSkipDistance());
        this.inputStream = inputStream;
    }

    @Override
    public long getPosition() {
        return this.inputStream.getPosition();
    }

    // TODO: Will this work?
    @Override
    protected void seekHard(long position) throws IOException {
        ((ElgatoInputStream) this.inputStream).seekHard(position);
    }

    @Override
    public boolean canSeekHard() {
        return this.inputStream.canSeekHard();
    }

    @Override
    public List<AudioTrackInfoProvider> getTrackInfoProviders() {
        return this.inputStream.getTrackInfoProviders();
    }

    @Override
    public int read() throws IOException {
        final var read = this.inputStream.read();

        if (read == -1) {
            return -1;
        }

        return read ^ XOR_VAL;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.inputStream.close();
    }
}
