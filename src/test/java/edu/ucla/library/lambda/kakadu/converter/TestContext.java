
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

    public void setAwsRequestId(String aValue) {
        myAWSRequestId = aValue;
    }

    @Override
    public ClientContext getClientContext() {
        return myClientContext;
    }

    public void setClientContext(ClientContext aValue) {
        myClientContext = aValue;
    }

    @Override
    public String getFunctionName() {
        return myFunctionName;
    }

    public void setFunctionName(String aValue) {
        myFunctionName = aValue;
    }

    @Override
    public CognitoIdentity getIdentity() {
        return myIdentity;
    }

    public void setIdentity(CognitoIdentity aValue) {
        myIdentity = aValue;
    }

    @Override
    public String getLogGroupName() {
        return myLogGroupName;
    }

    public void setLogGroupName(String aValue) {
        myLogGroupName = aValue;
    }

    @Override
    public String getLogStreamName() {
        return myLogStreamName;
    }

    public void setLogStreamName(String aValue) {
        myLogStreamName = aValue;
    }

    @Override
    public LambdaLogger getLogger() {
        return myLogger;
    }

    public void setLogger(LambdaLogger aValue) {
        myLogger = aValue;
    }

    @Override
    public int getMemoryLimitInMB() {
        return myMemoryLimitInMB;
    }

    public void setMemoryLimitInMB(int aValue) {
        myMemoryLimitInMB = aValue;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return myRemainingTimeInMillis;
    }

    public void setRemainingTimeInMillis(int aValue) {
        myRemainingTimeInMillis = aValue;
    }

    @Override
    public String getFunctionVersion() {
        return myFunctionVersion;
    }

    public void setFunctionVersion(String aValue) {
        myFunctionVersion = aValue;
    }

    @Override
    public String getInvokedFunctionArn() {
        return myInvokedFunctionArn;
    }

    public void setInvokedFunctionArn(String aValue) {
        myInvokedFunctionArn = aValue;
    }

    /**
     * A simple {@code LambdaLogger} that prints everything to INFO.
     */
    private static class TestLogger implements LambdaLogger {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestLogger.class, Constants.MESSAGES);

        @Override
        public void log(String aMessage) {
            LOGGER.info(aMessage);
        }

        @Override
        public void log(byte[] aMessage) {
            LOGGER.info(MessageCodes.LKC_005, aMessage.length);
        }
    }
}
