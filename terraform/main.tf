provider "aws" {
  region  = "${var.region}"
}

resource "aws_iam_role" "iam_for_lambda" {
  name = "iam_for_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_lambda_permission" "allow_bucket" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.func.arn}"
  principal     = "s3.amazonaws.com"
  source_arn    = "${aws_s3_bucket.source_bucket.arn}"
}

resource "aws_lambda_function" "func" {
  filename      = "../target/lambda-kakadu-converter-0.0.1-SNAPSHOT.jar"
  function_name = "KakaduConverter"
  role          = "${aws_iam_role.iam_for_lambda.arn}"
  handler       = "edu.ucla.library.lambda.kakadu.converter.KakaduConverter"
  runtime       = "java8"
}

resource "aws_s3_bucket" "source_bucket" {
  bucket = "${var.source_bucket_name}"
  region = "${var.region}"
}

resource "aws_s3_bucket" "dest_bucket" {
  bucket = "${var.dest_bucket_name}"
  region = "${var.region}"
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = "${aws_s3_bucket.source_bucket.id}"

  lambda_function {
    lambda_function_arn = "${aws_lambda_function.func.arn}"
    events              = ["s3:ObjectCreated:*"]
    filter_prefix       = "AWSLogs/"
    filter_suffix       = ".log"
  }
}
