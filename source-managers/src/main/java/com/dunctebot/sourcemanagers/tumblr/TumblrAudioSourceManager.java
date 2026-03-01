package com.dunctebot.sourcemanagers.tumblr;

// TODO: posts to test
// https://www.tumblr.com/pukicho/809016701638279168/todays-my-27th-birthday (grab last audio)
// https://www.tumblr.com/pukicho/801421495209476096/you-control-the-volume-on-your-device
// https://pukicho.tumblr.com/post/801421495209476096/you-control-the-volume-on-your-device
// https://www.tumblr.com/pukicho/802302724791320576/fucking-around-with-granular-synthesis
// https://www.tumblr.com/ectoblood/809867644956213248/this-is-the-only-valid-tik-tok-bro

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.tumblr.audio.TumblrBasePostAudioTrack;
import com.dunctebot.sourcemanagers.tumblr.audio.TumblrMp3AudioTrack;
import com.dunctebot.sourcemanagers.tumblr.audio.TumblrMpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static com.dunctebot.sourcemanagers.Utils.USER_AGENT;

public class TumblrAudioSourceManager extends AbstractDuncteBotHttpSource {
    private static final Pattern NORMAL_POST_PATTERN = Pattern.compile("https:\\/\\/(?:www\\.)?tumblr\\.com\\/(?<username>[^\\/]+)\\/(?<postId>[^\\/]+)\\/?.*");
    private static final Pattern BLOG_SUB_PATTERN = Pattern.compile("https:\\/\\/(?<username>[^\\.]+)\\.tumblr\\.com\\/post\\/(?<postId>[^\\/]+)\\/?.*");
    private static final Logger log = LoggerFactory.getLogger(TumblrAudioSourceManager.class);

    // TODO: https://tmblr.co/ZWMt9uiVE9gHKi00 (when I feel like it tbh, nobody uses these links)

    private final String oauthConsumerKey;
    private final String oauthSecretKey;

    private long tokenExpiresAt = -1;
    private String accessToken = null;

    public TumblrAudioSourceManager(String consumerKey, String secretKey) {
        this.oauthConsumerKey = consumerKey;
        this.oauthSecretKey = secretKey;
    }

    @Override
    public String getSourceName() {
        return "tumblr";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final String identifier = reference.identifier;
        final var dashMatcher = NORMAL_POST_PATTERN.matcher(identifier);

        try {
            if (dashMatcher.matches()) {
                final var username = dashMatcher.group("username");
                final var postId = dashMatcher.group("postId");

                return this.fetchPostData(username, postId);
            }

            final var blogSubMatcher = BLOG_SUB_PATTERN.matcher(identifier);

            if (blogSubMatcher.matches()) {
                final var username = blogSubMatcher.group("username");
                final var postId = blogSubMatcher.group("postId");

                return this.fetchPostData(username, postId);
            }
        } catch (Exception e) {
            throw new FriendlyException("Failed to load tumblr data", FriendlyException.Severity.SUSPICIOUS, e);
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        if (!(track instanceof TumblrBasePostAudioTrack)) {
            throw new FriendlyException(
                    String.format("Track to encode is not a TumblrBasePostAudioTrack audio track but a %s", track.getClass()),
                    FriendlyException.Severity.FAULT,
                    null
            );
        }

        output.writeUTF(((TumblrBasePostAudioTrack) track).getMediaUrl());
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        final var mediaUrl = input.readUTF();

        if (trackInfo.uri.endsWith("audio/mpeg")) {
            return new TumblrMp3AudioTrack(trackInfo, this, mediaUrl);
        }

        if (trackInfo.uri.endsWith("video/mp4")) {
            return new TumblrMpegAudioTrack(trackInfo, this, mediaUrl);
        }

        throw new FriendlyException("Unknown media type encountered whilst trying to decode tumblr track", FriendlyException.Severity.COMMON, null);
    }

    public TumblrBasePostAudioTrack fetchPostData(String username, String postId) throws Exception {
        final var response = this.fetchNPFData(username, postId).get("response");

        if (response.isNull()) {
            return null;
        }

        final String tumblrPostUrl = String.format("https://www.tumblr.com/%s/%s", username, postId);

        final var mainPostExtract = this.postToTrack(response, tumblrPostUrl);

        if (mainPostExtract != null) {
            return mainPostExtract;
        }

        for (JsonBrowser trail : response.get("trail").values()) {
            final var trailExtract = this.postToTrack(trail, tumblrPostUrl);

            if (trailExtract != null) {
                return trailExtract;
            }
        }

        return null;
    }

    public TumblrBasePostAudioTrack postToTrack(JsonBrowser post, String postUrl) {
        final var content = post.get("content");

        // Should never happen, but just in case
        if (!content.isList()) {
            return null;
        }

        for (JsonBrowser ci : content.values()) {
            if ("audio".equals(ci.get("type").text())) {
                if (!"tumblr".equals(ci.get("provider").text())) {
                    throw new FriendlyException("Provider not supported: " + ci.get("provider").text(), FriendlyException.Severity.COMMON, null);
                }

                final var creatorName = getBlogName(post);
                final var artwork = post.get("blog").get("avatar").index(0).get("url").text();

                final var audioType = ci.get("media").get("type").text();
                final var audioUrl = ci.get("media").get("url").text();
                final var audioTitle = ci.get("title").textOrDefault("Tumblr Audio by " + creatorName);

                if (audioType.equals("audio/mpeg")) {
                    return new TumblrMp3AudioTrack(
                            new AudioTrackInfo(
                                    audioTitle,
                                    creatorName,
                                    Units.CONTENT_LENGTH_UNKNOWN,
                                    postUrl,
                                    false,
                                    postUrl + "?dbextra=" + audioType,
                                    artwork,
                                    null
                            ),
                            this,
                            audioUrl
                    );
                }
            } else if ("video".equals(ci.get("type").text())) {
                if (!"tumblr".equals(ci.get("provider").text())) {
                    throw new FriendlyException("Provider not supported: " + ci.get("provider").text(), FriendlyException.Severity.COMMON, null);
                }

                final var creatorName = getBlogName(post);
                final var artwork = post.get("blog").get("avatar").index(0).get("url").text();

                final var videoType = ci.get("media").get("type").text();
                final var videoUrl = ci.get("media").get("url").text();

                if (videoType.equals("video/mp4")) {
                    return new TumblrMpegAudioTrack(
                            new AudioTrackInfo(
                                    "Tumblr Video by " + creatorName,
                                    creatorName,
                                    Units.CONTENT_LENGTH_UNKNOWN,
                                    postUrl,
                                    false,
                                    postUrl + "?dbextra=" + videoType,
                                    artwork,
                                    null
                            ),
                            this,
                            videoUrl
                    );
                }
            }
        }

        return null;
    }

    public JsonBrowser fetchNPFData(String username, String postId) throws Exception {
        final var httpGet = new HttpGet(
                String.format(
                        "https://api.tumblr.com/v2/blog/%s/posts/%s",
                        username,
                        postId
                )
        );

        final var token = this.fetchOAuth2Token();

        httpGet.addHeader("Authorization", "Bearer " + token);
        httpGet.addHeader("Accept", "application/json");
        httpGet.setHeader("User-Agent", USER_AGENT);

        try (final CloseableHttpResponse res = getHttpInterface().execute(httpGet)) {
            final String content = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);

            System.out.println(content);

            final int statusCode = res.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IOException("Tumblr api request failed: " + statusCode);
            }

            return JsonBrowser.parse(content);
        }
    }

    public String fetchOAuth2Token() throws Exception {
        if (System.currentTimeMillis() < this.tokenExpiresAt) {
            return this.accessToken;
        }

        final var httpPost = new HttpPost("https://api.tumblr.com/v2/oauth2/token");

        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", USER_AGENT);

        final var body = String.join("&", List.of(
                // Thank you for not documenting this LMAO
                "grant_type=client_credentials",
                "client_id=" + this.oauthConsumerKey,
                "client_secret=" + this.oauthSecretKey
        ));

        httpPost.setEntity(
                new StringEntity(body, ContentType.APPLICATION_FORM_URLENCODED)
        );

        try (final CloseableHttpResponse res = getHttpInterface().execute(httpPost)) {
            final int statusCode = res.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IOException("Invalid status for fetching tumblr access token: " + statusCode);
            }

            final String content = IOUtils.toString(res.getEntity().getContent(), StandardCharsets.UTF_8);

            System.out.println(content);

            final var json = JsonBrowser.parse(content);
            this.accessToken = json.get("access_token").text();
            // expires_in is in seconds
            this.tokenExpiresAt = System.currentTimeMillis() + (json.get("expires_in").asLong(0L) * 1000L);

            return this.accessToken;
        }
    }

    private static String getBlogName(JsonBrowser post) {
        final var blogTitle = post.get("blog").get("title").safeText();

        if (blogTitle.isBlank()) {
            return post.get("blog").get("name").text();
        }

        return blogTitle;
    }
}
