package com.dunctebot.lavalinkplugin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plugins.dunctebot")
public class DuncteBotConfig {
    private String ttsLanguage = "en-AU";

    public String getTtsLanguage() {
        return ttsLanguage;
    }

    public void setTtsLanguage(String ttsLanguage) {
        this.ttsLanguage = ttsLanguage;
    }

    @Component
    @ConfigurationProperties(prefix = "plugins.dunctebot.sources")
    public static class Sources {
        private boolean getyarn = true;
        private boolean clypit = true;
        private boolean tts = true;
        private boolean pornhub = true;
        private boolean reddit = true;
        private boolean ocremix = true;
        private boolean tiktok = true;

        public boolean isGetyarn() {
            return getyarn;
        }

        public void setGetyarn(boolean getyarn) {
            this.getyarn = getyarn;
        }

        public boolean isClypit() {
            return clypit;
        }

        public void setClypit(boolean clypit) {
            this.clypit = clypit;
        }

        public boolean isTts() {
            return tts;
        }

        public void setTts(boolean tts) {
            this.tts = tts;
        }

        public boolean isPornhub() {
            return pornhub;
        }

        public void setPornhub(boolean pornhub) {
            this.pornhub = pornhub;
        }

        public boolean isReddit() {
            return reddit;
        }

        public void setReddit(boolean reddit) {
            this.reddit = reddit;
        }

        public boolean isOcremix() {
            return ocremix;
        }

        public void setOcremix(boolean ocremix) {
            this.ocremix = ocremix;
        }

        public boolean isTiktok() {
            return tiktok;
        }

        public void setTiktok(boolean tiktok) {
            this.tiktok = tiktok;
        }
    }
}