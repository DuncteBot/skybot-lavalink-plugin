import com.dunctebot.sourcemanagers.pixeldrain.PixeldrainAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;

public class PixeldrainTest {

    public static void main(String[] args) {
        final var id1 = "https://pixeldrain.com/u/WUJkkH3F";
        final var mngr = new PixeldrainAudioSourceManager();
        final var res1 = mngr.loadItem(null, new AudioReference(id1, ""));

        System.out.println(res1);
    }
}
