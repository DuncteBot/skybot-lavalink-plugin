package lavalink.server.config

import com.dunctebot.sourcemanagers.DuncteBotSources
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.*
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup
import com.sedmelluq.lava.extensions.youtuberotator.planner.*
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Created by napster on 05.03.18.
 */
@Configuration
class AudioPlayerConfiguration {

    private val log = LoggerFactory.getLogger(AudioPlayerConfiguration::class.java)

    @Bean
    fun audioPlayerManagerSupplier(sources: AudioSourcesConfig, serverConfig: ServerConfig, routePlanner: AbstractRoutePlanner?): AudioPlayerManager {
        val audioPlayerManager = DefaultAudioPlayerManager()

        if (serverConfig.isGcWarnings) {
            audioPlayerManager.enableGcMonitoring()
        }

        val defaultFrameBufferDuration = audioPlayerManager.frameBufferDuration
        serverConfig.frameBufferDurationMs?.let {
            if (it < 200) { // At the time of writing, LP enforces a minimum of 200ms.
                log.warn("Buffer size of {}ms is illegal. Defaulting to {}", it, defaultFrameBufferDuration)
            }

            val bufferDuration = it.takeIf { it >= 200 } ?: defaultFrameBufferDuration
            log.debug("Setting frame buffer duration to {}", bufferDuration)
            audioPlayerManager.frameBufferDuration = bufferDuration
        }

        if (sources.isYoutube) {
            val youtube = YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled)
            if (routePlanner != null) {
                val retryLimit = serverConfig.ratelimit?.retryLimit ?: -1
                when {
                    retryLimit < 0 -> YoutubeIpRotatorSetup(routePlanner).forSource(youtube).setup()
                    retryLimit == 0 -> YoutubeIpRotatorSetup(routePlanner).forSource(youtube).withRetryLimit(Int.MAX_VALUE).setup()
                    else -> YoutubeIpRotatorSetup(routePlanner).forSource(youtube).withRetryLimit(retryLimit).setup()

                }
            }
            val playlistLoadLimit = serverConfig.youtubePlaylistLoadLimit
            if (playlistLoadLimit != null) youtube.setPlaylistPageCount(playlistLoadLimit)
            audioPlayerManager.registerSourceManager(youtube)

            // Clyp.it, ph, speach and youtube filter override
            DuncteBotSources.registerCustom(audioPlayerManager, "en-AU", playlistLoadLimit ?: 1)
        }
        if (sources.isSoundcloud) {
            val dataReader = DefaultSoundCloudDataReader()
            val htmlDataLoader = DefaultSoundCloudHtmlDataLoader()
            val formatHandler = DefaultSoundCloudFormatHandler()

            audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager(
                    serverConfig.isSoundcloudSearchEnabled,
                    dataReader,
                    htmlDataLoader,
                    formatHandler,
                    DefaultSoundCloudPlaylistLoader(htmlDataLoader, dataReader, formatHandler)
            ))
        }
        if (sources.isBandcamp) audioPlayerManager.registerSourceManager(BandcampAudioSourceManager())
        if (sources.isTwitch) audioPlayerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        if (sources.isVimeo) audioPlayerManager.registerSourceManager(VimeoAudioSourceManager())
        if (sources.isMixer) audioPlayerManager.registerSourceManager(BeamAudioSourceManager())
        if (sources.isHttp) audioPlayerManager.registerSourceManager(HttpAudioSourceManager())
        if (sources.isLocal) audioPlayerManager.registerSourceManager(LocalAudioSourceManager())

        audioPlayerManager.configuration.isFilterHotSwapEnabled = true

        return audioPlayerManager
    }

    @Bean
    fun routePlanner(serverConfig: ServerConfig): AbstractRoutePlanner? {
        val rateLimitConfig = serverConfig.ratelimit
        if (rateLimitConfig == null) {
            log.debug("No rate limit config block found, skipping setup of route planner")
            return null
        }
        val ipBlockList = rateLimitConfig.ipBlocks
        if (ipBlockList.isEmpty()) {
            log.info("List of ip blocks is empty, skipping setup of route planner")
            return null
        }

        val blacklisted = rateLimitConfig.excludedIps.map { InetAddress.getByName(it) }
        val filter = Predicate<InetAddress> {
            !blacklisted.contains(it)
        }
        val ipBlocks = ipBlockList.map {
            when {
                Ipv4Block.isIpv4CidrBlock(it) -> Ipv4Block(it)
                Ipv6Block.isIpv6CidrBlock(it) -> Ipv6Block(it)
                else -> throw RuntimeException("Invalid IP Block '$it', make sure to provide a valid CIDR notation")
            }
        }

        return when (rateLimitConfig.strategy.toLowerCase().trim()) {
            "rotateonban" -> RotatingIpRoutePlanner(ipBlocks, filter, rateLimitConfig.searchTriggersFail)
            "loadbalance" -> BalancingIpRoutePlanner(ipBlocks, filter, rateLimitConfig.searchTriggersFail)
            "nanoswitch" -> NanoIpRoutePlanner(ipBlocks, rateLimitConfig.searchTriggersFail)
            "rotatingnanoswitch" -> RotatingNanoIpRoutePlanner(ipBlocks, filter, rateLimitConfig.searchTriggersFail)
            else -> throw RuntimeException("Unknown strategy!")
        }
    }

}
