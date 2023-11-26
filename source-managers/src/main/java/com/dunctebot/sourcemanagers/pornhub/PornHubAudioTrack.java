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
import com.dunctebot.sourcemanagers.MpegTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager.VIDEO_INFO_REGEX;
import static com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager.getPlayerPage;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.*;

public class PornHubAudioTrack extends MpegTrack {
    private static final Pattern MEDIA_STRING_FILTER = Pattern.compile("\\/\\* \\+ [a-zA-Z0-9_]+ \\+ \\*\\/");

    public PornHubAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource sourceManager) {
        super(trackInfo, sourceManager);
    }

    @Override
    public String getPlaybackUrl() {
        try {
            return loadFromMediaInfo();
        } catch (IOException e) {
            throw new FriendlyException("Could not load PornHub video", SUSPICIOUS, e);
        }
    }

    public String loadFromMediaInfo() throws IOException {
        final HttpGet httpGet = new HttpGet(getPlayerPage(this.trackInfo.identifier));

        httpGet.setHeader("Cookie", "platform=pc; age_verified=1");

        try (final CloseableHttpResponse response = this.getSourceManager().getHttpInterface().execute(httpGet)) {
            final String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final Matcher matcher = VIDEO_INFO_REGEX.matcher(html);

            if (matcher.find()) {
                final String js = matcher.group(matcher.groupCount());
                final JsonBrowser videoInfo = JsonBrowser.parse(js);

                if (videoInfo.get("video_unavailable_country").asBoolean(false)) {
                    throw new FriendlyException("Video is not available in your country", COMMON, null);
                }

                final JsonBrowser defs = videoInfo.get("mediaDefinitions");

                if (defs.isNull()) {
                    throw new FriendlyException("Media info not present", COMMON, null);
                }

                int i = 0;
                while (!defs.index(i).isNull()) {
                    // we found the default quality
                    if ("mp4".equalsIgnoreCase(defs.index(i).get("format").safeText())) {
                        final String cookies = Arrays.stream(response.getHeaders("Set-Cookie"))
                            .map(NameValuePair::getValue)
                            .map((s) -> s.split(";", 2)[0])
                            .collect(Collectors.joining("; "));
                        final String getMedia = parseJsValueToUrl(
                            html,
                            scoupMediaVar(html, "media_" + i)
                        );

                        return loadMp4Url(getMedia, cookies);
                    }

                    i++;
                }

                /*return parseJsValueToUrl(
                    html,
                    scoupMediaVar(html, "media_0") // fallback to first item (not mp4)
                );*/
            }

            throw new FriendlyException("Could not find media info", COMMON, null);
        }
    }

    private String scoupMediaVar(String html, String varName) {
        final Pattern pattern = Pattern.compile("(var(?:\\s+)?" + varName + "(?:\\s+)?=(?:\\s+)?[^;]+;)");
        final Matcher matcher = pattern.matcher(html);

        if (!matcher.find()) {
            throw new FriendlyException("Media var has changed, please contact developer", FAULT, null);
        }

        return matcher.group(matcher.groupCount());
    }

    private String parseJsValueToUrl(String htmlPage, String js) {
        final String filteredJsValue = MEDIA_STRING_FILTER.matcher(js).replaceAll("");
        final String variables = filteredJsValue.split("=")[1].split(";")[0];
        final String[] items = variables.split("\\+");
        final List<String> videoParts = new ArrayList<>();

        for (final String i : items) {
            final String item = i.trim();
            final String regex = "var\\s+?" + item + "=\"([a-zA-Z0-9=?&%~_\\-\\.\\/\"\\+: ]+)\";";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(htmlPage);

            if (!matcher.find()) {
                System.out.println(htmlPage);
                throw new FriendlyException("URL part " + item + " missing", SUSPICIOUS, null);
            }

            videoParts.add(
                matcher.group(matcher.groupCount()).replaceAll("\"\\s+?\\+\\s+?\"", "")
            );
        }

        return String.join("", videoParts);
    }

    private String loadMp4Url(String jsonPage, String cookie) throws IOException {
        final HttpGet mediaGet = new HttpGet(jsonPage);

        mediaGet.setHeader("Cookie", cookie + "; platform=pc; age_verified=1");
        mediaGet.setHeader("Referer", getPlayerPage(this.trackInfo.identifier));

        try (final CloseableHttpResponse response = this.getSourceManager().getHttpInterface().execute(mediaGet)) {
            final String body = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final JsonBrowser json = JsonBrowser.parse(body);

            for (JsonBrowser info : json.values()) {
                if (info.get("defaultQuality").asBoolean(false)) {
                    return info.get("videoUrl").text();
                }
            }

            final JsonBrowser firstItem = json.index(0);

            if (firstItem.isNull()) {
                throw new FriendlyException("Video url missing on playback page", FAULT, null);
            }

            final String videoUrl = firstItem.get("videoUrl").text();

            if (videoUrl == null) {
                throw new FriendlyException("Video url missing on playback page", FAULT, null);
            }

            return videoUrl;
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new PornHubAudioTrack(trackInfo, getSourceManager());
    }
}
