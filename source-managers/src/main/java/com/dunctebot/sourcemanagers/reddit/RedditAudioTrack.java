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

package com.dunctebot.sourcemanagers.reddit;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.MpegTrack;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class RedditAudioTrack extends MpegTrack {
    public RedditAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    public String getPlaybackUrl() {
        return getPlaybackUrl(this.trackInfo.identifier);
    }

    static String getPlaybackUrl(String id) {
        return "https://v.redd.it/" + id + "/DASH_audio.mp4?source=fallback";
    }

    @Override
    protected long getTrackDuration() {
        // return unknown so we get a more accurate representation of the length
        return Units.CONTENT_LENGTH_UNKNOWN;
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new RedditAudioTrack(this.trackInfo, this.getSourceManager());
    }
}
