package com.dunctebot.sourcemanagers.pixeldrain;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PixeldrainAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final String PIXELDRAIN_LOOKUP_BASE = "https://pixeldrain.com/u/";

    /* package */ static final String AUDIO_TEMPLATE = "https://pixeldrain.com/api/file/%s";
    private static final String THUMBNAIL_TEMPLATE = AUDIO_TEMPLATE + "/thumbnail";

    @Override
    public String getSourceName() {
        return "pixeldrain";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final var id = reference.getIdentifier();

        if (id.startsWith(PIXELDRAIN_LOOKUP_BASE)) {
            final var parts = id.split(PIXELDRAIN_LOOKUP_BASE);

            if (parts.length < 2) {
                return null;
            }

            final var identifier = parts[1];

            try {
                final var trackInfo = downloadInfo(identifier);

                return decodeTrack(trackInfo, null);
            } catch (IOException e) {
                throw new FriendlyException(
                        "Could not download pixeldrain track info",
                        FriendlyException.Severity.SUSPICIOUS,
                        e
                );
            }
        }

        return null;
    }

    private AudioTrackInfo downloadInfo(String identifier) throws IOException {
        final var document = loadHtml(identifier);

        if (document == null) {
            notAvailable();
        }

//        System.out.println(document.select("meta[property]"));

        final var title = document.selectFirst("meta[property='og:title']").attr("content");
        final var type = document.selectFirst("meta[property='og:type']").attr("content");

        if (!"music.song".equals(type)) {
            throw new FriendlyException("Type " + type + " is currently not supported", FriendlyException.Severity.COMMON, null);
        }

        return new AudioTrackInfo(
                title,
                "Unknown artist",
                Units.CONTENT_LENGTH_UNKNOWN,
                identifier,
                false,
                PIXELDRAIN_LOOKUP_BASE + identifier,
                String.format(THUMBNAIL_TEMPLATE, identifier),
                null
        );
    }

    private Document loadHtml(String identifier) throws IOException {
        final var httpGet = new HttpGet(PIXELDRAIN_LOOKUP_BASE + identifier);

        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null;
                }

                throw new IOException("Unexpected status code for pixeldrain page response: " + statusCode);
            }

            final var html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            return Jsoup.parse(html);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // Nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new PixelDrainAudioTrack(trackInfo, this);
    }

    private void notAvailable() {
        throw new FriendlyException("This item is not available", FriendlyException.Severity.COMMON, null);
    }
}
