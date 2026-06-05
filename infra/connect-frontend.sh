#!/bin/bash

set -euo pipefail

ENVIRONMENT="${1:?Environment required (local|testing|stg|prod)}"
AWS_REGION="${2:?Region required}"
STACK_NAME="${3:?Stack Name required}"

if [[ "$ENVIRONMENT" == "local" || "$ENVIRONMENT" == "testing" ]]; then
  AWS_CMD="awslocal"
else
  AWS_CMD="aws"
fi

CONFIG_JS_FILE="config.js" # after build in the dist/ folder, the config.js file will be at root level
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

if [[ "$ENVIRONMENT" == "testing" ]]; then
  echo "⏭️  Environment is 'testing' (CI). Skipping."
elif [[ "$ENVIRONMENT" == "local" ]]; then
  echo "✅ Writing config.js locally to $LOCAL_CONFIG_JS_FILE"
  cp "$TEMP_CONFIG" "$LOCAL_CONFIG_JS_FILE"
else
  echo "Uploading fresh config to s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE ..."
  # --content-type is required: the source is an extension-less mktemp file, so
  # aws s3 cp would default to binary/octet-stream. Combined with the
  # X-Content-Type-Options: nosniff header from CloudFront, the browser would
  # refuse to execute config.js as a <script>, leaving window.APP_CONFIG unset.
  $AWS_CMD s3 cp "$TEMP_CONFIG" "s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE" \
    --region "$AWS_REGION" --content-type "text/javascript"
  echo "✅ Uploaded to s3://$FRONTEND_BUCKET_NAME/$CONFIG_JS_FILE"
fi

# file auto-deleted by trap