#### AWS Provider
region                                = "${lambda.region}"
cred_profile                          = "${lambda.cred.profile}"

#### S3 Source Bucket
src_bucket_name                       = "${src.s3.bucket}"
src_bucket_region                     = "${lambda.region}"
force_destroy_src_bucket              = "${force.destroy.src.bucket}"

#### S3 Source Bucket Trigger
src_bucket_notification_filter_suffix = "${src.bucket.notification.filter.suffix}"
src_bucket_event                      = ["s3:ObjectCreated:*"]

#### S3 JP2 Bucket
jp2_bucket_name                       = "${jp2.s3.bucket}"
jp2_bucket_region                     = "${lambda.region}"
force_destroy_jp2_bucket              = "${force.destroy.jp2.bucket}"

#### Kakadu Lambda IAM
cloudwatch_iam_allowed_actions        = ["logs:CreateLogGroup","logs:CreateLogStream","logs:PutLogEvents"]
lambda_iam_allowed_actions            = ["s3:*"]

#### Kakada Lambda
kakadu_bin_layer                      = "${kakadu.bin.layer.versioned.arn}"
kakadu_lib_layer                      = "${kakadu.lib.layer.versioned.arn}"
project_artifactId                    = "${project.artifactId}"
project_version                       = "${project.version}"
kakadu_handler                        = "edu.ucla.library.lambda.kakadu.converter.KakaduConverter"
kakadu_runtime                        = "java8"
kakadu_timeout                        = "600"
monitoring_endpoint                   = "${monitoring.endpoint}"

