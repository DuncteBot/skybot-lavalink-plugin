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

import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

public class IdentifiedAudioReference extends AudioReference {

    private final String uri;

    public IdentifiedAudioReference(String identifier, String uri, String title) {
        super(identifier, title);

        this.uri = uri;
    }

    @Override
    public Long getLength() {
        return Units.CONTENT_LENGTH_UNKNOWN;
    }

    @Override
    public String getUri() {
        return uri;
    }
}
