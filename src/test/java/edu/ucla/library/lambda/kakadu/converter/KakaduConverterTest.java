
package edu.ucla.library.lambda.kakadu.converter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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

import info.freelibrary.util.FileUtils;

/**
 * A simple test harness for locally invoking the Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class KakaduConverterTest {

    private static final String TIFF_FILE_NAME = "test.tif";

    private static final String JP2_FILE_NAME = "test.jpx";

    private static final String CONTENT_TYPE = "image/tif";

    private S3Event myEvent;

    private File myJP2File;

    @Mock
    private AmazonS3 myS3Client;

    @Mock
    private S3Object myS3Object;

    @Mock
    private Kakadu myKakadu;

    @Captor
    private ArgumentCaptor<GetObjectRequest> myGetObjectRequest;

    @Before
    public void setUp() throws IOException, InterruptedException {
        myEvent = TestUtils.parse("/s3-event.put.json", S3Event.class);

        final File jp2FileSource = new File("src/test/resources/images/" + JP2_FILE_NAME);
        final File testTiff = new File("src/test/resources/images/" + TIFF_FILE_NAME);
        final FileInputStream fileStream = new FileInputStream(testTiff);
        final S3ObjectInputStream inputStream = new S3ObjectInputStream(fileStream, new HttpGet());
        final ObjectMetadata objectMetadata = new ObjectMetadata();

        myJP2File = new File(System.getProperty("java.io.tmpdir"), JP2_FILE_NAME);
        FileUtils.copy(jp2FileSource, myJP2File);

        objectMetadata.setContentType(CONTENT_TYPE);
        objectMetadata.setContentLength(testTiff.length());

        when(myS3Object.getObjectContent()).thenReturn(inputStream);
        when(myS3Object.getObjectMetadata()).thenReturn(objectMetadata);
        when(myKakadu.isInstalled()).thenReturn(true);
        when(myS3Client.getObject(myGetObjectRequest.capture())).thenReturn(myS3Object);
        when(myKakadu.convert(any(String.class), ArgumentMatchers.any(File.class), any(Conversion.class))).thenReturn(
                myJP2File);
    }

    private Context createContext() {
        final TestContext context = new TestContext();

        context.setFunctionName(KakaduConverter.class.getSimpleName());

        return context;
    }

    @Test
    public void testKakaduConverter() {
        final KakaduConverter handler = new KakaduConverter(myS3Client, myKakadu);
        final Context context = createContext();

        assertTrue(myJP2File.exists());
        assertTrue(handler.handleRequest(myEvent, context));
        assertFalse(myJP2File.exists());
    }
}
