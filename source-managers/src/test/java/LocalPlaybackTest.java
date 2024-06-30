import com.dunctebot.sourcemanagers.elgato.streamdeck.StreamDeckAudioSourceManager;
import com.dunctebot.sourcemanagers.pixeldrain.PixeldrainAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;

public class LocalPlaybackTest {
    public static void main(String[] args) throws Exception {
        final var mngr = new PixeldrainAudioSourceManager();

        AudioPlayerManager manager = new DefaultAudioPlayerManager();

        manager.registerSourceManager(mngr);

        manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);

        AudioPlayer player = manager.createPlayer();

        player.setVolume(100);

        manager.loadItem(
                "https://pixeldrain.com/u/WUJkkH3F",
                new FunctionalResultHandler(item -> {
                    player.playTrack(item);
                }, playlist -> {
                    player.playTrack(playlist.getTracks().get(0));
                }, null, null)
        );


        AudioDataFormat format = manager.getConfiguration().getOutputFormat();
        AudioInputStream stream = AudioPlayerInputStream.createStream(player, format, 10000L, false);
        SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

        line.open(stream.getFormat());
        line.start();

        byte[] buffer = new byte[COMMON_PCM_S16_BE.maximumChunkSize()];
        int chunkSize;

        while ((chunkSize = stream.read(buffer)) >= 0) {
            line.write(buffer, 0, chunkSize);
        }
    }
}
