# Instructions on how to use this

The terraform configuration files are set up to use an external module to provision lambda, S3, and IAM roles. Assure that you have proper AWS credentials and permissions prior to running terraform.

### Deploying Terraform integrated with app.terraform.io as backend state file storage

After compiling and generating your artifacts via Maven, your target directory should contain the necessary terraform.tfvars file needed. Please execute and follow the steps below to run terraform:

    cp -rp backend.hcl.sample backend.hcl
    vi backend.hcl # insert token and organization name into fields
    bin/app-terraform-deploy.sh
    NOTE: if this is your first time running the deploy script, you will have to manually enter `1` to select (yourprefix)-lambda as the default workspace
    terraform apply plan.out

Running `bin/app-terraform-deploy.sh` will initlaize terraform and create a workspace prefixed with your username defined in `backend.hcl` This will allow you to create/destroy resources that only you have created/managed via terraform. If the terraform configuration attempts to modify an existing resource outside of its state file, terraform will most likely abort and not continue with the operation. It is preferred that you do not touch any resources outside of what your state file manages. You can also login to https://app.terraform.io to view the contents of your statefile.

### Destroying your Terraform resources integrated with app.terraform.io

This will deploy all resources stored in the remote statefile you have instantiated from backend.hcl
    bin/app-terraform-deploy.sh destroy

