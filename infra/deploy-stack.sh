#!/bin/bash

set -euo pipefail

ENV=${1:-}
CONNECT_FRONTEND=${2:-}

ALLOWED_ENVS="local stg prod"

if [[ -z "$ENV" ]]; then
  echo "❌  ./deploy-stack.sh [local|stg|prod]"
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

if [ "$ENV" == "local" ]; then
  SAM_CMD="samlocal"
else
  SAM_CMD="sam"
fi

echo "🚀 Starting deployment for '$ENV'..."

$SAM_CMD deploy --config-env "$ENV"

echo "✅ Deployment successful."

echo "🔗 Connecting frontend..."
chmod +x connect-frontend.sh
./connect-frontend.sh "$ENV"