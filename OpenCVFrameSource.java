package brainstech.kvsdemo;

import com.amazonaws.kinesisvideo.mediasource.OnFrameDataAvailable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import org.jcodec.api.transcode.PixelStore;
import org.jcodec.api.transcode.PixelStoreImpl;
import org.jcodec.api.transcode.VideoFrameWithPacket;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.VideoEncoder;
import org.jcodec.common.model.*;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenCVFrameSource {

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final OpenCVMediaSourceConfiguration configuration;

    private OnFrameDataAvailable onFrameDataAvailable;
    private boolean isRunning = false;;

    private final Log log = LogFactory.getLog(OpenCVFrameSource.class);
    private int frameCounter;

    private Transform transform = null;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private VideoEncoder videoEncoder = H264Encoder.createH264Encoder();
    private ColorSpace[] colorSpaces = videoEncoder.getSupportedColorSpaces();
    private PixelStore pixelStore = new PixelStoreImpl();
    private ThreadLocal<ByteBuffer> bufferStore = new ThreadLocal<ByteBuffer>();

    private long frameNo = 0;
    private long timestamp = 0;
    private Rational fps = Rational.R(10,1);

    public OpenCVFrameSource(OpenCVMediaSourceConfiguration configuration) {
        this.configuration = configuration;
        if(colorSpaces != null){
            transform = ColorUtil.getTransform(ColorSpace.RGB, colorSpaces[0]);
        }
    }

    public void onBytesAvailable(OnFrameDataAvailable onFrameDataAvailable) {
        this.onFrameDataAvailable = onFrameDataAvailable;
    }

    public void start() {
        if (isRunning) {
            throw new IllegalStateException("Frame source is already running");
        }
        isRunning = true;
        startFrameGenerator();
    }

    public void stop() {
        isRunning = false;
        stopFrameGenerator();
    }

    private void startFrameGenerator() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                generateFrameAndNotifyListener();
            }
        });
    }

    private void stopFrameGenerator() {
        executor.shutdown();
    }

    private void generateFrameAndNotifyListener() {

        Java2DFrameConverter converter = new Java2DFrameConverter();
        FrameGrabber grabber = null;
        try {
            grabber = new IPCameraAuthFrameGrabber(
                    configuration.getVideoSrc(),
                    configuration.getUser(),
                    configuration.getPass());
            grabber.start();
        } catch (Exception e) {
            log.error("connect Exception ", e);
        }
        double frameRate = grabber.getFrameRate();
        if(frameRate == 0){
            frameRate = 10;
        }
        long wait = (long) (1000 / (frameRate == 0 ? 10 : frameRate));

        while (isRunning) {
            Frame cvFrame = null;
            try {
                cvFrame = grabber.grab();
            } catch (FrameGrabber.Exception e) {
                log.error("Frame read Exception ", e);
            }
            if (cvFrame != null && onFrameDataAvailable != null) {
                onFrameDataAvailable.onFrameDataAvailable(createKinesisVideoFrameFromImage(cvFrame));
            }
            try {
                Thread.sleep(Duration.ofSeconds(1L).toMillis() / fps.getDen());
            } catch (final InterruptedException e) {
                log.error("Frame interval wait interrupted by Exception ", e);
            }
        }
    }

    private ByteBuffer createKinesisVideoFrameFromImage(Frame cvFrame) {

        BufferedImage image = converter.convert(cvFrame);
        Picture pic = Picture.create(image.getWidth(), image.getHeight(), ColorSpace.RGB);
        byte[] dstData = pic.getPlaneData(0);
        int off = 0;
        for(int i = 0; i < image.getHeight(); ++i) {
            for(int j = 0; j < image.getWidth(); ++j) {
                int rgb1 = image.getRGB(j, i);
                dstData[off++] = (byte)((rgb1 >> 16 & 255) - 128);
                dstData[off++] = (byte)((rgb1 >> 8 & 255) - 128);
                dstData[off++] = (byte)((rgb1 & 255) - 128);
            }
        }
        ColorSpace sinkColor = colorSpaces[0];
        PixelStore.LoanerPicture toEncode;
        if (sinkColor != null) {
            toEncode = pixelStore.getPicture(pic.getWidth(), pic.getHeight(), sinkColor);
            transform.transform(pic, toEncode.getPicture());
        } else {
            toEncode = new PixelStore.LoanerPicture(pic, 0);
        }
        TapeTimecode tapeTimecode = TapeTimecode.tapeTimecode(frameNo,false,10);
        Packet.FrameType type = Packet.FrameType.KEY;
        Packet pkt = Packet.createPacket(
                (ByteBuffer)null,
                timestamp,
                fps.getNum(),
                (long)fps.getDen(),
                frameNo,
                type,
                tapeTimecode);

        Packet outputVideoPacket;
        ByteBuffer buffer = bufferStore.get();
        VideoFrameWithPacket videoFrame = new VideoFrameWithPacket(pkt, toEncode);
        int bufferSize = videoEncoder.estimateBufferSize(videoFrame.getFrame().getPicture());
        if (buffer == null || bufferSize < buffer.capacity()) {
            buffer = ByteBuffer.allocate(bufferSize);
            bufferStore.set(buffer);
        }
        buffer.clear();
        Picture frame = videoFrame.getFrame().getPicture();
        VideoEncoder.EncodedFrame enc = videoEncoder.encodeFrame(frame, buffer);
        ByteBuffer tmp = enc.getData().duplicate();
        frameNo++;
        timestamp += fps.getDen();
        return tmp;
    }

}
