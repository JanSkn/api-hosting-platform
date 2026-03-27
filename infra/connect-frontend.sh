#!/bin/bash

set -euo pipefail

ENVIRONMENT="${1:?Environment required (local|stg|prod)}"
AWS_REGION=$(grep -A 15 "\[${ENVIRONMENT}.deploy.parameters\]" samconfig.toml | grep "region =" | head -n 1 | cut -d'"' -f2 | xargs)
STACK_NAME=$(grep -A 15 "\[${ENVIRONMENT}.deploy.parameters\]" samconfig.toml | grep "stack_name =" | head -n 1 | cut -d'"' -f2 | xargs)

if [[ "$ENVIRONMENT" == "local" ]]; then
  AWS_CMD="awslocal"
else
  AWS_CMD="aws"
fi

CONFIG_JS_FILE="public/config.js"
LOCAL_CONFIG_JS_FILE="../web/$CONFIG_JS_FILE"

TEMP_CONFIG=$(mktemp)
trap 'rm -f "$TEMP_CONFIG"' EXIT

echo "Using stack: $STACK_NAME"
echo "Using region: $AWS_REGION"

STACK_OUTPUTS=$($AWS_CMD cloudformation describe-stacks \
  --stack-name "$STACK_NAME" \
  --region "$AWS_REGION" \
  --query "Stacks[0].Outputs" \
  --output json)

if [[ -z "$STACK_OUTPUTS" || "$STACK_OUTPUTS" == "null" ]]; then
  echo "Error: Could not fetch stack outputs"
  exit 1
fi

API_ID=$(echo "$STACK_OUTPUTS" | jq -r 'map({ (.OutputKey): .OutputValue }) | add | .ApiId')
USER_POOL_ID=$(echo "$STACK_OUTPUTS" | jq -r 'map({ (.OutputKey): .OutputValue }) | add | .UserPoolId')
USER_POOL_CLIENT_ID=$(echo "$STACK_OUTPUTS" | jq -r 'map({ (.OutputKey): .OutputValue }) | add | .UserPoolClientId')
FRONTEND_BUCKET_NAME=$(echo "$STACK_OUTPUTS" | jq -r 'map({ (.OutputKey): .OutputValue }) | add | .WebBucketName')

cat <<EOF > "$TEMP_CONFIG"
window.APP_CONFIG = {
  AWS_REGION: "$AWS_REGION",
  API_ID: "$API_ID",
  USER_POOL_ID: "$USER_POOL_ID",
  USER_POOL_CLIENT_ID: "$USER_POOL_CLIENT_ID",
  ENVIRONMENT: "$ENVIRONMENT"
};
EOF

if [[ "$ENVIRONMENT" == "local" ]]; then
  if [[ ! -f "$LOCAL_CONFIG_JS_FILE" ]]; then
    echo "✅ Writing config.js locally to $LOCAL_CONFIG_JS_FILE"
    cp "$TEMP_CONFIG" "$LOCAL_CONFIG_JS_FILE"
  else
    echo "✅ $LOCAL_CONFIG_JS_FILE already exists locally, skipping write"
  fi
else
  if ! aws s3 ls "s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE" --region "$AWS_REGION" >/dev/null 2>&1; then
    echo "Uploading to s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE ..."
    aws s3 cp "$TEMP_CONFIG" "s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE" --region "$AWS_REGION"
    echo "✅ Uploaded to s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE"
  else
    echo "✅ File already exists in s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE, skipping upload"
  fi
fi

# file auto-deleted by trap