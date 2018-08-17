package brainstech.kvsdemo;


import com.amazonaws.kinesisvideo.auth.DefaultAuthCallbacks;
import com.amazonaws.kinesisvideo.client.AbstractKinesisVideoClient;
import com.amazonaws.kinesisvideo.client.KinesisVideoClientConfiguration;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSource;
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.common.logging.Log;
import com.amazonaws.kinesisvideo.common.logging.LogLevel;
import com.amazonaws.kinesisvideo.mediasource.ProducerStreamSink;
import com.amazonaws.kinesisvideo.producer.*;
import com.amazonaws.kinesisvideo.producer.client.KinesisVideoServiceClient;
import com.amazonaws.kinesisvideo.producer.jni.NativeKinesisVideoProducerJni;
import com.amazonaws.kinesisvideo.service.DefaultServiceCallbacksImpl;
import com.amazonaws.kinesisvideo.streaming.DefaultStreamCallbacks;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.amazonaws.kinesisvideo.common.preconditions.Preconditions.checkNotNull;


public class OpenCVKinesisVideoClient extends AbstractKinesisVideoClient {
    /**
     * Logging tag
     */
    private static final String TAG = "OpenCVKinesisVideoClient";

    /**
     * Kinesis Video producer callbacks
     */
    private final AuthCallbacks authCallbacks;
    private final StorageCallbacks storageCallbacks;
    private final StreamCallbacks streamCallbacks;
    private final DefaultServiceCallbacksImpl defaultServiceCallbacks;
    private final List<MediaSource> mediaSources;

    /**
     * Underlying Kinesis Video producer object.
     */
    private KinesisVideoProducer kinesisVideoProducer;

    public OpenCVKinesisVideoClient(
            @Nonnull final KinesisVideoClientConfiguration configuration,
            @Nonnull final KinesisVideoServiceClient serviceClient,
            @Nonnull final ScheduledExecutorService executor) {
        this(new Log(configuration.getLogChannel(), LogLevel.VERBOSE, TAG),
                configuration,
                serviceClient,
                executor);
    }

    public OpenCVKinesisVideoClient(
            @Nonnull final Log log,
            @Nonnull final KinesisVideoClientConfiguration configuration,
            @Nonnull final KinesisVideoServiceClient serviceClient,
            @Nonnull final ScheduledExecutorService executor) {
        this(log,
                new DefaultAuthCallbacks(configuration.getCredentialsProvider(),
                        executor,
                        log),
                configuration.getStorageCallbacks(),
                new DefaultServiceCallbacksImpl(log, executor, configuration, serviceClient),
                new DefaultStreamCallbacks());
    }

    public OpenCVKinesisVideoClient(
            @Nonnull final Log log,
            @Nonnull final AuthCallbacks authCallbacks,
            @Nonnull final StorageCallbacks storageCallbacks,
            @Nonnull final ServiceCallbacks serviceCallbacks,
            @Nonnull final StreamCallbacks streamCallbacks) {

        super(log);

        this.authCallbacks = checkNotNull(authCallbacks);
        this.storageCallbacks = checkNotNull(storageCallbacks);
        defaultServiceCallbacks = (DefaultServiceCallbacksImpl) checkNotNull(serviceCallbacks);
        this.streamCallbacks = checkNotNull(streamCallbacks);

        mediaSources = new ArrayList<MediaSource>();
    }

    /**
     * Initializes the client object.
     */
    @Override
    public void initialize(@Nonnull final DeviceInfo deviceInfo) throws KinesisVideoException {
        // Create the producer object
        kinesisVideoProducer = new NativeKinesisVideoProducerJni(
                authCallbacks,
                storageCallbacks,
                defaultServiceCallbacks,
                mLog);

        kinesisVideoProducer.createSync(deviceInfo);

        super.initialize(deviceInfo);
    }

    @Override
    public void registerMediaSource(final String streamName,
                                    final MediaSource mediaSource) throws KinesisVideoException {

        final StreamInfo streamInfo;
        try {
            streamInfo = (StreamInfo) mediaSource.getConfiguration()
                    .getClass().getMethod("toStreamInfo", String.class)
                    .invoke(mediaSource.getConfiguration(), streamName);
        } catch (IllegalAccessException e) {
            throw new KinesisVideoException(e);
        } catch (InvocationTargetException e) {
            throw new KinesisVideoException(e);
        } catch (NoSuchMethodException e) {
            throw new KinesisVideoException(e);
        }

        final KinesisVideoProducerStream producerStream = kinesisVideoProducer.createStreamSync(streamInfo, streamCallbacks);
        mediaSources.add(mediaSource);
        mediaSource.initialize(new ProducerStreamSink(producerStream));
        defaultServiceCallbacks.addStream(producerStream);
    }

    @Override
    public void stopAllMediaSources() throws KinesisVideoException {
        super.stopAllMediaSources();
    }

    @Override
    public MediaSource createMediaSource(final String streamName,
                                         final MediaSourceConfiguration mediaSourceConfiguration)
            throws KinesisVideoException {

        throw new KinesisVideoException("creating media sources is not implemented yet");
    }

    @Override
    public List<MediaSourceConfiguration.Builder<? extends MediaSourceConfiguration>> listSupportedConfigurations() {
        return Collections.emptyList();
    }

    @Override
    public void free() throws KinesisVideoException {
        if (isInitialized()) {
            super.free();

            defaultServiceCallbacks.free();
            kinesisVideoProducer.stopStreams();
            kinesisVideoProducer.free();

            mIsInitialized = false;
        }
    }
}

