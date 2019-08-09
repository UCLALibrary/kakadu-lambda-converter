
package edu.ucla.library.lambda.kakadu.converter;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A simple mock implementation of the {@code Context} interface. Default values are stubbed out, and setters are
 * provided so you can customize the context before passing it to your function.
 */
public class TestContext implements Context {

    private String myAWSRequestId = "test-aws-request-id";

    private ClientContext myClientContext;

    private String myFunctionName = "test-function-name";

    private CognitoIdentity myIdentity;

    private String myLogGroupName = "test-log-group-name";

    private String myLogStreamName = "test-log-stream-name";

    private LambdaLogger myLogger = new TestLogger();

    private int myMemoryLimitInMB = 128;

    private int myRemainingTimeInMillis = 15000;

    private String myFunctionVersion = "0.0.0-test";

    private String myInvokedFunctionArn = "test-invoked-function-arn";

    @Override
    public String getAwsRequestId() {
        return myAWSRequestId;
    }

    /**
     * Sets AWS request ID
     *
     * @param aAWSRequestId An AWS request ID
     */
    public void setAwsRequestId(final String aAWSRequestId) {
        myAWSRequestId = aAWSRequestId;
    }

    @Override
    public ClientContext getClientContext() {
        return myClientContext;
    }

    /**
     * Sets client context.
     *
     * @param aClientContext A client context
     */
    public void setClientContext(final ClientContext aClientContext) {
        myClientContext = aClientContext;
    }

    @Override
    public String getFunctionName() {
        return myFunctionName;
    }

    /**
     * Sets functional name.
     *
     * @param aFunctionalName A functional name
     */
    public void setFunctionName(final String aFunctionalName) {
        myFunctionName = aFunctionalName;
    }

    @Override
    public CognitoIdentity getIdentity() {
        return myIdentity;
    }

    /**
     * Sets identity.
     *
     * @param aCognitoIdentity A cognito identity
     */
    public void setIdentity(final CognitoIdentity aCognitoIdentity) {
        myIdentity = aCognitoIdentity;
    }

    @Override
    public String getLogGroupName() {
        return myLogGroupName;
    }

    /**
     * Sets log group name.
     *
     * @param aLogGroupName A log group name
     */
    public void setLogGroupName(final String aLogGroupName) {
        myLogGroupName = aLogGroupName;
    }

    @Override
    public String getLogStreamName() {
        return myLogStreamName;
    }

    /**
     * Sets log stream name.
     *
     * @param aLogStreamName A log stream name
     */
    public void setLogStreamName(final String aLogStreamName) {
        myLogStreamName = aLogStreamName;
    }

    @Override
    public LambdaLogger getLogger() {
        return myLogger;
    }

    /**
     * Sets the lambda logger.
     *
     * @param aLogger A lambda logger
     */
    public void setLogger(final LambdaLogger aLogger) {
        myLogger = aLogger;
    }

    /**
     * Gets the memory limit in megabytes.
     *
     * @return The memory limit in megabytes
     */
    @Override
    public int getMemoryLimitInMB() {
        return myMemoryLimitInMB;
    }

    /**
     * Sets memory limit in megabytes.
     *
     * @param aMemLimitInMB A memory limit in megabytes
     */
    public void setMemoryLimitInMB(final int aMemLimitInMB) {
        myMemoryLimitInMB = aMemLimitInMB;
    }

    /**
     * Gets remaining time in milliseconds.
     *
     * @return A remaining time in milliseconds
     */
    @Override
    public int getRemainingTimeInMillis() {
        return myRemainingTimeInMillis;
    }

    /**
     * Sets remaining time in milliseconds.
     *
     * @param aTimeInMillis A milliseconds value
     */
    public void setRemainingTimeInMillis(final int aTimeInMillis) {
        myRemainingTimeInMillis = aTimeInMillis;
    }

    /**
     * Gets functional version.
     *
     * @return A functional version
     */
    @Override
    public String getFunctionVersion() {
        return myFunctionVersion;
    }

    /**
     * Sets functional version.
     *
     * @param aVersion A version
     */
    public void setFunctionVersion(final String aVersion) {
        myFunctionVersion = aVersion;
    }

    /**
     * Gets invoked functional ARN.
     *
     * @return invoked functional ARN
     */
    @Override
    public String getInvokedFunctionArn() {
        return myInvokedFunctionArn;
    }

    /**
     * Sets invoked functional ARN.
     *
     * @param aValue An ARN value
     */
    public void setInvokedFunctionArn(final String aValue) {
        myInvokedFunctionArn = aValue;
    }

    /**
     * A simple {@code LambdaLogger} that prints everything to INFO.
     */
    private static class TestLogger implements LambdaLogger {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestLogger.class, Constants.MESSAGES);

        @Override
        public void log(final String aMessage) {
            LOGGER.info(aMessage);
        }

        @Override
        public void log(final byte[] aMessage) {
            LOGGER.info(MessageCodes.LKC_005, aMessage.length);
        }
    }
}
