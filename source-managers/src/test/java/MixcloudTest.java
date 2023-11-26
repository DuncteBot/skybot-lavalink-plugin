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

import com.dunctebot.sourcemanagers.mixcloud.MixcloudAudioSourceManager;
import com.dunctebot.sourcemanagers.mixcloud.MixcloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

public class MixcloudTest {
    public static void main(String[] args) {
//        System.out.println(MixcloudAudioSourceManager.GRAPHQL_AUDIO_REQUEST);

        final var url = "https://www.mixcloud.com/jordy-boesten2/the-egotripper-lets-walk-to-my-house-mix-259/";
//        final var url = "https://www.mixcloud.com/Hirockn/the-100-best-tracks-2020-hip-hop-rb-pops-etc-the-weeknd-dababy-dua-lipa-juice-wrld-etc/";
        final var mnrg = new MixcloudAudioSourceManager();
        final AudioItem track = mnrg.loadItem(null, new AudioReference(url, null));

        if (track.equals(AudioReference.NO_TRACK)) {
            return;
        }

        final var mixcloudTrack = (MixcloudAudioTrack) track;
        final var playbackUrl = mixcloudTrack.getPlaybackUrl();

        System.out.println(playbackUrl);
    }
}
