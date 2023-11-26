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
import com.dunctebot.sourcemanagers.MpegTrack;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class SoundGasmAudioTrack extends MpegTrack {
    public SoundGasmAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    protected long getTrackDuration() {
        return Units.CONTENT_LENGTH_UNKNOWN;
    }

    @Override
    public String getPlaybackUrl() {
        return "https://media.soundgasm.net/sounds/" + this.trackInfo.identifier + ".m4a";
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SoundGasmAudioTrack(this.trackInfo, getSourceManager());
    }
}
