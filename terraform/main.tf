terraform {
  backend "remote" {}
}

provider "aws" {
  profile                 = "${var.cred_profile}"
  region                  = "${var.region}"
}

module "kakadu_converter_s3_tiff_bucket" {
  source        = "git::https://github.com/UCLALibrary/aws_terraform_s3_module.git"
  bucket_name   = "${var.src_bucket_name}"
  bucket_region = "${var.src_bucket_region}"
}

module "kakadu_converter_lambda_tiff" {
  source = "git::https://github.com/UCLALibrary/aws_terraform_lambda_module.git"

  ## KakaduConverter lambda role setup
  cloudwatch_iam_allowed_actions = "${var.kakadu_converter_cloudwatch_permissions}"
  s3_iam_allowed_actions         = "${var.kakadu_converter_s3_permissions}"
  s3_iam_allowed_resources       = "${var.kakadu_converter_s3_buckets}"

  ## KakaduConverter lambda function specification
  app_artifact      = "${local.artifact_path}"
  app_name          = "${var.function_name}"
  app_layers        = "${local.lambda_layers}"
  app_handler       = "${var.kakadu_converter_handler}"
  app_filter_suffix = "${var.kakadu_converter_filter_suffix}"
  app_runtime       = "${var.kakadu_converter_runtime}"
  app_memory_size   = "${var.kakadu_converter_memory_size}"
  app_timeout       = "${var.kakadu_converter_timeout}"
  app_environment_variables = "${var.kakadu_converter_environment_variables}"

  ## KakaduConverter S3 bucket notification settings
  bucket_event = "${var.kakadu_converter_bucket_event}"
  trigger_s3_bucket_id = "${module.kakadu_converter_s3_tiff.bucket_id}"
  trigger_s3_bucket_arn = "${module.kakadu_converter_s3_tiff.bucket_arn}"
}

