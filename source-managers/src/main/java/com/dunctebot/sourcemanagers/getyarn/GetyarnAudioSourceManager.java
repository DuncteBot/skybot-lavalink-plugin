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

package com.dunctebot.sourcemanagers.getyarn;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.IdentifiedAudioReference;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetyarnAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final Pattern GETYARN_REGEX = Pattern.compile("(?:http://|https://(?:www\\.)?)?getyarn\\.io/yarn-clip/(.*)");

    @Override
    public String getSourceName() {
        return "getyarn.io";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final Matcher m = GETYARN_REGEX.matcher(reference.identifier);

        if (!m.matches()) {
            return null;
        }

        final String videoId = m.group(m.groupCount());

        final IdentifiedAudioReference ref = new IdentifiedAudioReference(
            videoId,
            reference.identifier,
            videoId
        );

        return new GetyarnAudioTrack(
            AudioTrackInfoBuilder.create(ref, null)
                .setArtworkUrl("https://y.yarn.co/" + videoId + "_screenshot.jpg")
                .build(),
            this
        );
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
        return new GetyarnAudioTrack(trackInfo, this);
    }
}
