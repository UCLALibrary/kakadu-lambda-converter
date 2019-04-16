#! /bin/bash

#
# A convenience script to run terraform in this directory. It uses a variables file that has built by Maven.
#

# Create a plan based on our current state
terraform plan -out="plan.out" -var-file="../target/classes/terraform.tfvars"

# Run our plan
terraform apply -auto-approve -backup=".terraform.tfstate.backup" plan.out
