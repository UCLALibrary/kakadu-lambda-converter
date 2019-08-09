
package edu.ucla.library.lambda.kakadu.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.LoggerMarker;
import info.freelibrary.util.StringUtils;

/**
 * An AWS Lambda handler that converts TIFF images in S3 into JP2 images in S3.
 */
public class KakaduConverter implements RequestHandler<S3Event, Boolean> {

    /** A logger for our function */
    private static final Logger LOGGER = LoggerFactory.getLogger(KakaduConverter.class, Constants.MESSAGES);

    /* A S3 client for the test to use */
    private AmazonS3 myS3Client = AmazonS3ClientBuilder.standard().build();

    /**
     * Endpoint where something that monitors this process lives. It's expected to receive PATCH updates and to have
     * have three variable slots as a part of its URL: e.g., https://path.to.somewhere/{}/{}/{}. The job name, image
     * id, and boolean success (e.g., true or false).
     */
    private final Optional<String> myMonitoringEndpoint;

    /* The kakadu image converter */
    private final Kakadu myKakadu;

    /**
     * Creates a Kakadu converter.
     */
    public KakaduConverter() {
        myMonitoringEndpoint = Optional.ofNullable(System.getenv(Constants.MONITORING_ENDPOINT));
        myKakadu = new Kakadu();
    }

    // Test purpose only.
    KakaduConverter(final AmazonS3 aS3Client, final Kakadu aKakadu) {
        myMonitoringEndpoint = Optional.ofNullable(System.getenv(Constants.MONITORING_ENDPOINT));
        myS3Client = aS3Client;
        myKakadu = aKakadu;
    }

    @Override
    public Boolean handleRequest(final S3Event aEvent, final Context aContext) {
        if (LOGGER.isDebugEnabled()) {
            final ObjectMapper mapper = new ObjectMapper();
            final Object event;

            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                event = mapper.readValue(aEvent.toJson(), Object.class);

                // If we're running in a test mode, use a different EOL for the JSON message
                if (!"true".equals(System.getenv("CI"))) {
                    final Marker marker = MarkerFactory.getMarker(LoggerMarker.EOL_TO_CR);

                    LOGGER.debug(marker, MessageCodes.LKC_001, mapper.writeValueAsString(event));
                } else {
                    LOGGER.debug(MessageCodes.LKC_001, mapper.writeValueAsString(event));
                }
            } catch (final IOException details) {
                LOGGER.debug(details.getMessage(), details);
            }
        }

        final String bucket = aEvent.getRecords().get(0).getS3().getBucket().getName();
        final String key = aEvent.getRecords().get(0).getS3().getObject().getKey();
        final S3Object s3Object = myS3Client.getObject(new GetObjectRequest(bucket, key));
        final ObjectMetadata s3Metadata = s3Object.getObjectMetadata();
        final String jobNameMetadata = s3Metadata.getUserMetaDataOf(Constants.JOB_NAME);
        final Optional<String> jobName = Optional.ofNullable(jobNameMetadata);

        LOGGER.info(MessageCodes.LKC_002, bucket, key, s3Metadata.getContentType());

        // Get id from uploaded file's metadata if there is one available
        String id = StringUtils.trimToNull(s3Metadata.getUserMetaDataOf(Constants.ID));

        // Else, use the file name without path minus its extension as ID
        if (id == null) {
            id = FileUtils.stripExt(new File(key).getName());
        }

        if (myKakadu.isInstalled()) {
            final String ext = "." + FileUtils.getExt(key);
            final File sourceFile = new File(Kakadu.TMP_DIR, new File(id + ext).getName());

            // We've created a new temporary source file using image ID as the file name
            LOGGER.debug(MessageCodes.LKC_114, sourceFile);

            try {
                return convertFile(sourceFile, s3Object, new StatusUpdate(jobName, id));
            } catch (final FileNotFoundException details) {
                LOGGER.error(details, MessageCodes.LKC_116, sourceFile);
                notifyMonitorIfPresent(new StatusUpdate(jobName, id, false));
                return Boolean.FALSE;
            } catch (final IOException details) {
                LOGGER.error(details, MessageCodes.LKC_003, key, bucket);
                notifyMonitorIfPresent(new StatusUpdate(jobName, id, false));
                return Boolean.FALSE;
            }
        } else {
            notifyMonitorIfPresent(new StatusUpdate(jobName, id, false));
            return Boolean.FALSE;
        }
    }

    private boolean convertFile(final File aSourceFile, final S3Object aS3Object, final StatusUpdate aStatusUpdate)
            throws FileNotFoundException, IOException {
        final FileOutputStream fileOutStream = new FileOutputStream(aSourceFile);
        final String id = aStatusUpdate.getImageId();

        try {
            final S3ObjectInputStream s3ObjStream = aS3Object.getObjectContent();
            final ObjectMetadata s3Metadata = aS3Object.getObjectMetadata();
            final long contentLength = s3Metadata.getContentLength();
            final byte[] objectBytes = IOUtils.readBytes(s3ObjStream);

            IOUtils.closeQuietly(s3ObjStream);

            if (contentLength == objectBytes.length) {
                LOGGER.debug(MessageCodes.LKC_004, contentLength, objectBytes.length);

                try {
                    fileOutStream.write(objectBytes);
                } catch (final IOException details) {
                    LOGGER.error(details, MessageCodes.LKC_010, id, aSourceFile);
                    notifyMonitorIfPresent(aStatusUpdate.setSuccess(false));
                    return Boolean.FALSE;
                }

                try {
                    final File jp2File = myKakadu.convert(id, aSourceFile, Conversion.LOSSY);

                    if (jp2File.length() > 0) {
                        if (uploadImage(jp2File, s3Metadata.getContentType(), contentLength)) {
                            notifyMonitorIfPresent(aStatusUpdate.setSuccess(true));
                            return Boolean.TRUE;
                        } else {
                            notifyMonitorIfPresent(aStatusUpdate.setSuccess(false));
                            return Boolean.FALSE;
                        }
                    } else {
                        LOGGER.error(MessageCodes.LKC_009, jp2File);
                        notifyMonitorIfPresent(aStatusUpdate.setSuccess(false));
                        return Boolean.FALSE;
                    }
                } catch (final IOException | InterruptedException details) {
                    LOGGER.error(details, MessageCodes.LKC_111, aSourceFile);
                    notifyMonitorIfPresent(aStatusUpdate.setSuccess(false));
                    return Boolean.FALSE;
                }
            } else {
                LOGGER.error(MessageCodes.LKC_004, contentLength, objectBytes.length);
                notifyMonitorIfPresent(aStatusUpdate.setSuccess(false));
                return Boolean.FALSE;
            }
        } finally {
            IOUtils.closeQuietly(fileOutStream);

            if (!FileUtils.delete(aSourceFile)) {
                LOGGER.error(MessageCodes.LKC_113, aSourceFile);
            }
        }
    }

    private void notifyMonitorIfPresent(final StatusUpdate aStatusUpdate) {
        if (myMonitoringEndpoint.isPresent()) {
            final CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();

            // If we encounter problems reporting the status to our monitor, be persistent (before failing)
            try {
                if (!notifyMonitor(httpClient, aStatusUpdate)) {
                    if (!notifyMonitor(httpClient, aStatusUpdate)) {
                        LOGGER.error(MessageCodes.LKC_115, aStatusUpdate, myMonitoringEndpoint);
                    }
                }
            } catch (final IOException details) {
                try {
                    if (!notifyMonitor(httpClient, aStatusUpdate)) {
                        LOGGER.error(MessageCodes.LKC_115, aStatusUpdate, myMonitoringEndpoint);
                    }
                } catch (final IOException retryDetails) {
                    LOGGER.error(retryDetails, MessageCodes.LKC_115, aStatusUpdate, myMonitoringEndpoint);
                }
            } finally {
                try {
                    httpClient.close();
                } catch (final IOException details) {
                    LOGGER.error(details, details.getMessage());
                }
            }
        }
    }

    private boolean notifyMonitor(final CloseableHttpClient aClient, final StatusUpdate aStatusUpdate)
            throws ClientProtocolException, IOException {
        final String jobName = aStatusUpdate.getJobName();
        final String imageId = aStatusUpdate.getImageId();
        final boolean success = aStatusUpdate.getSuccess();
        final String url = StringUtils.format(myMonitoringEndpoint.get(), jobName, imageId, success);
        final CloseableHttpResponse response = aClient.execute(new HttpPatch(url));

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
            case 201:
            case 204:
                LOGGER.info(MessageCodes.LKC_117, url);
                response.close();
                return true;
            default:
                LOGGER.info(MessageCodes.LKC_118, url);
                response.close();
                return false;
        }
    }

    private boolean uploadImage(final File aJP2File, final String aContentType, final long aContentLength) {
        final String jp2Bucket = System.getenv(Constants.DESTINATION_BUCKET);
        final String jp2Key = aJP2File.getName();
        final PutObjectRequest request = new PutObjectRequest(jp2Bucket, jp2Key, aJP2File);
        final ObjectMetadata metadata = new ObjectMetadata();

        // Set the metadata that S3 wants for uploads
        metadata.setContentType(aContentType);
        metadata.setContentLength(aContentLength);
        request.setMetadata(metadata);

        try {
            myS3Client.putObject(request);

            LOGGER.info(MessageCodes.LKC_008, FileUtils.stripExt(jp2Key), jp2Bucket, jp2Key);
            return Boolean.TRUE;
        } catch (final AmazonServiceException details) {
            LOGGER.error(details, details.getMessage());
            return Boolean.FALSE;
        } catch (final SdkClientException details) {
            LOGGER.error(details, details.getMessage());
            return Boolean.FALSE;
        } finally {
            if (!FileUtils.delete(aJP2File)) {
                LOGGER.error(MessageCodes.LKC_113, aJP2File);
            }
        }
    }

}
