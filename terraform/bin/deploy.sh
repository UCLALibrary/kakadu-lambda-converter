#!/bin/bash
# Configured to use https://apps.terraform.io

TERRAFORM_CLASS_PATH="../target/classes"
BACKEND_FILE="${TERRAFORM_CLASS_PATH}/backend.hcl"
TERRAFORM="/opt/terraform/bin/terraform"
PLAN_FILE="plan.out"
LOCAL_SECRETS="${TERRAFORM_CLASS_PATH}/terraform.tfvars"
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
  if [[ $1 == "destroy" ]];
  then
    ${TERRAFORM} destroy -var-file="${LOCAL_SECRETS}"
  else
    ${TERRAFORM} plan -out ${PLAN_FILE} -var-file="${LOCAL_SECRETS}" 
  fi
else
  ${TERRAFORM} plan -out ${PLAN_FILE}
fi

