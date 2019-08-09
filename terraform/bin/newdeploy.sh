#!/bin/bash
# Configured to use https://apps.terraform.io

BACKEND_FILE="backend.hcl"
TERRAFORM="/opt/terraform/bin/terraform"
PLAN_FILE="plan.out"
LOCAL_SECRETS="../target/classes/terraform.tfvars"
WORKSPACE="lambda"

if [[ ! -f ${BACKEND_FILE} ]];
then
  echo "${BACKEND_FILE} not found"
  exit
fi

${TERRAFORM} init \
  -backend-config="${BACKEND_FILE}"

if [[ -z $(${TERRAFORM} workspace list | grep -i ${WORKSPACE}) ]];
then
  ${TERRAFORM} workspace new ${WORKSPACE}
else
  ${TERRAFORM} workspace select ${WORKSPACE}
fi

echo "Working in workspace: $(${TERRAFORM} workspace show)"

if [[ -f "${LOCAL_SECRETS}" ]];
then
  ${TERRAFORM} plan -out ${PLAN_FILE} -var-file="${LOCAL_SECRETS}" 
else
  ${TERRAFORM} plan -out ${PLAN_FILE}
fi

