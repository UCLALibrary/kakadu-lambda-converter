
package edu.ucla.library.lambda.kakadu.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

/**
 * An AWS Lambda handler that converts TIFF images in S3 into JP2 images in S3.
 */
public class KakaduConverter implements RequestHandler<S3Event, Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KakaduConverter.class, Constants.MESSAGES);

    private static final String JP2_EXT = ".jp2";

    /* A S3 client for the test to use */
    private AmazonS3 myS3Client = AmazonS3ClientBuilder.standard().build();

    /* The kakadu image converter */
    private final Kakadu myKakadu;

    /**
     * Creates a Kakadu converter.
     */
    public KakaduConverter() {
        myKakadu = new Kakadu();
    }

    // Test purpose only.
    KakaduConverter(final AmazonS3 aS3Client, final Kakadu aKakadu) {
        myS3Client = aS3Client;
        myKakadu = aKakadu;
    }

    @Override
    public Boolean handleRequest(final S3Event aEvent, final Context aContext) {
        if (LOGGER.isDebugEnabled()) {
            final ObjectMapper mapper = new ObjectMapper();
            final Object jsonObj;

            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                jsonObj = mapper.readValue(aEvent.toJson(), Object.class);

                LOGGER.debug(MessageCodes.LKC_001, mapper.writeValueAsString(jsonObj));
            } catch (final IOException details) {
                LOGGER.debug(details.getMessage(), details);
            }
        }

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
            final long contentLength = objectBytes.length;
            final FileOutputStream fileOutStream;
            final File tiffFile;
            final File jp2File;

            IOUtils.closeQuietly(s3ObjStream);

            if (expectedByteCount == contentLength) {
                LOGGER.debug(MessageCodes.LKC_004, expectedByteCount, contentLength);

                if (myKakadu.isInstalled()) {
                    final String ext = "." + FileUtils.getExt(key);

                    // Get id from metadata if it has it; else, use the file name minus extension as ID
                    String id = s3Metadata.getUserMetaDataOf(Constants.ID);

                    if (id == null) {
                        id = FileUtils.stripExt(key);
                    }

                    tiffFile = new File(Kakadu.TMP_DIR, new File(id + ext).getName());

                    try {
                        fileOutStream = new FileOutputStream(tiffFile);
                        fileOutStream.write(objectBytes);
                        IOUtils.closeQuietly(fileOutStream);

                        try {
                            jp2File = myKakadu.convert(id, tiffFile, Conversion.LOSSLESS);

                            if (jp2File.length() > 0) {
                                return uploadImage(id, jp2File, contentType, contentLength);
                            } else {
                                LOGGER.error(MessageCodes.LKC_009, jp2File);
                                return Boolean.FALSE;
                            }
                        } catch (final IOException | InterruptedException details) {
                            LOGGER.error(details, MessageCodes.LKC_111, tiffFile);
                            return Boolean.FALSE;
                        }
                    } catch (final IOException details) {
                        LOGGER.error(details, MessageCodes.LKC_010, id, tiffFile);
                        return Boolean.FALSE;
                    }
                } else {
                    // The exception is already logged in method: isInstalled()
                    return Boolean.FALSE;
                }
            } else {
                LOGGER.error(MessageCodes.LKC_004, expectedByteCount, contentLength);
                return Boolean.FALSE;
            }
        } catch (final IOException details) {
            LOGGER.error(details, MessageCodes.LKC_003, key, bucket);
            return Boolean.FALSE;
        }
    }

    private boolean uploadImage(final String aID, final File aJP2File, final String aContentType,
            final long aContentLength) {
        final String jp2Bucket = System.getenv(Constants.DESTINATION_BUCKET);
        final String jp2S3Key = aID + JP2_EXT;
        final PutObjectRequest request = new PutObjectRequest(jp2Bucket, jp2S3Key, aJP2File);
        final ObjectMetadata metadata = new ObjectMetadata();

        metadata.setContentType(aContentType);
        metadata.setContentLength(aContentLength);
        request.setMetadata(metadata);

        try {
            myS3Client.putObject(request);

            LOGGER.info(MessageCodes.LKC_008, aID, jp2Bucket, jp2S3Key);

            return Boolean.TRUE;
        } catch (final AmazonServiceException details) {
            LOGGER.error(details, details.getMessage());
            return Boolean.FALSE;
        } catch (final SdkClientException details) {
            LOGGER.error(details, details.getMessage());
            return Boolean.FALSE;
        }
    }

}
