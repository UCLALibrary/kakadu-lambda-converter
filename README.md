# kakadu-lambda-converter &nbsp;[![Build Status](https://travis-ci.com/UCLALibrary/kakadu-lambda-converter.svg?branch=master)](https://travis-ci.com/UCLALibrary/kakadu-lambda-converter) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/83adb954344644a2ac6fbb8ecd022cd9)](https://www.codacy.com/app/UCLALibrary/kakadu-lambda-converter?utm_source=github.com&utm_medium=referral&utm_content=UCLALibrary/kakadu-lambda-converter&utm_campaign=Badge_Coverage) [![Known Vulnerabilities](https://snyk.io/test/github/uclalibrary/kakadu-lambda-converter/badge.svg)](https://snyk.io/test/github/uclalibrary/kakadu-lambda-converter)

The kakadu-lambda-converter function reads TIFFs from an S3 bucket, converts them into JP2s using Kakadu, and then pushes the JP2s into a different S3 bucket. Since kakadu is proprietary software, you will need a license from Kakadu Software. If you have that license, you can use the [kakadu-lambda-layer](https://github.com/UCLALibrary/kakadu-lambda-layer) to make kakadu available to your AWS Lambda functions. The kakadu-lambda-layer is a prerequisite for this project.

### Prerequisites

In order to deploy successfully, you'll need the following prerequisites:

* Terraform Enterprise (Free/Paid) Account
  * Create an organization or use your own account name as your organization
* Terraform Enterprise (Free Tier) Token
  * Generate a user token from: https://app.terraform.io/app/settings/tokens
* The [Terraform software](https://www.terraform.io/downloads.html) installed on your machine.

You will also need to override some default properties that are specified in the pom.xml file. These can be set using the Maven [settings.xml](https://maven.apache.org/settings.html) file or by passing the values on the command line when you run the build (e.g., `mvn -Dlambda.cred.profile="converter-profile" -Dlambda.region="us-east-1" -Dsrc.s3.bucket="source-bucket-name" -Djp2.s3.bucket="jp2-bucket-name" package`). You shouldn't need to change the values in the pom.xml file itself.

* Required POM override values (Examples shown)
  * terraform.workspace.prefix:
  
      ```joebruin```

  * terraform.organization.name:

      ```examplecom```

  * terraform.user.token

      ```Fgdsgkj29gbxMw.atlasv1.gdslkgjdlsljlkjl32l590gdsljlk10909dslj5l1209gdsgjdslkgjJyf4bJhXyeSE```

  * lambda.region

      ```us-east-1```

  * lambda.cred_profile

      ```converter-profile```

  * src.s3.bucket

      ```joebruin-src-bucket```

  * jp2.s3.bucket

      ```joebruin-output-bucket```

  * kakadu.lib.layer.versioned.arn

      ```arn:aws:lambda:us-west-2:0123456789:layer:img2lambda-sha256-d89d9gd987239879gdsgdsg469a2735b7539c89b03c9821284ad3fc6e20aa502:1```

  * kakadu.bin.layer.versioned.arn

      ```arn:aws:lambda:us-west-2:0123456789:layer:img2lambda-sha256-d89d9gd987239879gdsgdsg469a2735b7539c89b03c9821284ad3fc6e20aa502:1```

* Optional POM override values (Examples shown)
  * lambda.function.name
  
      ```joebruin-lambda-converter-two```

    There is a default value that works for single use cases, but it can be overridden if you want to deploy more than one kakadu-lambda-converter in the same AWS region.

  * force.destroy.src.bucket
  
      ```true```

    Setting to true will wipe the S3 source bucket and all its contents.

  * force.destroy.jp2.bucket

      ```true```

    Setting to true will wipe the S3 target bucket and all its contents.

### Building the project

Once the required properties have been supplied, the kakadu-lambda-converter function can be built using the standard Maven mechanism:

    mvn package

If you've chosen to override the required properties on the command line instead of in the settings.xml file, your command line will be much longer, of course.

### Initial deployment

In order to run the Lambda function, several AWS resources need to be created. We've provided a Terraform configuration to do this. The credentials needed to create these resources need to be available in your `~/.aws/credentials` file. Once you have the necessary credentials available and configured in your environment, you can run Terraform with the following steps:

    cd terraform
    bin/run

This will create all the AWS resources that you need and upload the Lambda function contained in the Maven built Jar file. You can also clean up all these resource by using a bin script from the same `terraform` directory:

    bin/run destroy

If you want to see what Terraform is going to do before it actually creates the resources, run the following before running `bin/run deploy`:

    bin/check_plan

That's it! That will install your Lambda function for you.

### Contact

If you have any questions, feel free to contact <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>. If you find a bug or would like to make a suggestion about the project, feel free to open a ticket in the project's [issues queue](https://github.com/UCLALibrary/kakadu-lambda-converter/issues).