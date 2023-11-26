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
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dunctebot.sourcemanagers.Utils.USER_AGENT;
import static com.dunctebot.sourcemanagers.Utils.isURL;
import static com.dunctebot.sourcemanagers.reddit.RedditAudioTrack.getPlaybackUrl;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.JsonBrowser.NULL_BROWSER;

public class RedditAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final Pattern FULL_LINK_REGEX = Pattern.compile("https:\\/\\/(?:www|old)\\.reddit\\.com\\/r\\/(?:[^\\/]+)\\/(?:[^\\/]+)\\/([^\\/]+)(?:\\/?(?:[^\\/]+)?\\/?)?");
    private static final Pattern VIDEO_LINK_REGEX = Pattern.compile("https:\\/\\/v\\.redd\\.it\\/([^\\/]+)(?:.*)?");

    public RedditAudioSourceManager() {
        this.configureBuilder(
            (builder) -> builder.setUserAgent(USER_AGENT)
        );
    }

    @Override
    public String getSourceName() {
        return "reddit";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final String identifier = reference.identifier;
        final Matcher fullLink = FULL_LINK_REGEX.matcher(identifier);

        // If it is a full link to a reddit post we can extract the id easily
        // and send that to fetch the json and build the track
        if (fullLink.matches()) {
            final String group = fullLink.group(fullLink.groupCount());
            final JsonBrowser data = this.fetchJson(group);

            return this.buildTrack(data, identifier);
        }

        final Matcher videoLink = VIDEO_LINK_REGEX.matcher(identifier);

        // If we have a short video link we firstly need to follow all redirects
        if (videoLink.matches()) {
            // Once we have the link we can extract the post id and build the track the normal way
            final String actualRedditUrl = this.fetchRedirectUrl(identifier);
            final String id = this.getPostId(actualRedditUrl);
            final JsonBrowser data = this.fetchJson(id);

            return this.buildTrack(data, actualRedditUrl);
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new RedditAudioTrack(trackInfo, this);
    }

    private String getPostId(String url) {
        final Matcher matcher = FULL_LINK_REGEX.matcher(url);

        if (matcher.matches()) {
            return matcher.group(matcher.groupCount());
        }

        return url;
    }

    private String fetchRedirectUrl(String vRedditUrl) {
        final HttpGet httpGet = new HttpGet(vRedditUrl);
        final HttpInterface httpInterface = this.getHttpInterface();

        // Follow all redirects until there are no more to follow and return that
        try (final CloseableHttpResponse ignored = httpInterface.execute(httpGet)) {
            return httpInterface.getFinalLocation().toString();
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load data from reddit", COMMON, e);
        }
    }

    @Nullable
    private JsonBrowser fetchJson(String pageURl) {
        // Fetch the json from the reddit api so we don't get any useless stuff we don't care about
        final HttpGet httpGet = new HttpGet("https://api.reddit.com/api/info/?id=t3_" + pageURl);

        try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final JsonBrowser child = JsonBrowser.parse(content).get("data").get("children").index(0);

            // If we have nothing in the children array we can safely return null
            if (child.equals(NULL_BROWSER)) {
                return null;
            }

            return child.get("data");
        }
        catch (IOException e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load data from reddit", COMMON, e);
        }
    }

    private boolean canPlayAudio(String id) {
        final HttpGet httpGet = new HttpGet(getPlaybackUrl(id));

        // Probe the audio and check the response code, if it is 200 we have some audio
        try (final CloseableHttpResponse response = this.getHttpInterface().execute(httpGet)) {
            return response.getStatusLine().getStatusCode() == 200;
        }
        catch (IOException e) {
            return false;
        }
    }

    private AudioItem buildTrack(@Nullable JsonBrowser data, String pageURl) {
        // If we don't have any data we can return null
        if (data == null) {
            return AudioReference.NO_TRACK;
        }

        final String postHint = data.get("post_hint").safeText();

        // Check if this is a video post that is hosted on reddit
        // we cannot play other types
        if (!"hosted:video".equals(postHint)) {
            throw new FriendlyException("This video is not hosted on the reddit website," +
                " only videos hosted on the reddit website can be played", COMMON, null);
        }

        final JsonBrowser media = data.get("media").get("reddit_video");
        final String url = data.get("url").safeText();

        if (media.get("is_gif").asBoolean(false)) {
            throw new FriendlyException("Cannot play gifs", COMMON, null);
        }

        final Matcher videoLink = VIDEO_LINK_REGEX.matcher(url);

        // This should never happen unless my regex is wrong
        if (!videoLink.matches()) {
            return AudioReference.NO_TRACK;
        }

        final String videoId = videoLink.group(videoLink.groupCount());

        // Probe the audio to check if we can actually play it (there's probably a better way with the dash playlists)
        if (!this.canPlayAudio(videoId)) {
            throw new FriendlyException("This video does not have audio", COMMON, null);
        }

        String thumbnail = data.get("thumbnail").safeText();

        // Fallback to null if the thumbnail is not a url
        if (!isURL(thumbnail)) {
            thumbnail = null;
        }

        return new RedditAudioTrack(
            new AudioTrackInfo(
                data.get("title").safeText(),
                "u/" + data.get("author").safeText(),
                media.get("duration").asLong(1) * 1000,
                videoId,
                false,
                pageURl,
                thumbnail,
                null
            ),
            this
        );
    }
}
