package brainstech.kvsdemo;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSource;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;

import com.amazonaws.kinesisvideo.java.client.KinesisVideoJavaClientFactory;
import com.amazonaws.regions.Regions;

public class OpenCVDemo {

    private static String videoSrc ="http://tccam.miemasu.net:7011/nphMotionJpeg";
    private static String user = "nwcadmin";
    private static String pass = "passwd34";
    private static String streamName = "test";
    private static boolean run = true;

    public static void main(final String[] args) {
        try {
            // create Kinesis Video high level client
            final KinesisVideoClient kinesisVideoClient = KinesisVideoJavaClientFactory
                    .createKinesisVideoClient(
                            Regions.US_WEST_2,
                            new SystemPropertiesCredentialsProvider());

            // create a media source. this class produces the data and pushes it into
            // Kinesis Video Producer lower level components
            final MediaSource openCVMediaSource = createOpenCVMediaSource(videoSrc,user,pass);

            // register media source with Kinesis Video Client
            kinesisVideoClient.registerMediaSource(streamName, openCVMediaSource);

            // start streaming
            openCVMediaSource.start();

        } catch (final KinesisVideoException e) {
            throw new RuntimeException(e);
        }
    }

    private static MediaSource createOpenCVMediaSource(String videoSrc,String user,String pass) {

        final MediaSourceConfiguration configuration =
                new OpenCVMediaSourceConfiguration.Builder()
                        .fps(5)
                        .videoSrc(videoSrc)
                        .user(user)
                        .pass(pass)
                        .build();
        final OpenCVMediaSource mediaSource = new OpenCVMediaSource();
        mediaSource.configure(configuration);

        return mediaSource;
    }
}

