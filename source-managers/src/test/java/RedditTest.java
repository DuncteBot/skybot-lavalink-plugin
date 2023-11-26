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

import com.dunctebot.sourcemanagers.reddit.RedditAudioSourceManager;
import com.dunctebot.sourcemanagers.reddit.RedditAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

public class RedditTest {
    public static void main(String[] args) {
        final var id1 = "https://www.reddit.com/r/funnyvideos/comments/15cvyaj/cant_find_this_woman_but_she_is_smart/";
        final var mngr = new RedditAudioSourceManager();
        final var res1 = mngr.loadItem(null, new AudioReference(id1, ""));

        System.out.println(res1);

        if (res1 instanceof RedditAudioTrack) {
            final var track = (RedditAudioTrack) res1;

            System.out.println(track.getPlaybackUrl());
        }
    }
}
