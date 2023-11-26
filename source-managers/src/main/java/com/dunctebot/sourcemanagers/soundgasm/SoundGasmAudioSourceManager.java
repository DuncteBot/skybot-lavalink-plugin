/*
 * Copyright 2022 Duncan "duncte123" Sterken
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dunctebot.sourcemanagers.soundgasm;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundGasmAudioSourceManager extends AbstractDuncteBotHttpSource  {
    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/soundgasm\\.net\\/u\\/(?<path>(?<author>[^\\/]+)\\/[^\\/]+)");
    private static final Pattern SOUND_PATTERN = Pattern.compile("m4a:(?:\\s+)?[\"']https:\\/\\/media\\.soundgasm\\.net\\/sounds\\/([^.]+)\\.m4a[\"']");
    private static final Pattern TITLE_PATTERN = Pattern.compile("<div class=\"jp-title\" aria-label=\"title\">([^<]+)<\\/div>");

    @Override
    public String getSourceName() {
        return "soundgasm";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final String url = reference.identifier;
        final Matcher urlMatcher = URL_PATTERN.matcher(url);

        if (!urlMatcher.matches()) {
            return null;
        }

        final String fetchUrl = "https://soundgasm.net/u/" + urlMatcher.group("path");

        while (true) {
            try {
                return loadItemOnce(fetchUrl, urlMatcher);
            } catch (Exception e) {
                if (!HttpClientTools.isRetriableNetworkException(e)) {
                    throw ExceptionTools.wrapUnfriendlyExceptions(
                        "Loading of soundgasm track went wrong",
                        FriendlyException.Severity.FAULT,
                        e
                    );
                }
            }
        }
    }

    private AudioItem loadItemOnce(String fetchUrl, Matcher urlMatcher) throws IOException {
        final HttpGet httpGet = new HttpGet(fetchUrl);

        try (final CloseableHttpResponse res = getHttpInterface().execute(httpGet)) {
            final int statusCode = res.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return AudioReference.NO_TRACK;
                }

                throw new IOException("Invalid status code for soundgasm track page response: " + statusCode);
            }

            final String content = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
            final Matcher soundPatternMatcher = SOUND_PATTERN.matcher(content);

            if (!soundPatternMatcher.find()) {
                throw new FriendlyException("Failed to extract audio file", FriendlyException.Severity.FAULT, null);
            }

            final Matcher titleMatcher = TITLE_PATTERN.matcher(content);
            final String title;

            if (titleMatcher.find()) {
                title = titleMatcher.group(titleMatcher.groupCount());
            } else {
                title = "Unknown title";
            }

            final String identifier = soundPatternMatcher.group(soundPatternMatcher.groupCount());

            return new SoundGasmAudioTrack(
                new AudioTrackInfo(
                    title,
                    urlMatcher.group("author"),
                    Units.DURATION_MS_UNKNOWN,
                    identifier,
                    false,
                    fetchUrl
                ),
                this
            );
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SoundGasmAudioTrack(trackInfo, this);
    }
}
