# kakadu-lambda-converter &nbsp; [![Build Status](https://travis-ci.com/UCLALibrary/kakadu-lambda-converter.svg?branch=master)](https://travis-ci.com/UCLALibrary/kakadu-lambda-converter) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/83adb954344644a2ac6fbb8ecd022cd9)](https://www.codacy.com/app/UCLALibrary/kakadu-lambda-converter?utm_source=github.com&utm_medium=referral&utm_content=UCLALibrary/kakadu-lambda-converter&utm_campaign=Badge_Coverage)

The kakadu-lambda-converter function reads TIFFs from an S3 bucket, converts them into JP2s using Kakadu, and then pushes the JP2s into a different S3 bucket. Since kakadu is proprietary software, you will need a license from Kakadu Software. If you have that license, you can use the [kakadu-lambda-layer](https://github.com/UCLALibrary/kakadu-lambda-layer) to make kakadu available to your AWS Lambda functions. The kakadu-lambda-layer is a prerequisite for this project.

### Building the project

The kakadu-lambda-converter function can be built using the standard Maven mechanism:

    mvn package

Before you do that though, you'll want to set three properties: `lambda.region`, `src.s3.bucket`, and `jp2.s3.bucket`. These can be set at the point of building the project using the Maven [settings.xml](https://maven.apache.org/settings.html) file or by passing the values to the build on the command line (e.g., `mvn -Dlambda.region="us-east-1" -Dsrc.s3.bucket="source-bucket-name" -Djp2.s3.bucket="jp2-bucket-name" package`). When you've supplied values for those properties and built the project you will have a Jar file that can then be uploaded to AWS Lambda.

### Initial deployment

In order to run the Lambda function, several AWS resources need to be created. We've provided a simple Terraform configuration to do this. The credentials needed to create these resources need to be available in your `~/.aws/credentials` file. Once you have the necessary credentials available, you can run Terraform with the following steps:

    cd terraform
    bin/deploy.sh

This will create all the AWS resources that you need and upload the Lambda function contained in the Maven built Jar file. You can also clean up all these resource by using a bin script from the same `terraform` directory:

    bin/destroy.sh

If you want to see what Terraform is going to do before it actually creates the resources, run the following before running `bin/deploy.sh`:

    bin/check_plan.sh

That's it! That will install your Lambda function for you. But what if you want to make changes to the code and then redeploy the function?

### Incremental deployments

If you want to push out changes to the Lambda function without changing all the other resources that have been created in the AWS space, you can use Maven to do a new build and push of the resulting Jar artifact. Do do this, just run the following from the project root:

    mvn package aws:deployLambda

This will push a newly built version of the function to AWS Lambda and trigger a rebuild and refresh of the service that the Lambda function provides.

### Contact

If you have any questions, feel free to contact <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>. If you find a bug or would like to make a suggestion about the project, feel free to open a ticket in the project's [issues queue](https://github.com/UCLALibrary/kakadu-lambda-converter/issues).
