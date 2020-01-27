
package edu.ucla.library.lambda.kakadu.converter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class KakaduTest {

    private static final File TEST_FILE = new File("src/test/resources/logback-test.xml");

    private static final File TEST_TIFF = new File("src/test/resources/images/test.tif");

    private String myID;

    /**
     * Sets up our test.
     */
    @Before
    public void setup() {
        myID = UUID.randomUUID().toString();
    }

    /**
     * Tests a valid use of convert().
     *
     * @throws InterruptedException If the process has been interrupted
     * @throws IOException If there is trouble reading or writing a file
     */
    @Test
    public final void testConvert() throws InterruptedException, IOException {
        try {
            new Kakadu().convert(myID, TEST_TIFF, "5").deleteOnExit();
        } catch (final IOException details) {
            // If we get to this point (of not finding kakadu) it's worked
        }
    }

    /**
     * Tests passing a negative float to convert().
     *
     * @throws InterruptedException If the process has been interrupted
     * @throws IOException If there is trouble reading or writing a file
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testConvertNegative() throws InterruptedException, IOException {
        new Kakadu().convert(myID, TEST_TIFF, "-5").deleteOnExit();
    }

    /**
     * Tests passing a zero float to convert().
     *
     * @throws InterruptedException If the process has been interrupted
     * @throws IOException If there is trouble reading or writing a file
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testConvertZero() throws InterruptedException, IOException {
        new Kakadu().convert(myID, TEST_TIFF, "0").deleteOnExit();
    }

    /**
     * Tests that the supplied file has the expected absolute path.
     *
     * @throws IOException If the supplied file couldn't be found
     */
    @Test
    public final void testGetPath() throws IOException {
        assertEquals(TEST_FILE.getAbsolutePath(), new Kakadu().getPath(TEST_FILE));
    }

    /**
     * Tests that a missing file supplied to getPath() throws an exception.
     *
     * @throws IOException If the supplied file couldn't be found
     */
    @Test(expected = IOException.class)
    public final void testGetPathNotFound() throws IOException {
        new Kakadu().getPath(new File("src/test/resources/this/should/fail"));
    }

}
