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

package com.dunctebot.sourcemanagers.clypit;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.IdentifiedAudioReference;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClypitAudioSourceManager extends AbstractDuncteBotHttpSource {

    private static final Pattern CLYPIT_REGEX = Pattern.compile("(http://|https://(www\\.)?)?clyp\\.it/(.*)");

    @Override
    public String getSourceName() {
        return "clypit";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final Matcher m = CLYPIT_REGEX.matcher(reference.identifier);

        if (!m.matches()) {
            return null;
        }

        try {
            final String clypitId = m.group(m.groupCount());
            final JsonBrowser json = fetchJson(clypitId);

            if (json == null) {
                return AudioReference.NO_TRACK;
            }

            final IdentifiedAudioReference ref = new IdentifiedAudioReference(
                json.get("Mp3Url").safeText(),
                reference.identifier,
                json.get("Title").safeText()
            );

            return new ClypitAudioTrack(AudioTrackInfoBuilder.create(ref, null).build(), this);
        }
        catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    // Switched from WebUtils to lavaplayer's stuff because that is better I guess
    private JsonBrowser fetchJson(String itemId) throws IOException {
        final HttpGet httpGet = new HttpGet("https://api.clyp.it/" + itemId);

        try (final CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                if (statusCode == 404) {
                    return null;
                }

                throw new IOException("Unexpected status code for video page response: " + statusCode);
            }

            final String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            return JsonBrowser.parse(json);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new ClypitAudioTrack(trackInfo, this);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // empty because we don't need them
    }
}
