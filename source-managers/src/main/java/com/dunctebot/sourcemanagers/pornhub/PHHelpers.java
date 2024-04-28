package com.dunctebot.sourcemanagers.pornhub;

import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.jsoup.nodes.Element;

public final class PHHelpers {
    public static AudioTrack trackFromSearchElement(Element element, PornHubAudioSourceManager mngr) {
        final var info = infoFromSearchElement(element);
        return new PornHubAudioTrack(info, mngr);
    }

    public static AudioTrackInfo infoFromSearchElement(Element element) {
        // TODO: parse min:sec duration
        // final var durText = element.getElementsByClass("duration").first().text();

        // System.out.println(durText);

        final String title = element.getElementsByClass("title").first().text();
        final String author = element.getElementsByClass("usernameWrap").first().text();
        final long duration = Units.CONTENT_LENGTH_UNKNOWN;
        final String identifier = element.attr("data-video-vkey");
        final String uri = "https://www.pornhub.com/view_video.php?viewkey=" + identifier;
        final String imageUrl = element.getElementsByTag("img").first().attr("src");

        return new AudioTrackInfo(
                title, author, duration, identifier, false, uri, imageUrl, null
        );
    }
}
