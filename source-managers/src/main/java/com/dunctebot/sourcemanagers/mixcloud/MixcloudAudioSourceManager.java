/*
 * Copyright 2022 Duncan "duncte123" Sterken & devoxin
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

package com.dunctebot.sourcemanagers.mixcloud;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dunctebot.sourcemanagers.Utils.urlDecode;

public class MixcloudAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final String THUMBNAILER_BASE = "https://thumbnailer.mixcloud.com/unsafe/390x390/";
    private static final String GRAPHQL_AUDIO_REQUEST = "query PlayerHeroQuery(\n" +
        "    $lookup: CloudcastLookup!\n" +
        ") {\n" +
        "    cloudcast: cloudcastLookup(lookup: $lookup) {\n" +
        "        id\n" +
        "        name\n" +
        "        picture {\n" +
        "            isLight\n" +
        "            primaryColor\n" +
        "            darkPrimaryColor: primaryColor(darken: 60)\n" +
        "            ...UGCImage_picture\n" +
        "        }\n" +
        "        owner {\n" +
        "            ...AudioPageAvatar_user\n" +
        "            id\n" +
        "        }\n" +
        "        restrictedReason\n" +
        "        seekRestriction\n" +
        "        ...PlayButton_cloudcast\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "fragment AudioPageAvatar_user on User {\n" +
        "    displayName\n" +
        "    username\n" +
        "}\n" +
        "\n" +
        "fragment PlayButton_cloudcast on Cloudcast {\n" +
        "    restrictedReason\n" +
        "    owner {\n" +
        "        displayName\n" +
        "        country\n" +
        "        username\n" +
        "        isSubscribedTo\n" +
        "        isViewer\n" +
        "        id\n" +
        "    }\n" +
        "    slug\n" +
        "    id\n" +
        "    isDraft\n" +
        "    isPlayable\n" +
        "    streamInfo {\n" +
        "        hlsUrl\n" +
        "        dashUrl\n" +
        "        url\n" +
        "        uuid\n" +
        "    }\n" +
        "    audioLength\n" +
        "    seekRestriction\n" +
        "}\n" +
        "\n" +
        "fragment UGCImage_picture on Picture {\n" +
        "    urlRoot\n" +
        "    primaryColor\n" +
        "}\n";
    private static final Pattern URL_REGEX = Pattern.compile("https?://(?:(?:www|beta|m)\\.)?mixcloud\\.com/([^/]+)/(?!stream|uploads|favorites|listens|playlists)([^/]+)/?");

    @Override
    public String getSourceName() {
        return "mixcloud";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final Matcher matcher = URL_REGEX.matcher(reference.identifier);

        if (!matcher.matches()) {
            return null;
        }

        // retry if possible
        while (true) {
            try {
                return this.loadItemOnce(reference, matcher);
            } catch (Exception e) {
                if (!HttpClientTools.isRetriableNetworkException(e)) {
                    throw ExceptionTools.wrapUnfriendlyExceptions(
                        "Loading information for a MixCloud track failed.",
                        FriendlyException.Severity.FAULT, e);
                }
            }
        }
    }

    private AudioItem loadItemOnce(AudioReference reference, Matcher matcher) throws IOException {
        final String username = urlDecode(matcher.group(1));
        final String slug = urlDecode(matcher.group(2));
        final JsonBrowser trackInfo = this.extractTrackInfoGraphQl(username, slug);

        if (trackInfo == null) {
            return AudioReference.NO_TRACK;
        }

        final JsonBrowser restrictedReason = trackInfo.get("restrictedReason");

        if (!restrictedReason.isNull()) {
            throw new FriendlyException(
                "Playback of this track is restricted.",
                FriendlyException.Severity.COMMON,
                new Exception(restrictedReason.text())
            );
        }

        final String picturePath = trackInfo.get("picture").get("urlRoot").text();
        final String title = trackInfo.get("name").text();
        final long duration = trackInfo.get("audioLength").as(Long.class) * 1000;
        final String uploader = trackInfo.get("owner").get("username").text(); // displayName

        return new MixcloudAudioTrack(
            new AudioTrackInfo(
                title,
                uploader,
                duration,
                slug,
                false,
                reference.identifier,
                THUMBNAILER_BASE + picturePath,
                null
            ),
            this
        );
    }

    protected JsonBrowser extractTrackInfoGraphQl(String username, String slug) throws IOException {
        final var body = JsonBrowser.newMap();

        body.put("query", GRAPHQL_AUDIO_REQUEST);

        final var variables = JsonBrowser.newMap();

        variables.put("lookup", new MixcloudLookup(
            slug, username
        ));

        body.put("variables", variables);

        final HttpPost httpPost = new HttpPost("https://app.mixcloud.com/graphql");

        httpPost.setEntity(new StringEntity(body.text(), ContentType.APPLICATION_JSON));

        try (final CloseableHttpResponse res = getHttpInterface().execute(httpPost)) {
            final int statusCode = res.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null;
                }

                throw new IOException("Invalid status code for Mixcloud track page response: " + statusCode);
            }

            final String content = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);
            final JsonBrowser json = JsonBrowser.parse(content).get("data").get("cloudcast");

            if (json.get("streamInfo").isNull()) {
                return null;
            }

            return json;
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
        return new MixcloudAudioTrack(trackInfo, this);
    }
}
