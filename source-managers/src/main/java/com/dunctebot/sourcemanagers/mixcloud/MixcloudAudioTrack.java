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
import com.dunctebot.sourcemanagers.MpegTrack;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.IOException;
import java.util.Base64;

import static com.dunctebot.sourcemanagers.Utils.decryptXor;
import static com.dunctebot.sourcemanagers.Utils.urlDecode;

public class MixcloudAudioTrack extends MpegTrack {
    private static final String DECRYPTION_KEY = "IFYOUWANTTHEARTISTSTOGETPAIDDONOTDOWNLOADFROMMIXCLOUD";

    public MixcloudAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    protected long getTrackDuration() {
        // supply an unknown content length so lavaplayer actually loads the file
        return Units.CONTENT_LENGTH_UNKNOWN;
    }

    @Override
    public String getPlaybackUrl() {
        try {
            final var trackInfo = getSourceManager().extractTrackInfoGraphQl(
                this.trackInfo.author,
                urlDecode(this.trackInfo.identifier)
            );
            final String encryptedUrl = trackInfo.get("streamInfo").get("url").text();
            final String xorUrl = new String(Base64.getDecoder().decode(encryptedUrl));

            return decryptXor(xorUrl, DECRYPTION_KEY);
        } catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(
                "Playback of mixcloud track failed",
                FriendlyException.Severity.SUSPICIOUS,
                e
            );
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new MixcloudAudioTrack(trackInfo, getSourceManager());
    }

    @Override
    public MixcloudAudioSourceManager getSourceManager() {
        return (MixcloudAudioSourceManager) super.getSourceManager();
    }
}
