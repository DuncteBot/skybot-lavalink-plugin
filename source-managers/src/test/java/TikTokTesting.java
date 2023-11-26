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

import com.dunctebot.sourcemanagers.tiktok.TikTokAudioSourceManager;
import com.dunctebot.sourcemanagers.tiktok.TikTokAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

public class TikTokTesting {
    public static void main(String[] args) {
        final var mngr = new TikTokAudioSourceManager();
//        final var ref = new AudioReference("https://www.tiktok.com/@nataliya_xoxo_love/video/6923984361150745862", "aaaaa");
        final var ref = new AudioReference("https://www.tiktok.com/@kallmekris/video/7229737213712436486?lang=en", "aaaaa");
        final TikTokAudioTrack load = (TikTokAudioTrack) mngr.loadItem(null, ref);

        System.out.println(
            load.getPlaybackUrl()
        );

        System.out.println("Url cache");
        System.out.println(load.getUrlCache());
    }
}
