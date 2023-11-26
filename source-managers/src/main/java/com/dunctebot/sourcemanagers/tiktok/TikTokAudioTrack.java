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
import com.dunctebot.sourcemanagers.MpegTrack;
import com.dunctebot.sourcemanagers.Pair;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class TikTokAudioTrack extends MpegTrack {
    private Pair<String, String> urlCache = null;
    private boolean failedOnce = false;

    public TikTokAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    public Pair<String, String> getUrlCache() {
        return urlCache;
    }

    @Override
    public String getPlaybackUrl() {
        try {
            if (this.urlCache == null) {
                this.urlCache = loadPlaybackUrl();
            }

            if (this.failedOnce) {
                return this.urlCache.getRight();
            }

            return this.urlCache.getLeft();
        } catch (Exception e) {
            throw new FriendlyException("Could not load TikTok video", SUSPICIOUS, e);
        }
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (HttpInterface httpInterface = this.getHttpInterface()) {
            loadStream(executor, httpInterface);
        }
    }

    @Override
    protected void loadStream(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface) throws Exception {
        try {
            super.loadStream(localExecutor, httpInterface);
        } catch (Exception e) {
            if (this.failedOnce) {
                throw e;
            }

            this.failedOnce = true;
            super.loadStream(localExecutor, httpInterface);
        }
    }

    /*@Override
    protected void loadStream(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface) throws Exception {
        final String trackUrl = getPlaybackUrl();
        log.debug("Starting {} track from URL: {}", getSourceManager().getSourceName(), trackUrl);
        // Setting contentLength (last param) to null makes it default to Long.MAX_VALUE
        try (final PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackUrl), Units.CONTENT_LENGTH_UNKNOWN)) {
            // dump the stream
            Files.copy(
                stream,
                new File("DUMP.raw").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );

            processDelegate(createAudioTrack(this.trackInfo, stream), localExecutor);
        }
    }*/

    protected Pair<String, String> loadPlaybackUrl() throws Exception {
        final TikTokAudioSourceManager.MetaData metdata = this.getSourceManager().extractData(
            this.trackInfo.author,
            this.trackInfo.identifier
        );

        return new Pair<>(
            metdata.videoUrl,
            metdata.musicUrl
        );
    }

    @Override
    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        if (this.failedOnce && this.urlCache.getRight().contains(".mp3")) {
            return new Mp3AudioTrack(trackInfo, stream);
        }

        return super.createAudioTrack(trackInfo, stream);
    }

    @Override
    protected long getTrackDuration() {
        return Units.CONTENT_LENGTH_UNKNOWN;
    }

    @Override
    protected HttpInterface getHttpInterface() {
        return this.getSourceManager().getHttpInterface();
    }

    @Override
    public TikTokAudioSourceManager getSourceManager() {
        return (TikTokAudioSourceManager) super.getSourceManager();
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new TikTokAudioTrack(this.trackInfo, getSourceManager());
    }
}
