package brainstech.kvsdemo;

import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.producer.StreamInfo;
import com.amazonaws.kinesisvideo.producer.Tag;

import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.kinesisvideo.producer.Time.HUNDREDS_OF_NANOS_IN_AN_HOUR;
import static com.amazonaws.kinesisvideo.producer.Time.HUNDREDS_OF_NANOS_IN_A_MILLISECOND;
import static com.amazonaws.kinesisvideo.producer.Time.HUNDREDS_OF_NANOS_IN_A_SECOND;

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

    private static final boolean NOT_ADAPTIVE = false;
    private static final boolean KEYFRAME_FRAGMENTATION = true;
    private static final boolean SDK_GENERATES_TIMECODES = false;
    private static final boolean RELATIVE_FRAGMENT_TIMECODES = false;
    private static final String NO_KMS_KEY_ID = null;
    private static final int VERSION_ZERO = 0;
    private static final long MAX_LATENCY_ZERO = 0L;
    private static final long RETENTION_ONE_HOUR = 1L * HUNDREDS_OF_NANOS_IN_AN_HOUR;
    private static final boolean REQUEST_FRAGMENT_ACKS = true;
    private static final boolean RECOVER_ON_FAILURE = true;
    private static final long DEFAULT_GOP_DURATION = 2000L * HUNDREDS_OF_NANOS_IN_A_SECOND;
    private static final int DEFAULT_BITRATE = 2_000_000;
    private static final int DEFAULT_TIMESCALE = 10_000;
    private static final int FRAME_RATE_25 = 25;
    private static final boolean USE_FRAME_TIMECODES = true;
    private static final boolean RELATIVE_TIMECODES = false;
    private static final boolean RECALCULATE_METRICS = true;
    // CHECKSTYLE:SUPPRESS:LineLength
    private static final byte[] AVCC_EXTRA_DATA = {
            (byte) 0x01,
            (byte) 0x64, (byte) 0x00, (byte) 0x28,
            (byte) 0xff, (byte) 0xe1, (byte) 0x00,
            (byte) 0x0e,
            (byte) 0x27, (byte) 0x64, (byte) 0x00, (byte) 0x28, (byte) 0xac, (byte) 0x2b, (byte) 0x40, (byte) 0x50, (byte) 0x1e, (byte) 0xd0, (byte) 0x0f, (byte) 0x12, (byte) 0x26, (byte) 0xa0, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x28, (byte) 0xee, (byte) 0x1f, (byte) 0x2c};
    /**
     * Default buffer duration for a stream
     */
    public static final long DEFAULT_BUFFER_DURATION_IN_SECONDS = 40;

    /**
     * Default replay duration for a stream
     */
    public static final long DEFAULT_REPLAY_DURATION_IN_SECONDS = 20;

    /**
     * Default connection staleness detection duration.
     */
    public static final long DEFAULT_STALENESS_DURATION_IN_SECONDS = 20;

    public StreamInfo toStreamInfo(final String streamName) {
        return new StreamInfo(VERSION_ZERO,
                streamName,
                StreamInfo.StreamingType.STREAMING_TYPE_REALTIME,
                "video/h264",
                NO_KMS_KEY_ID,
                RETENTION_ONE_HOUR,
                NOT_ADAPTIVE,
                MAX_LATENCY_ZERO,
                DEFAULT_GOP_DURATION * HUNDREDS_OF_NANOS_IN_A_MILLISECOND,
                KEYFRAME_FRAGMENTATION,
                USE_FRAME_TIMECODES,
                RELATIVE_TIMECODES,
                REQUEST_FRAGMENT_ACKS,
                RECOVER_ON_FAILURE,
                "V_MPEG4/ISO/AVC",
                "we-did-it",
                DEFAULT_BITRATE,
                FRAME_RATE_25,
                DEFAULT_BUFFER_DURATION_IN_SECONDS * HUNDREDS_OF_NANOS_IN_A_SECOND,
                DEFAULT_REPLAY_DURATION_IN_SECONDS * HUNDREDS_OF_NANOS_IN_A_SECOND,
                DEFAULT_STALENESS_DURATION_IN_SECONDS * HUNDREDS_OF_NANOS_IN_A_SECOND,
                DEFAULT_TIMESCALE,
                RECALCULATE_METRICS,
                AVCC_EXTRA_DATA,
                getTags(),
                /*
                 * Here we have the CPD hardcoded in AVCC format already, hence no need to adapt NAL.
                 */
                StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_NALS);
    }

    private static Tag[] getTags() {
        final List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("device", "Test Device"));
        tagList.add(new Tag("stream", "Test Stream"));
        return tagList.toArray(new Tag[0]);
    }
}
