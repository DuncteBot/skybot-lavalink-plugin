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

import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager;
import com.dunctebot.sourcemanagers.pornhub.PornHubAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

public class PHSearchTest {
    public static void main(String[] args) throws Exception {
//        final var link = "https://www.pornhub.com/view_video.php?viewkey=ph5fc5ef73cfc87";
        final var link = "https://www.pornhub.com/view_video.php?viewkey=ph6383940fcb8d7";
        final var mngr = new PornHubAudioSourceManager();

        final var test = mngr.attemptSearch("minecraft bedwards");

        System.out.println(test);
    }
}
