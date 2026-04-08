# Prerequisites
For local development, install
- just
- aws-vault
- localstack
- samlocal
- sops
- awslocal cli
- playwright for frontend tests

## Deployments
- sam instead of samlocal
- aws cli instead of awslocal cli

# Tech Stack
- **Frontend:** React & Tailwind CSS
- **Backend:** Java 21

# Overview

`infra/samconfig.toml` is the single source of truth to configure the AWS region, stack name and environment variables.

For local development, use `just run-localstack` to spin up the localstack container, then `just deploy-stack local [--connect]` 
to deploy the stack locally. 

- **`secrets/`**: Contains local dev secrets (e.g., LocalStack auth token).
- **`infra/`**: Contains SAM templates for the AWS architecture.
- **`lambda/`**: Contains backend code running on AWS Lambda
    **`common-utils/`** common utils and shared code.
    - **`core-backend-lambda/`**: Contains the core platform logic.
    - **`build-orchestrator-lambnda/`**: Creates isolated Lambda functions per user from AWS SQS.

## Secrets
To encrypt the Localstack auth token, the `localstack-token.yaml`should have the format
```yaml
KEY: value
```
See [this](https://technotim.com/posts/secret-encryption-sops/) for more information on encryption.

## Maven Wrapper
```sh
cd lambda && mvn wrapper:wrapper
```