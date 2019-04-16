
package edu.ucla.library.lambda.kakadu.converter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * A simple test harness for locally invoking the Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class KakaduConverterTest {

    private static final String FILE_NAME = "test.tif";

    private static final String CONTENT_TYPE = "image/tif";

    private S3Event myEvent;

    @Mock
    private AmazonS3 myS3Client;

    @Mock
    private S3Object myS3Object;

    @Captor
    private ArgumentCaptor<GetObjectRequest> myGetObjectRequest;

    @Before
    public void setUp() throws IOException {
        myEvent = TestUtils.parse("/s3-event.put.json", S3Event.class);

        final File testTiff = new File("src/test/resources/images/" + FILE_NAME);
        final FileInputStream fileStream = new FileInputStream(testTiff);
        final S3ObjectInputStream inputStream = new S3ObjectInputStream(fileStream, new HttpGet());
        final ObjectMetadata objectMetadata = new ObjectMetadata();

        objectMetadata.setContentType(CONTENT_TYPE);
        objectMetadata.setContentLength(testTiff.length());

        when(myS3Object.getObjectContent()).thenReturn(inputStream);
        when(myS3Object.getObjectMetadata()).thenReturn(objectMetadata);
        when(myS3Client.getObject(myGetObjectRequest.capture())).thenReturn(myS3Object);
    }

    private Context createContext() {
        final TestContext context = new TestContext();

        context.setFunctionName(KakaduConverter.class.getSimpleName());

        return context;
    }

    @Test
    public void testKakaduConverter() {
        final KakaduConverter handler = new KakaduConverter(myS3Client);
        final Context context = createContext();

        assertTrue(handler.handleRequest(myEvent, context));
    }
}
