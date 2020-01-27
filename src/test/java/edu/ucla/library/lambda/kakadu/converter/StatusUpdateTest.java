
package edu.ucla.library.lambda.kakadu.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import info.freelibrary.util.StringUtils;

/**
 * Tests for StatusUpdateTest.
 */
public class StatusUpdateTest {

    private static final String PATTERN = "[Job Name: {}] [Image ID: {}] [Success: {}]";

    private static final String JOB_NAME = "asdf";

    private static final String NEW_JOB_NAME = "fdsa";

    private static final String IMAGE_ID = "aaaa";

    private static final String NEW_IMAGE_ID = "bbbb";

    /**
     * Test for StatusUpdate(String).
     */
    @Test
    public final void testStatusUpdateString() {
        final String expected = StringUtils.format(PATTERN, StatusUpdate.DEFAULT_JOB_NAME, IMAGE_ID, false);
        assertEquals(expected, new StatusUpdate(IMAGE_ID).toString());
    }

    /**
     * Test for StatusUpdate(String, Boolean).
     */
    @Test
    public final void testStatusUpdateStringBooleanTrue() {
        final String expected = StringUtils.format(PATTERN, StatusUpdate.DEFAULT_JOB_NAME, IMAGE_ID, true);
        assertEquals(expected, new StatusUpdate(IMAGE_ID, true).toString());
    }

    /**
     * Test for StatusUpdate(String, Boolean)
     */
    @Test
    public final void testStatusUpdateStringBooleanFalse() {
        final String expected = StringUtils.format(PATTERN, StatusUpdate.DEFAULT_JOB_NAME, IMAGE_ID, false);
        assertEquals(expected, new StatusUpdate(IMAGE_ID, false).toString());
    }

    /**
     * Test for StatusUpdate(Optional&lt;String&gt;, String, Boolean)
     */
    @Test
    public final void testStatusUpdateOptionalOfStringStringBoolean() {
        final String expected = StringUtils.format(PATTERN, JOB_NAME, IMAGE_ID, true);
        assertEquals(expected, new StatusUpdate(Optional.of(JOB_NAME), IMAGE_ID, true).toString());
    }

    /**
     * Test for StatusUpdate(Optional&lt;String&gt;, String, Boolean)
     */
    @Test
    public final void testStatusUpdateEmptyOptionalOfStringStringBoolean() {
        final String expected = StringUtils.format(PATTERN, StatusUpdate.DEFAULT_JOB_NAME, IMAGE_ID, true);
        assertEquals(expected, new StatusUpdate(Optional.ofNullable(null), IMAGE_ID, true).toString());
    }

    /**
     * Test for setJobName()
     */
    @Test
    public final void testSetJobName() {
        final Optional<String> jobName = Optional.of(JOB_NAME);
        assertEquals(NEW_JOB_NAME, new StatusUpdate(jobName, IMAGE_ID, true).setJobName(NEW_JOB_NAME).getJobName());
    }

    /**
     * Test for getJobName()
     */
    @Test
    public final void testGetJobName() {
        final Optional<String> jobName = Optional.of(JOB_NAME);
        assertEquals(JOB_NAME, new StatusUpdate(jobName, IMAGE_ID, true).getJobName());
    }

    /**
     * Test for setImageId()
     */
    @Test
    public final void testSetImageId() {
        assertEquals(NEW_IMAGE_ID, new StatusUpdate(IMAGE_ID, true).setImageId(NEW_IMAGE_ID).getImageId());
    }

    /**
     * Test for getImageId()
     */
    @Test
    public final void testGetImageId() {
        assertEquals(IMAGE_ID, new StatusUpdate(IMAGE_ID, true).getImageId());
    }

    /**
     * Test for setSuccess()
     */
    @Test
    public final void testSetSuccess() {
        assertTrue(new StatusUpdate(IMAGE_ID, true).getSuccess());
    }

    /**
     * Test for getSuccess()
     */
    @Test
    public final void testGetSuccess() {
        assertFalse(new StatusUpdate(IMAGE_ID, false).getSuccess());
    }

}
