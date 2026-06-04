#!/bin/bash
set -euo pipefail

ENVIRONMENT="${1:-}"
ALLOWED_ENVS="stg prod"

if [[ -z "$ENVIRONMENT" ]]; then
  echo "❌ ./create-bootstrap-user.sh [stg|prod]"
  exit 1
fi

if [[ ! " $ALLOWED_ENVS " =~ " $ENVIRONMENT " ]]; then
  echo "❌ Only $ALLOWED_ENVS are allowed"
  exit 1
fi

case "$ENVIRONMENT" in
  stg)  ACCOUNT_ID="071308038858" ;;
  prod) ACCOUNT_ID="591292939760" ;;
esac

USER_NAME="bootstrap-admin-${ENVIRONMENT}"
ROLE_NAME="BootstrapAdminRole-${ENVIRONMENT}"
ROLE_ARN="arn:aws:iam::${ACCOUNT_ID}:role/${ROLE_NAME}"

# create in console (just used for setup)
echo "Root Access Key ID:"
read -rs ROOT_ACCESS_KEY_ID
echo "Root Secret Access Key:"
read -rs ROOT_SECRET_ACCESS_KEY

export AWS_ACCESS_KEY_ID="${ROOT_ACCESS_KEY_ID}"
export AWS_SECRET_ACCESS_KEY="${ROOT_SECRET_ACCESS_KEY}"
unset AWS_SESSION_TOKEN

# delete for safety: only needed for bootstrapping
cleanup() {
  echo "Deleting root access key..."
  aws iam delete-access-key \
    --access-key-id "${ROOT_ACCESS_KEY_ID}" 2>/dev/null && echo "✅ Root access key deleted" || echo "⚠️  Could not delete root access key – please delete manually!"
  unset AWS_ACCESS_KEY_ID
  unset AWS_SECRET_ACCESS_KEY
}
trap cleanup EXIT

echo "Creating role: ${ROLE_NAME}..."

TRUST_POLICY=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::${ACCOUNT_ID}:root"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
)

aws iam create-role \
  --role-name "${ROLE_NAME}" \
  --assume-role-policy-document "${TRUST_POLICY}" \
  >/dev/null

aws iam attach-role-policy \
  --role-name "${ROLE_NAME}" \
  --policy-arn "arn:aws:iam::aws:policy/AdministratorAccess"

echo "✅ Role created"

echo "Creating user: ${USER_NAME}..."

aws iam create-user \
  --user-name "${USER_NAME}" \
  >/dev/null

echo "Waiting for user to propagate..."
sleep 10

TRUST_POLICY_SCOPED=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::${ACCOUNT_ID}:user/${USER_NAME}"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
)

aws iam update-assume-role-policy \
  --role-name "${ROLE_NAME}" \
  --policy-document "${TRUST_POLICY_SCOPED}"

echo "Creating assume-role policy..."

cat > /tmp/bootstrap-assume-role-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "sts:AssumeRole",
      "Resource": "${ROLE_ARN}"
    }
  ]
}
EOF

aws iam put-user-policy \
  --user-name "${USER_NAME}" \
  --policy-name BootstrapAssumeRole \
  --policy-document file:///tmp/bootstrap-assume-role-policy.json

rm -f /tmp/bootstrap-assume-role-policy.json

echo "✅ User created"

echo "Creating access key..."

ACCESS_KEY_JSON=$(aws iam create-access-key --user-name "${USER_NAME}")
ACCESS_KEY_ID=$(echo "$ACCESS_KEY_JSON" | jq -r '.AccessKey.AccessKeyId')
SECRET_ACCESS_KEY=$(echo "$ACCESS_KEY_JSON" | jq -r '.AccessKey.SecretAccessKey')

echo "Importing credentials into aws-vault..."
aws-vault add "${USER_NAME}" <<EOF
${ACCESS_KEY_ID}
${SECRET_ACCESS_KEY}
EOF

echo
echo "Done."
