package brainstech.kvsdemo;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSource;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;

import com.amazonaws.kinesisvideo.java.client.KinesisVideoJavaClientFactory;
import com.amazonaws.regions.Regions;

public class OpenCVDemo {

    private static String videoSrc ;
    private static String user ;
    private static String pass ;
    private static String streamName ;
    private staitc String region = "us-west-1";
    private static int fps = 5;

    public static void main(final String[] args) {

        try {
            videoSrc = args[0];
            user = args[1];
            pass = args[2];
            streamName = args[3];
            if(args.length > 4){
                region = args[4];
                if(args.length > 5){
                    fps = Integer.parseInt(args[5]);
                }
            }
            

            KinesisVideoClient kinesisVideoClient = KinesisVideoJavaClientFactory
                    .createKinesisVideoClient(
                            Regions.fromName(region),
                            new SystemPropertiesCredentialsProvider());

            MediaSource openCVMediaSource = createOpenCVMediaSource(videoSrc,user,pass,fps);

            kinesisVideoClient.registerMediaSource(streamName, openCVMediaSource);

            openCVMediaSource.start();

        } catch (final KinesisVideoException e) {
            throw new RuntimeException(e);
        }
    }

    private static MediaSource createOpenCVMediaSource(String videoSrc,String user,String pass,int fps) {

        MediaSourceConfiguration configuration =
                new OpenCVMediaSourceConfiguration.Builder()
                        .fps(fps)
                        .videoSrc(videoSrc)
                        .user(user)
                        .pass(pass)
                        .build();

        OpenCVMediaSource mediaSource = new OpenCVMediaSource();
        mediaSource.configure(configuration);

        return mediaSource;
    }
}

