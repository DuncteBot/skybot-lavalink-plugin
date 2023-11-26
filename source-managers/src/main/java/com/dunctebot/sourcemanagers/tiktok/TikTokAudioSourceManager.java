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

package com.dunctebot.sourcemanagers.tiktok;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
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

import static com.dunctebot.sourcemanagers.Utils.fakeChrome;

public class TikTokAudioSourceManager extends AbstractDuncteBotHttpSource {
    private final TikTokAudioTrackHttpManager httpManager = new TikTokAudioTrackHttpManager();
    private static final String BASE = "https:\\/\\/(?:www\\.|m\\.)?tiktok\\.com";
    private static final String USER = "@(?<user>[^/]+)";
    private static final String VIDEO = "(?<video>[0-9]+)";
    protected static final Pattern VIDEO_REGEX = Pattern.compile("^" + BASE + "\\/" + USER + "\\/video\\/" + VIDEO + "(?:.*)$");
    private static final Pattern JS_REGEX = Pattern.compile(
        "<script id=\"SIGI_STATE\" type=\"application/json\">([^<]+)<\\/script>");
    private static final Pattern SIGI_REGEX = Pattern.compile(
        "<script id=\"sigi-persisted-data\">(?:\n)?window\\[(?:'SIGI_STATE'|\"SIGI_STATE\")\\](?:\\s+)?=(?:\\s+)?(.*);(?:\\s+)?(?:.*)?<\\/script>");

    public TikTokAudioSourceManager() {
        super(false);
    }

    @Override
    public String getSourceName() {
        return "tiktok";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final Matcher matcher = VIDEO_REGEX.matcher(reference.identifier);

        if (!matcher.matches()) {
            return null;
        }

        final String user = matcher.group("user");
        final String video = matcher.group("video");

        try {
            final MetaData metaData = extractData(user, video);

            return new TikTokAudioTrack(metaData.toTrackInfo(), this);
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
        // Nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new TikTokAudioTrack(trackInfo, this);
    }

    MetaData extractData(String userId, String videoId) throws Exception {
        System.out.println("userId: " + userId + ", videoId: " + videoId);
        return extractData("https://www.tiktok.com/@" + userId + "/video/" + videoId);
    }

    @Override
    public HttpInterface getHttpInterface() {
        return httpManager.getHttpInterface();
    }

    protected MetaData extractData(String url) throws Exception {
        final HttpGet httpGet = new HttpGet(url);

        fakeChrome(httpGet);

        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                if (statusCode == 302) { // most likely a 404
                    return null;
                }

                throw new IOException("Unexpected status code for video page response: " + statusCode);
            }

            final String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final Matcher matcher = JS_REGEX.matcher(html);

            if (matcher.find()) {
                final JsonBrowser json = JsonBrowser.parse(matcher.group(1).trim());
                final String videoId = json.get("ItemList").get("video").get("list").index(0).text();
                final JsonBrowser base = json.get("ItemModule").get(videoId);

                return getMetaData(url, base);
            }

            final Matcher sigiMatcher = SIGI_REGEX.matcher(html);

            if (sigiMatcher.find()) {
                final JsonBrowser json = JsonBrowser.parse(sigiMatcher.group(1).trim());
                final String videoId = json.get("ItemList").get("video").get("keyword").text();
                final JsonBrowser video = json.get("ItemModule").get(videoId);

                return getMetaData(url, video);
            }

            // TODO: temp
            System.out.println(html);
            throw new FriendlyException("Failed to find data for tiktok video", Severity.SUSPICIOUS, null);
        }
    }

    protected static MetaData getMetaData(String url, JsonBrowser base) {
        final MetaData metaData = new MetaData();
        final JsonBrowser videoJson = base.get("video");

        metaData.pageUrl = url;
        metaData.videoId = base.get("id").safeText();
        metaData.videoUrl = videoJson.get("downloadAddr").text();
        metaData.cover = videoJson.get("cover").safeText();
        metaData.title = base.get("desc").safeText();

//        metaData.uri = videoJson.get("downloadAddr").safeText();
        metaData.uri = videoJson.get("playAddr").safeText();
        metaData.duration = Integer.parseInt(videoJson.get("duration").safeText());

        metaData.musicUrl = base.get("music").get("playUrl").text();

        metaData.uniqueId = base.get("author").safeText();

        return metaData;
    }

    protected static class MetaData {
        // video
        String cover; // image url
        String pageUrl;
        String videoId;
        String videoUrl;
        String uri;
        int duration; // in seconds
        String title;

        // backup
        String musicUrl;

        // author
        String uniqueId;

        AudioTrackInfo toTrackInfo() {
            return new AudioTrackInfo(
                this.title,
                this.uniqueId,
                this.duration * 1000L,
                this.videoId,
                false,
                this.pageUrl,
                this.cover,
                null
            );
        }

        // TEMP
        @Override
        public String toString() {
            return "MetaData{" +
                "cover='" + cover + '\'' +
                ", pageUrl='" + pageUrl + '\'' +
                ", videoId='" + videoId + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", uri='" + uri + '\'' +
                ", duration=" + duration +
                ", title='" + title + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                '}';
        }
    }
}
