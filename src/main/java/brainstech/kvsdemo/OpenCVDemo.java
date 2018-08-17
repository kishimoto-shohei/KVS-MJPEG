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

    public static void main(final String[] args) {

        try {
            if(args.length == 4){
                videoSrc = args[0];
                user = args[1];
                pass = args[2];
                streamName = args[3];
            }

            KinesisVideoClient kinesisVideoClient = KinesisVideoJavaClientFactory
                    .createKinesisVideoClient(
                            Regions.US_WEST_2,
                            new SystemPropertiesCredentialsProvider());

            MediaSource openCVMediaSource = createOpenCVMediaSource(videoSrc,user,pass);

            kinesisVideoClient.registerMediaSource(streamName, openCVMediaSource);

            openCVMediaSource.start();

        } catch (final KinesisVideoException e) {
            throw new RuntimeException(e);
        }
    }

    private static MediaSource createOpenCVMediaSource(String videoSrc,String user,String pass) {

        MediaSourceConfiguration configuration =
                new OpenCVMediaSourceConfiguration.Builder()
                        .fps(5)
                        .videoSrc(videoSrc)
                        .user(user)
                        .pass(pass)
                        .build();

        OpenCVMediaSource mediaSource = new OpenCVMediaSource();
        mediaSource.configure(configuration);

        return mediaSource;
    }
}

