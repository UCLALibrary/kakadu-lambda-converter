variable "region" {}

variable "cred_profile" {}

variable "src_bucket_name" {}

variable "src_bucket_region" {}

variable "function_name" {}

variable "kakadu_lib_layer" {}

variable "kakadu_bin_layer" {}

variable "force_destroy_src_bucket" {
  default = "false"
}

variable "jp2_bucket_name" {}

variable "jp2_bucket_region" {}

variable "force_destroy_jp2_bucket" {
  default = "false"
}
variable "monitoring_endpoint" {}

variable "project_artifactId" {}

variable "project_version" {}

variable "kakadu_handler" {}

variable "src_bucket_notification_filter_suffix" {}

variable "kakadu_runtime" {}

variable "kakadu_memory_size" {}

variable "kakadu_timeout" {}

variable "src_bucket_event" {}

variable "cloudwatch_iam_allowed_actions" {}

variable "lambda_iam_allowed_actions" {}

variable "kakadu_compression_rate" {}

#### These locals expression are set to help with interpolation after initializing variables####
locals {
  artifact_path = "../target/${var.project_artifactId}-${var.project_version}.jar"
  lambda_layers = ["${var.kakadu_bin_layer}", "${var.kakadu_lib_layer}"]

  environment_variables = {
    DESTINATION_BUCKET  = "${var.jp2_bucket_name}"
    MONITORING_ENDPOINT = "${var.monitoring_endpoint}"
    KAKADU_COMPRESSION_RATE = "${var.kakadu_compression_rate}"
  }

  lambda_iam_allowed_resources = ["${module.tiff_src_bucket.bucket_arn}*", "${module.output_jp2_bucket.bucket_arn}*"]
}

