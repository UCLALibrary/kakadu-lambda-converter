#! /bin/bash

terraform destroy -force -var-file="../target/classes/terraform.tfvars"
rm -f plan.out
