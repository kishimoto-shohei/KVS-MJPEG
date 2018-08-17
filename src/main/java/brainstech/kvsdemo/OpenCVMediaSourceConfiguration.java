package brainstech.kvsdemo;

import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;

public class OpenCVMediaSourceConfiguration implements MediaSourceConfiguration {

    private final int fps;
    private final String videoSrc;
    private final String user;
    private final String pass;

    public OpenCVMediaSourceConfiguration(final OpenCVMediaSourceConfiguration.Builder builder) {
        this.fps = builder.fps;
        this.videoSrc = builder.videoSrc;
        this.user = builder.user;
        this.pass = builder.pass;
    }

    @Override
    public String getMediaSourceType() {
        return null;
    }

    @Override
    public String getMediaSourceDescription() {
        return null;
    }

    public int getFps() {
        return fps;
    }

    public String getVideoSrc() { return videoSrc; }

    public String getUser() { return user; }

    public String getPass() { return pass; }

    public static class Builder implements MediaSourceConfiguration.Builder<OpenCVMediaSourceConfiguration> {
        private int fps;
        private String videoSrc;
        private String user;
        private String pass;


        public Builder fps(final int fps) {
            this.fps = fps;
            if (fps <= 0) {
                throw new IllegalArgumentException("Fps should not be negative or zero.");
            }
            return this;
        }

        public Builder videoSrc(final String videoSrc) {
            this.videoSrc = videoSrc;
            return this;
        }


        public Builder user(final String user) {
            this.user = user;
            return this;
        }


        public Builder pass(final String pass) {
            this.pass = pass;
            return this;
        }

        @Override
        public OpenCVMediaSourceConfiguration build() {
            return new OpenCVMediaSourceConfiguration(this);
        }
    }

}
