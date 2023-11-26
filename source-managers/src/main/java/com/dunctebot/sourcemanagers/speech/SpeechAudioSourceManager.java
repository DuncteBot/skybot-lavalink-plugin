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

package com.dunctebot.sourcemanagers.speech;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SpeechAudioSourceManager extends AbstractDuncteBotHttpSource {

    private static final String PREFIX = "speak:";
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.google.com/translate_tts" +
        "?tl=%language%" +
        "&q=%query%" +
        "&ie=UTF-8&total=1&idx=0" +
        "&text" + "len=%length%" +
        "&client=tw-ob";

    private final String templateURL;

    /**
     * @param language
     *         The language and accent code to play back audio in
     */
    public SpeechAudioSourceManager(String language) {
        this.templateURL = GOOGLE_TRANSLATE_URL.replace("%language%", language);
    }

    @Override
    public String getSourceName() {
        return "speak";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        // We check if it's larger so we don't send requests of nothing
        if (!reference.identifier.startsWith(PREFIX) || reference.identifier.length() <= PREFIX.length()) {
            return null;
        }

        String data = reference.identifier.substring(PREFIX.length());
        data = data
            // Remove whitespaces at the end
            .trim()
            // Remove whitespaces at the front
            .replaceAll("^\\s+", "");

        final String encoded = URLEncoder.encode(data, StandardCharsets.UTF_8);

        final String mp3URL = templateURL
            .replace("%length%", Integer.toString(data.length()))
            .replace("%query%", encoded);

        // Redirect to somewhere else
        return new SpeechAudioTrack(new AudioTrackInfo(
            "Speaking " + data,
            "TTS B0t",
            Units.CONTENT_LENGTH_UNKNOWN,
            reference.identifier,
            false,
            mp3URL
        ), this);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // empty because we don't need them
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpeechAudioTrack(trackInfo, this);
    }
}
