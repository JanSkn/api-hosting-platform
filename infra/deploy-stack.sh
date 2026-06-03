#!/bin/bash

set -euo pipefail

ENV=${1:-}
ALLOWED_ENVS="local testing stg prod"

AWS_REGION=$(grep -A 15 "\[${ENV}.deploy.parameters\]" samconfig.toml | grep "region =" | head -n 1 | cut -d'"' -f2 | xargs)
STACK_NAME=$(grep -A 15 "\[${ENV}.deploy.parameters\]" samconfig.toml | grep "stack_name =" | head -n 1 | cut -d'"' -f2 | xargs)

if [[ -z "$ENV" ]]; then
  echo "❌  ./deploy-stack.sh [local|testing|stg|prod]"
  exit 1
fi

if [[ ! " $ALLOWED_ENVS " =~ " $ENV " ]]; then
  echo "❌ Only $ALLOWED_ENVS are allowed"
  exit 1
fi

# prevented by AWS IAM, but for early safety we also block it here
if [[ "$ENV" != "local" && "${GITHUB_ACTIONS:-}" != "true" ]]; then
  echo "🛑 Deployments for '$ENV' are only allowed using GitHub Actions"
  exit 1
fi

if [[ "$ENV" == "local" || "$ENV" == "testing" ]]; then
  SAM_CMD="samlocal"
  AWS_CMD="awslocal"
else
  SAM_CMD="sam"
  AWS_CMD="aws"
fi

echo "🚀 Starting deployment for '$ENV'..."

$SAM_CMD deploy --config-env "$ENV"

echo "✅ Deployment successful."

echo "📤 Uploading Dockerfile templates..."
TEMPLATE_BUCKET=$($AWS_CMD cloudformation describe-stacks \
  --stack-name "$STACK_NAME" \
  --region "$AWS_REGION" \
  --query "Stacks[0].Outputs[?OutputKey=='DockerfileTemplateBucket'].OutputValue" \
  --output text)

$AWS_CMD s3 cp ../lambda/sqs-dispatcher-lambda/src/main/resources/templates/ s3://"$TEMPLATE_BUCKET"/ --recursive

echo "🔗 Connecting frontend..."
chmod +x connect-frontend.sh
./connect-frontend.sh "$ENV" "$AWS_REGION" "$STACK_NAME"