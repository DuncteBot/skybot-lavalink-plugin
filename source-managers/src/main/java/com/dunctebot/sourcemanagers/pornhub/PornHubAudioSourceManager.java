/*
 * Copyright 2021 Duncan "duncte123" Sterken
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

package com.dunctebot.sourcemanagers.pornhub;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
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

public class PornHubAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final String DOMAIN_PATTERN = "https?://([a-z]+\\.)?pornhub\\.(com|net|org)";
    public static final Pattern DOMAIN_REGEX = Pattern.compile(DOMAIN_PATTERN);
    private static final Pattern VIDEO_REGEX = Pattern.compile("^" + DOMAIN_PATTERN + "/view_video\\.php\\?viewkey=([a-zA-Z0-9]+)(?:.*)$");
    public static final Pattern VIDEO_INFO_REGEX = Pattern.compile("var flashvars_\\d+ = (\\{.+})");
    private static final Pattern MODEL_INFO_REGEX = Pattern.compile("var MODEL_PROFILE = (\\{.+})");

    @Override
    public String getSourceName() {
        return "pornhub";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!VIDEO_REGEX.matcher(reference.identifier).matches()) {
            return null;
        }

        try {
            return loadItemOnce(reference);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong", Severity.SUSPICIOUS, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // Nothing
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new PornHubAudioTrack(trackInfo, this);
    }

    private AudioItem loadItemOnce(AudioReference reference) throws IOException {
        final String html = loadHtml(reference.identifier);

        if (html == null) {
            notAvailable();
        }

        final JsonBrowser videoInfo = getVideoInfo(html);
        final JsonBrowser modelInfo = getModelInfo(html);

        if (videoInfo == null || modelInfo == null) {
            notAvailable();
        }

        if ("true".equals(videoInfo.get("video_unavailable").safeText())) {
            notAvailable();
        }

        final String title = videoInfo.get("video_title").safeText();
        final String author = modelInfo.get("username").safeText();
        final int duration = Integer.parseInt(videoInfo.get("video_duration").safeText()) * 1000; // PornHub returns seconds
        final Matcher matcher = VIDEO_REGEX.matcher(reference.identifier);
        final String identifier = matcher.matches() ? matcher.group(matcher.groupCount()) : null;
        final String uri = reference.identifier;
        final String imageUrl = videoInfo.get("image_url").safeText();

        return buildAudioTrack(
            title,
            author,
            duration,
            identifier,
            uri,
            imageUrl
        );
    }

    private AudioTrackInfo buildInfo(String title, String author, long duration, String identifier, String uri, String imageUrl) {
        return new AudioTrackInfo(
            title,
            author,
            duration,
            identifier,
            false,
            uri,
            imageUrl,
            null
        );
    }

    private PornHubAudioTrack buildAudioTrack(String title, String author, long duration, String identifier, String uri, String imageUrl) {
        return new PornHubAudioTrack(
            buildInfo(
                title,
                author,
                duration,
                identifier,
                uri,
                imageUrl
            ),
            this
        );
    }

    private JsonBrowser getVideoInfo(String html) throws IOException {
        final Matcher matcher = VIDEO_INFO_REGEX.matcher(html);

        if (matcher.find()) {
            return JsonBrowser.parse(matcher.group(1));
        }

        return null;
    }

    private JsonBrowser getModelInfo(String html) throws IOException {
        final Matcher matcher = MODEL_INFO_REGEX.matcher(html);

        if (matcher.find()) {
            return JsonBrowser.parse(matcher.group(1));
        }

        return null;
    }

    private String loadHtml(String url) throws IOException {
        final HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Cookie", "platform=pc; age_verified=1");

        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null;
                }

                throw new IOException("Unexpected status code for video page response: " + statusCode);
            }

            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        }
    }

    private void notAvailable() {
        throw new FriendlyException("This video is not available", Severity.COMMON, null);
    }

    public static String getPlayerPage(String id) {
        return "https://www.pornhub.com/view_video.php?viewkey=" + id;
    }
}
