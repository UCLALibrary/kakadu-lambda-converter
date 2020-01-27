
package edu.ucla.library.lambda.kakadu.converter;

import java.util.Objects;
import java.util.Optional;

import info.freelibrary.util.StringUtils;

public class StatusUpdate {

    public static final String DEFAULT_JOB_NAME = "default";

    private String myJobName;

    private String myImageId;

    private boolean mySuccess;

    /**
     * Creates a new status update using the default job name. This might be because we were unable to learn the job
     * name for the image that was attempted to be updated. Or, if may just be that the image doesn't have a job name
     * associated with it.
     *
     * @param aImageId The image ID whose status is being updated
     */
    public StatusUpdate(final String aImageId) {
        this(Optional.empty(), aImageId, false);
    }

    /**
     * Creates a new status update using the default job name. This might be because we were unable to learn the job
     * name for the image that was attempted to be updated. Or, if may just be that the image doesn't have a job name
     * associated with it.
     *
     * @param aJobName A job name associated with the update
     * @param aImageId The image ID whose status is being updated
     */
    public StatusUpdate(final Optional<String> aJobName, final String aImageId) {
        this(aJobName, aImageId, false);
    }

    /**
     * Creates a new status update using the default job name. This might be because we were unable to learn the job
     * name for the image that was attempted to be updated. Or, if may just be that the image doesn't have a job name
     * associated with it.
     *
     * @param aImageId The image ID whose status is being updated
     * @param aSuccess The status information
     */
    public StatusUpdate(final String aImageId, final boolean aSuccess) {
        this(Optional.empty(), aImageId, aSuccess);
    }

    /**
     * Creates a new status update.
     *
     * @param aJobName A job name associated with the update
     * @param aImageId The ID of the image whose status is being updated
     * @param aSuccess The status information
     */
    public StatusUpdate(final Optional<String> aJobName, final String aImageId, final boolean aSuccess) {
        myJobName = aJobName.isPresent() ? aJobName.get() : DEFAULT_JOB_NAME;
        myImageId = aImageId;
        mySuccess = aSuccess;
    }

    /**
     * Sets the job name associated with the status update.
     *
     * @param aJobName The job name associated with the status update
     * @return The status update
     */
    public StatusUpdate setJobName(final String aJobName) {
        Objects.requireNonNull(aJobName);
        myJobName = aJobName;
        return this;
    }

    /**
     * Gets the job name associated with the status update.
     *
     * @return The job name associated with the status update
     */
    public String getJobName() {
        return myJobName;
    }

    /**
     * Sets the status update's image ID.
     *
     * @param aImageId The image ID of the status update
     * @return The status update
     */
    public StatusUpdate setImageId(final String aImageId) {
        myImageId = aImageId;
        return this;
    }

    /**
     * Gets the status update's image ID.
     *
     * @return The image ID of the status update
     */
    public String getImageId() {
        return myImageId;
    }

    /**
     * Sets whether the image conversion was successful.
     *
     * @param aSuccess Whether the conversion was successful
     * @return The status update
     */
    public StatusUpdate setSuccess(final boolean aSuccess) {
        mySuccess = aSuccess;
        return this;
    }

    /**
     * Gets whether the image conversion was successful.
     *
     * @return Whether the conversion was successful
     */
    public boolean getSuccess() {
        return mySuccess;
    }

    /**
     * Returns a string representation of the status update.
     *
     * @return A string representation of the status update
     */
    @Override
    public String toString() {
        final String jobName = myJobName != null ? myJobName : DEFAULT_JOB_NAME;
        return StringUtils.format("[Job Name: {}] [Image ID: {}] [Success: {}]", jobName, myImageId, mySuccess);
    }
}
