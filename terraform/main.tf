#
# A Terraform configuration for instantiating our Lambda function and its related resources
#

provider "aws" {
  region  = "${var.region}"
  profile = "${var.cred_profile}"
}

# TODO: Most of the IAM rules could use tightening up

# IAM role for Lambda to assume
resource "aws_iam_role" "iam_for_lambda" {
  name = "iam_for_lambda"
  assume_role_policy = "${data.aws_iam_policy_document.lambda-assume-policy-document.json}"
}

data "aws_iam_policy_document" "lambda-assume-policy-document" {
  statement {
    actions = ["sts:AssumeRole"]
 
    principals {
      type = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

# Adding IAM policy to IAM role
resource "aws_iam_role_policy" "iam_policy_for_lambda" {
  name = "iam-policy-for-lambda"
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy = "${data.aws_iam_policy_document.iam_policy_document_for_lambda.json}"
}
 
data "aws_iam_policy_document" "iam_policy_document_for_lambda" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "arn:aws:logs:*:*:*"
    ]
  }

  statement {
    actions = ["s3:*"]
    resources = [
       "arn:aws:s3:::*"
#      "arn:aws:s3:::${var.jp2_bucket_name}",
#      "arn:aws:s3:::${var.src_bucket_name}"
    ]
  }
}

# Create S3 bucket permission
resource "aws_lambda_permission" "allow_bucket" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.kakadu_converter.arn}"
  principal     = "s3.amazonaws.com"
  source_arn    = "${aws_s3_bucket.source_bucket.arn}"
}

# Create CloudWatch log group using the name Lambda expects
resource "aws_cloudwatch_log_group" "kakadu-converter-log-group" {
  name              = "/aws/lambda/${aws_lambda_function.kakadu_converter.function_name}"
  retention_in_days = 14
}

# Create converter Lambda function
resource "aws_lambda_function" "kakadu_converter" {
  filename          = "../target/${var.project_artifactId}-${var.project_version}.jar"
  # FIXME? Doesn't seem to be a way to self-reference filename for source_code_hash(?)
  source_code_hash  = "${filebase64sha256("../target/${var.project_artifactId}-${var.project_version}.jar")}"
  function_name     = "KakaduConverter"
  role              = "${aws_iam_role.iam_for_lambda.arn}"
  handler           = "edu.ucla.library.lambda.kakadu.converter.KakaduConverter"
  runtime           = "java8"
  memory_size       = "1024"
  timeout           = "600"
  layers            = ["${var.kakadu_bin_layer}","${var.kakadu_lib_layer}"]

  environment {
    variables = {
      DESTINATION_BUCKET = "${var.jp2_bucket_name}"
      MONITORING_ENDPOINT = "${var.monitoring_endpoint}"
    }
  }
}

# Create TIFF S3 bucket
resource "aws_s3_bucket" "source_bucket" {
  bucket        = "${var.src_bucket_name}"
  region        = "${var.region}"
  force_destroy = "${var.force_destroy_src_bucket}"
}

# Create JP2 S3 bucket
resource "aws_s3_bucket" "jp2_bucket" {
  bucket        = "${var.jp2_bucket_name}"
  region        = "${var.region}"
  force_destroy = "${var.force_destroy_jp2_bucket}"
}

# Create TIFF bucket notification for converter Lambda
resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = "${aws_s3_bucket.source_bucket.id}"

  lambda_function {
    lambda_function_arn = "${aws_lambda_function.kakadu_converter.arn}"
    events              = ["s3:ObjectCreated:*"]
    filter_suffix       = ".tif"
  }
}
