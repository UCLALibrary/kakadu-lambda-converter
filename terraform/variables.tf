variable "region" {
  default = "us-west-2"
}

variable "cred_profile" {
  default = "default"
}

variable "src_bucket_name" {}

variable "src_bucket_region" {}

variable "function_name" {}

variable "kakadu_lib_layer" {}

variable "kakadu_bin_layer" {}

# Need to be make this an optional field in the module source code
variable "force_destroy_src_bucket" {}

variable "jp2_bucket_name" {}

# Need to be make this an optional field in the module source code
variable "force_destroy_jp2_bucket" {}

variable "project_artifactId" {}

variable "project_version" {}

variable "kakadu_converter_handler" {}

#TODO: this should probably be an optional field
variable "kakadu_converter_filter_suffix" {}

variable "kakadu_converter_runtime" {}

variable "kakadu_converter_memory_size" {}

variable "kakadu_converter_timeout" {}

variable "kakadu_converter_environment_variables" {}

variable "kakadu_converter_bucket_event" {}

locals {
  artifact_path = "../target/${var.project_artifactId}-${var.project_version}.jar"
  lambda_layers = ["${var.kakadu_bin_layer}", "${var.kakadu_lib_layer}"]

  #TODO: figure out a way to map environment variables as map or a list of key value pairs of some sort
  environment_variables = [ "
}

