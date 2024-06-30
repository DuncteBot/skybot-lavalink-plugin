package com.dunctebot.sourcemanagers.pixeldrain;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.Mp3Track;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

import java.io.IOException;

public class PixelDrainAudioTrack extends Mp3Track {
    public PixelDrainAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    private String getAudioType() {
        try {
            final var document = getSourceManager().loadHtml(this.trackInfo.identifier);
            final var title = document.selectFirst("meta[property='og:title']").attr("content");

            if (title.endsWith(".mp3")) {
                return "audio/x-mp3";
            }

            return document.selectFirst("meta[property='og:audio:type']").attr("content");
        } catch (IOException e) {
            throw new FriendlyException("Failed to extract html data", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    @Override
    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        final var audioType = getAudioType();

        switch (audioType) {
            case "audio/x-mp3":
                return new Mp3AudioTrack(trackInfo, stream);
            case "audio/mpeg":
                return new MpegAudioTrack(trackInfo, stream);
            case "audio/x-m4a":
                notSupported();
            case "audio/x-wavpack":
                notSupported();
            default:
                return new WavAudioTrack(trackInfo, stream);
        }
    }

    private void notSupported() {
        throw new FriendlyException("Audio format is not supported", FriendlyException.Severity.FAULT, null);
    }

    @Override
    public String getPlaybackUrl() {
        return String.format(PixeldrainAudioSourceManager.AUDIO_TEMPLATE, this.trackInfo.identifier);
    }

    @Override
    public PixeldrainAudioSourceManager getSourceManager() {
        return (PixeldrainAudioSourceManager) super.getSourceManager();
    }
}
