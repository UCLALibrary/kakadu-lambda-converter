terraform {
  backend "remote" {}
}

provider "aws" {
  profile = "${var.cred_profile}"
  region  = "${var.region}"
}

module "tiff_src_bucket" {
  source             = "git::https://github.com/UCLALibrary/aws_terraform_s3_module.git"
  bucket_name        = "${var.src_bucket_name}"
  bucket_region      = "${var.src_bucket_region}"
  force_destroy_flag = "${var.force_destroy_src_bucket}"
}

module "output_jp2_bucket" {
  source             = "git::https://github.com/UCLALibrary/aws_terraform_s3_module.git"
  bucket_name        = "${var.jp2_bucket_name}"
  bucket_region      = "${var.jp2_bucket_region}"
  force_destroy_flag = "${var.force_destroy_jp2_bucket}"
}

module "kakadu_converter_lambda_tiff" {
  source = "git::https://github.com/UCLALibrary/aws_terraform_lambda_module.git"

  ## KakaduConverter lambda role setup
  cloudwatch_iam_allowed_actions = "${var.cloudwatch_iam_allowed_actions}"
  s3_iam_allowed_actions         = "${var.lambda_iam_allowed_actions}"
  s3_iam_allowed_resources       = "${local.lambda_iam_allowed_resources}"

  ## KakaduConverter lambda function specification
  app_artifact              = "${local.artifact_path}"
  app_name                  = "${var.function_name}"
  app_layers                = "${local.lambda_layers}"
  app_handler               = "${var.kakadu_handler}"
  app_filter_suffix         = "${var.src_bucket_notification_filter_suffix}"
  app_runtime               = "${var.kakadu_runtime}"
  app_memory_size           = "${var.kakadu_memory_size}"
  app_timeout               = "${var.kakadu_timeout}"
  app_environment_variables = "${local.environment_variables}"

  ## KakaduConverter S3 bucket notification settings
  bucket_event          = "${var.src_bucket_event}"
  trigger_s3_bucket_id  = "${module.tiff_src_bucket.bucket_id}"
  trigger_s3_bucket_arn = "${module.tiff_src_bucket.bucket_arn}"
}

