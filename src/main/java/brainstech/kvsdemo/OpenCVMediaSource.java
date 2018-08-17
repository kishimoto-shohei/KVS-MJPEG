package brainstech.kvsdemo;

import com.amazonaws.kinesisvideo.client.mediasource.MediaSource;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceSink;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceState;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.mediasource.OnFrameDataAvailable;
import com.amazonaws.kinesisvideo.producer.KinesisVideoFrame;

import com.amazonaws.kinesisvideo.producer.StreamInfo;
import com.amazonaws.kinesisvideo.producer.Tag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

import static com.amazonaws.kinesisvideo.producer.Time.HUNDREDS_OF_NANOS_IN_A_MILLISECOND;
import static com.amazonaws.kinesisvideo.producer.Time.HUNDREDS_OF_NANOS_IN_A_SECOND;
import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.*;
import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.RECALCULATE_METRICS;

public class OpenCVMediaSource implements MediaSource {

    private static final long HUNDREDS_OF_NANOS_IN_MS = 10 * 1000;
    private static final int FRAME_FLAG_KEY_FRAME = 1;
    private static final int FRAME_FLAG_NONE = 0;
    private static final long FRAME_DURATION_20_MS = 20L;

    private MediaSourceState mediaSourceState;
    private MediaSourceSink mediaSourceSink;
    private OpenCVMediaSourceConfiguration openCVMediaSourceConfiguration;
    private OpenCVFrameSource openCVFrameSource;
    private int frameIndex;

    private final Log log = LogFactory.getLog(OpenCVMediaSource.class);

    private static final byte[] AVCC_EXTRA_DATA = {
            (byte) 0x01,
            (byte) 0x64, (byte) 0x00, (byte) 0x28,
            (byte) 0xff, (byte) 0xe1, (byte) 0x00,
            (byte) 0x0e,
            (byte) 0x27, (byte) 0x64, (byte) 0x00, (byte) 0x28, (byte) 0xac, (byte) 0x2b, (byte) 0x40, (byte) 0x50,
            (byte) 0x1e, (byte) 0xd0, (byte) 0x0f, (byte) 0x12, (byte) 0x26, (byte) 0xa0, (byte) 0x01, (byte) 0x00,
            (byte) 0x04, (byte) 0x28, (byte) 0xee, (byte) 0x1f, (byte) 0x2c};

    @Override
    public MediaSourceState getMediaSourceState() {
        return mediaSourceState;
    }

    @Override
    public MediaSourceConfiguration getConfiguration() { return openCVMediaSourceConfiguration; }

    @Override
    public void initialize(@Nonnull final MediaSourceSink mediaSourceSink) throws KinesisVideoException {
        this.mediaSourceSink = mediaSourceSink;
    }

    @Override
    public void configure(final MediaSourceConfiguration configuration) {
        if (!(configuration instanceof OpenCVMediaSourceConfiguration)) {
            throw new IllegalStateException("Configuration must be an instance of OpenCvMediaSourceConfiguration");
        }
        this.openCVMediaSourceConfiguration = (OpenCVMediaSourceConfiguration) configuration;
        this.frameIndex = 0;
    }


    @Override
    public void start() throws KinesisVideoException {
        mediaSourceState = MediaSourceState.RUNNING;
        openCVFrameSource = new OpenCVFrameSource(openCVMediaSourceConfiguration);
        openCVFrameSource.onBytesAvailable(createKinesisVideoFrameAndPushToProducer());
        openCVFrameSource.start();
    }

    @Override
    public void stop() throws KinesisVideoException {
        if (openCVFrameSource != null) {
            openCVFrameSource.stop();
        }
        mediaSourceState = MediaSourceState.STOPPED;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public void free() throws KinesisVideoException {

    }

    private OnFrameDataAvailable createKinesisVideoFrameAndPushToProducer() {
        return new OnFrameDataAvailable() {
            @Override
            public void onFrameDataAvailable(final ByteBuffer data) {
                final long currentTimeMs = System.currentTimeMillis();

                final int flags = isKeyFrame()
                        ? FRAME_FLAG_KEY_FRAME
                        : FRAME_FLAG_NONE;

                final KinesisVideoFrame frame = new KinesisVideoFrame(
                        frameIndex++,
                        flags,
                        currentTimeMs * HUNDREDS_OF_NANOS_IN_MS,
                        currentTimeMs * HUNDREDS_OF_NANOS_IN_MS,
                        FRAME_DURATION_20_MS * HUNDREDS_OF_NANOS_IN_MS,
                        data);

                if (frame.getSize() == 0) {
                    return;
                }

                putFrame(frame);
            }
        };
    }

    private boolean isKeyFrame() {
        return frameIndex % openCVMediaSourceConfiguration.getFps() == 0;
    }

    private void putFrame(final KinesisVideoFrame kinesisVideoFrame) {
        try {
            mediaSourceSink.onFrame(kinesisVideoFrame);
        } catch (final KinesisVideoException ex) {
            log.error("Failed to put frame with Exception", ex);
        }
    }

    @Override
    public MediaSourceSink getMediaSourceSink() {
        return mediaSourceSink;
    }

    @Override
    public StreamInfo getStreamInfo(final String streamName) {
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
                ABSOLUTE_TIMECODES,
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
                new Tag[] {
                        new Tag("device", "Test Device"),
                        new Tag("stream", "Test Stream") },
                StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_NALS);
    }
}
