# kakadu-lambda-converter &nbsp;[![Build Status](https://travis-ci.com/UCLALibrary/kakadu-lambda-converter.svg?branch=master)](https://travis-ci.com/UCLALibrary/kakadu-lambda-converter) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/83adb954344644a2ac6fbb8ecd022cd9)](https://www.codacy.com/app/UCLALibrary/kakadu-lambda-converter?utm_source=github.com&utm_medium=referral&utm_content=UCLALibrary/kakadu-lambda-converter&utm_campaign=Badge_Coverage) [![Known Vulnerabilities](https://snyk.io/test/github/uclalibrary/kakadu-lambda-converter/badge.svg)](https://snyk.io/test/github/uclalibrary/kakadu-lambda-converter)

The kakadu-lambda-converter function reads TIFFs from an S3 bucket, converts them into JP2s using Kakadu, and then pushes the JP2s into a different S3 bucket. Since kakadu is proprietary software, you will need a license from Kakadu Software. If you have that license, you can use the [kakadu-lambda-layer](https://github.com/UCLALibrary/kakadu-lambda-layer) to make kakadu available to your AWS Lambda functions. The kakadu-lambda-layer is a prerequisite for this project.

### Required Attributes Needed
* Terraform Enterprise(Free/Paid) Account
  * Create an organization or use your own account name as your organization
* Terraform Enterprise(Free Tier) Token
  * Generate a user token from: https://app.terraform.io/app/settings/tokens
* Required POM override values (Examples shown)
  * terraform.workspace.prefix
    joebruin
  * terraform.organization.name
    * Example: ExampleCom
  * terraform.user.token
    * Example: Fgdsgkj29gbxMw.atlasv1.gdslkgjdlsljlkjl32l590gdsljlk10909dslj5l1209gdsgjdslkgjJyf4bJhXyeSE
  * lambda.function.name
    * Example: joebruin-lambda-converter
  * src.s3.bucket
    * Example: joebruin-src-bucket
  * jp2.s3.bucket
    * Example: joebruin-output-bucket
  * kakadu.lib.layer.versioned.arn
    * Example: arn:aws:lambda:us-west-2:0123456789:layer:img2lambda-sha256-d89d9gd987239879gdsgdsg469a2735b7539c89b03c9821284ad3fc6e20aa502:1
  * kakadu.bin.layer.versioned.arn
    * Example: arn:aws:lambda:us-west-2:0123456789:layer:img2lambda-sha256-d89d9gd987239879gdsgdsg469a2735b7539c89b03c9821284ad3fc6e20aa502:1

### Building the project

The kakadu-lambda-converter function can be built using the standard Maven mechanism:

    mvn package

Before you do that though, you'll want to set four properties: `lambda.cred.profile`, `lambda.region`, `src.s3.bucket`, and `jp2.s3.bucket`. These can be set at the point of building the project using the Maven [settings.xml](https://maven.apache.org/settings.html) file or by passing the values to the build on the command line (e.g., `mvn -Dlambda.cred.profile="converter-profile" -Dlambda.region="us-east-1" -Dsrc.s3.bucket="source-bucket-name" -Djp2.s3.bucket="jp2-bucket-name" package`). When you've supplied values for these properties and built the project, you will have a Jar file that can then be uploaded to AWS Lambda.

The `lambda.cred.profile`, in case it's not clear, refers to an AWS profile defined in your user's `~/.aws/credentials` file. It should be a profile that has the privileges necessary to create resources within your AWS account.

### Initial deployment

In order to run the Lambda function, several AWS resources need to be created. We've provided a simple Terraform configuration to do this. The credentials needed to create these resources need to be available in your `~/.aws/credentials` file. Once you have the necessary credentials available, you can run Terraform with the following steps:

    cd terraform
    bin/run

This will create all the AWS resources that you need and upload the Lambda function contained in the Maven built Jar file. You can also clean up all these resource by using a bin script from the same `terraform` directory:

    bin/run destroy

If you want to see what Terraform is going to do before it actually creates the resources, run the following before running `bin/deploy.sh`:

    bin/check_plan.sh

That's it! That will install your Lambda function for you. But what if you want to make changes to the code and then redeploy the function?

### Incremental deployments

If you want to push out changes to the Lambda function without changing all the other resources that have been created in the AWS space, you can use Maven to do a new build and push of the resulting Jar artifact. Do do this, just run the following from the project root:

    mvn package aws:deployLambda

This will push a newly built version of the function to AWS Lambda and trigger a rebuild and refresh of the service that the Lambda function provides.

### Contact

If you have any questions, feel free to contact <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>. If you find a bug or would like to make a suggestion about the project, feel free to open a ticket in the project's [issues queue](https://github.com/UCLALibrary/kakadu-lambda-converter/issues).
