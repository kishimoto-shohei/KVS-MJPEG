package brainstech.kvsdemo;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSource;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;

import com.amazonaws.kinesisvideo.java.client.KinesisVideoJavaClientFactory;
import com.amazonaws.regions.Regions;

public class OpenCVDemo {

    private static String videoSrc;
    private static String user;
    private static String pass;
    private static String streamName;
    private static String region;

    public static void main(final String[] args) {

        try {
            if(args.length == 5){
                videoSrc = args[0];
                user = args[1];
                pass = args[2];
                streamName = args[3];
                region = args[4];
            }

            KinesisVideoClient kinesisVideoClient = KinesisVideoJavaClientFactory
                    .createKinesisVideoClient(
                            Regions.fromName(region),
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

