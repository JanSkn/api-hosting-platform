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

For local development, use `just run-localstack` to spin up the localstack container, then `just deploy-stack local` 
to deploy the stack locally. 

- **`secrets/`**: Contains local dev secrets (e.g., LocalStack auth token).
- **`infra/`**: Contains SAM templates for the AWS architecture.
- **`bootstrap/`**: Contains AWS account setup.
- **`lambda/`**: Contains backend code running on AWS Lambda
    **`common-utils/`** common utils and shared code.
    - **`backend-api-lambda/`**: Contains the core platform logic.
    - **`sqs-dispatcher-lambda/`**: Takes build job from SQS queue and starts CodeBuild to build and push Dockerimage with usercode to ECR.
    - **`function-deployer-lambda/`**: Creates isolated Lambda functions per user from EventBridge event after CodeBuild finished in `sqs-dispatcher-lambda`.

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