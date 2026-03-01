import com.dunctebot.sourcemanagers.tumblr.TumblrAudioSourceManager;

public class TumblrTest {
    public static void main(String[] args) throws Exception {
        final var consumerKey = System.getenv("OAUTH_CONSUMER_KEY");
        final var secretKey = System.getenv("OAUTH_SECRET_KEY");

        final var mngr = new TumblrAudioSourceManager(consumerKey, secretKey);

        final var token = mngr.fetchOAuth2Token();

        System.out.println("Token found " + token);

        final var npfData = mngr.fetchPostData("pukicho", "801421495209476096");

        System.out.println(npfData.getMediaUrl());
    }
}
