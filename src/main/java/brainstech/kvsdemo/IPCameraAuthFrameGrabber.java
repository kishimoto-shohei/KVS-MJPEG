package brainstech.kvsdemo;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.io.EOFException;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.IplImage;

import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;

/**
 * IPCAM for auth URL
 */
public class IPCameraAuthFrameGrabber extends FrameGrabber {

    private final FrameConverter converter;
    private final URL url;
    private final int connectionTimeout;
    private final int readTimeout;
    private final String user;
    private final String pass;
    private DataInputStream input;
    private byte[] pixelBuffer;
    private IplImage decoded;

    public IPCameraAuthFrameGrabber(URL url,String user,String pass,int startTimeout, int grabTimeout, TimeUnit timeUnit) {
        this.converter = new ToIplImage();
        this.pixelBuffer = new byte[1024];
        this.decoded = null;
        this.user = user;
        this.pass = pass;
        if (url == null) {
            throw new IllegalArgumentException("URL can not be null");
        } else {
            this.url = url;
            if (timeUnit != null) {
                this.connectionTimeout = toIntExact(TimeUnit.MILLISECONDS.convert((long)startTimeout, timeUnit));
                this.readTimeout = toIntExact(TimeUnit.MILLISECONDS.convert((long)grabTimeout, timeUnit));
            } else {
                this.connectionTimeout = -1;
                this.readTimeout = -1;
            }

        }
    }


    public IPCameraAuthFrameGrabber(String urlstr,String user,String  pass) throws MalformedURLException {
        this(new URL(urlstr), user, pass, -1, -1, null);
    }

    public void start() throws Exception {
        try {
            URLConnection connection = this.url.openConnection();
            if (this.connectionTimeout >= 0) {
                connection.setConnectTimeout(this.connectionTimeout);
            }

            // 本当はここだけ継承して実装したい。
            setRequestProperty(connection);

            if (this.readTimeout >= 0) {
                connection.setReadTimeout(this.readTimeout);
            }

            this.input = new DataInputStream(connection.getInputStream());
        } catch (IOException var2) {
            throw new Exception(var2.getMessage(), var2);
        }
    }

    protected void setRequestProperty(URLConnection connection){
        if(user != null) {
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes()));
        }
    }

    public void stop() throws Exception {
        if (this.input != null) {
            try {
                this.input.close();
            } catch (IOException var5) {
                throw new Exception(var5.getMessage(), var5);
            } finally {
                this.input = null;
                this.releaseDecoded();
            }
        }
    }

    public void trigger() {
    }

    public Frame grab() throws Exception {
        try {
            byte[] b = this.readImage();
            CvMat mat = opencv_core.cvMat(1, b.length, opencv_core.CV_8UC1, new BytePointer(b));
            this.releaseDecoded();
            return this.converter.convert(this.decoded = opencv_imgcodecs.cvDecodeImage(mat));
        } catch (IOException var3) {
            throw new Exception(var3.getMessage(), var3);
        }
    }

    private void releaseDecoded() {
        if (this.decoded != null) {
            opencv_core.cvReleaseImage(this.decoded);
            this.decoded = null;
        }

    }

    private byte[] readImage() throws IOException {
        StringBuffer sb = new StringBuffer();

        int c;
        while((c = this.input.read()) != -1) {
            if (c > 0) {
                sb.append((char)c);
                if (c == 13) {
                    sb.append((char)this.input.read());
                    c = this.input.read();
                    sb.append((char)c);
                    if (c == 13) {
                        sb.append((char)this.input.read());
                        break;
                    }
                }
            }
        }

        String subheader = sb.toString().toLowerCase();
        int c0 = subheader.indexOf("content-length: ");
        int c1 = subheader.indexOf(13, c0);
        if (c0 < 0) {
            throw new EOFException("The camera stream ended unexpectedly");
        } else {
            c0 += 16;
            int contentLength = Integer.parseInt(subheader.substring(c0, c1).trim());
            this.ensureBufferCapacity(contentLength);
            this.input.readFully(this.pixelBuffer, 0, contentLength);
            this.input.read();
            this.input.read();
            this.input.read();
            this.input.read();
            return this.pixelBuffer;
        }
    }

    public void release() {
    }

    private void ensureBufferCapacity(int desiredCapacity) {
        int capacity;
        for(capacity = this.pixelBuffer.length; capacity < desiredCapacity; capacity *= 2) {
        }

        if (capacity > this.pixelBuffer.length) {
            this.pixelBuffer = new byte[capacity];
        }

    }

    private static int toIntExact(long value) {
        if ((long)((int)value) != value) {
            throw new ArithmeticException("integer overflow");
        } else {
            return (int)value;
        }
    }
}
