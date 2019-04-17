
package edu.ucla.library.lambda.kakadu.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * An AWS Lambda handler that converts TIFF images in S3 into JP2 images in S3.
 */
public class KakaduConverter implements RequestHandler<S3Event, Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KakaduConverter.class, Constants.MESSAGES);

    /* A S3 client for the test to use */
    private AmazonS3 myS3Client = AmazonS3ClientBuilder.standard().build();

    /**
     * Creates a Kakadu converter.
     */
    public KakaduConverter() {
    }

    // Test purpose only.
    KakaduConverter(final AmazonS3 aS3Client) {
        myS3Client = aS3Client;
    }

    @Override
    public Boolean handleRequest(final S3Event aEvent, final Context aContext) {
        LOGGER.debug(MessageCodes.LKC_001, aEvent.toJson());

        final String bucket = aEvent.getRecords().get(0).getS3().getBucket().getName();
        final String key = aEvent.getRecords().get(0).getS3().getObject().getKey();

        try {
            final S3Object s3Object = myS3Client.getObject(new GetObjectRequest(bucket, key));
            final String contentType = s3Object.getObjectMetadata().getContentType();

            LOGGER.info(MessageCodes.LKC_002, bucket, key, contentType);

            final S3ObjectInputStream s3ObjStream = s3Object.getObjectContent();
            final ObjectMetadata s3Metadata = s3Object.getObjectMetadata();
            final byte[] objectBytes = IOUtils.readBytes(s3ObjStream);
            final long expectedByteCount = s3Metadata.getContentLength();
            final long actualByteCount = objectBytes.length;

            IOUtils.closeQuietly(s3ObjStream);

            if (expectedByteCount == actualByteCount) {
                LOGGER.debug(MessageCodes.LKC_004, expectedByteCount, actualByteCount);

                return convertImage();
            } else {
                LOGGER.error(MessageCodes.LKC_004, expectedByteCount, actualByteCount);
                return Boolean.FALSE;
            }
        } catch (final Exception details) {
            LOGGER.error(details, MessageCodes.LKC_003, key, bucket);
            return Boolean.FALSE;
        }
    }

    private boolean convertImage() {
        final ProcessBuilder processBuilder = new ProcessBuilder("/opt/kakadu/kdu_compress", "-v");
        final BufferedReader inStream;
        final Process process;

        processBuilder.redirectErrorStream(true);

        try {
            final StringBuilder buffer = new StringBuilder();
            String line;

            process = processBuilder.start();
            inStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = inStream.readLine()) != null) {
                buffer.append(line);
            }

            LOGGER.debug(buffer.toString());

            return process.waitFor() == 0 ? true : false;
        } catch (final IOException | InterruptedException details) {
            LOGGER.error(details, details.getMessage());

            return false;
        }
    }
}
