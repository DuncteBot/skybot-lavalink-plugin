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

package com.dunctebot.sourcemanagers;

import com.dunctebot.sourcemanagers.clypit.ClypitAudioSourceManager;
import com.dunctebot.sourcemanagers.getyarn.GetyarnAudioSourceManager;
import com.dunctebot.sourcemanagers.mixcloud.MixcloudAudioSourceManager;
import com.dunctebot.sourcemanagers.ocremix.OCRemixAudioSourceManager;
import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager;
import com.dunctebot.sourcemanagers.reddit.RedditAudioSourceManager;
import com.dunctebot.sourcemanagers.soundgasm.SoundGasmAudioSourceManager;
import com.dunctebot.sourcemanagers.speech.SpeechAudioSourceManager;
import com.dunctebot.sourcemanagers.tiktok.TikTokAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

public class DuncteBotSources {
    /**
     * Registers all custom source managers onto the player manager
     *
     * @param playerManager Your lavalink player manager
     * @param speechLanguage The default language for the TTS engine
     */
    public static void registerAll(AudioPlayerManager playerManager, String speechLanguage) {
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new ClypitAudioSourceManager());
        playerManager.registerSourceManager(new SpeechAudioSourceManager(speechLanguage));
        playerManager.registerSourceManager(new PornHubAudioSourceManager());
        playerManager.registerSourceManager(new RedditAudioSourceManager());
        playerManager.registerSourceManager(new OCRemixAudioSourceManager());
        playerManager.registerSourceManager(new TikTokAudioSourceManager());
        playerManager.registerSourceManager(new MixcloudAudioSourceManager());
        playerManager.registerSourceManager(new SoundGasmAudioSourceManager());
    }

    /**
     * Registers only the sourcemanagers used on DuncteBot, missing sources are as follows
     *
     * <ul>
     *     <li>Mixcloud: they banned my server's ips</li>
     * </ul>
     *
     * @param playerManager Lavaplayer player manager
     * @param speechLanguage Default language for tts
     * @param playlistPageCount Overriding the youtube playlist count
     */
    public static void registerDuncteBot(AudioPlayerManager playerManager, String speechLanguage, int playlistPageCount) {
        final YoutubeAudioSourceManager youtubeSource = playerManager.source(YoutubeAudioSourceManager.class);
        youtubeSource.setPlaylistPageCount(playlistPageCount);

        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new ClypitAudioSourceManager());
        playerManager.registerSourceManager(new SpeechAudioSourceManager(speechLanguage));
        playerManager.registerSourceManager(new PornHubAudioSourceManager());
        playerManager.registerSourceManager(new RedditAudioSourceManager());
        playerManager.registerSourceManager(new OCRemixAudioSourceManager());
        playerManager.registerSourceManager(new TikTokAudioSourceManager());
        playerManager.registerSourceManager(new SoundGasmAudioSourceManager());
    }
}
