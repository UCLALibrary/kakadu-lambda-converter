# Documentation found here: https://www.terraform.io/docs/backends/types/remote.html
workspaces {
  prefix = "${terraform.workspace.prefix}-"
}

hostname = "app.terraform.io"
organization = "${terraform.organization.name}"
token = "${terraform.user.token}"

