# Prerequisites
For local development, install
- docker
- just
- aws-vault
- localstack
- samlocal
- sops
- awslocal cli
- playwright for frontend tests
- uv for Python script to bootstrap AWS IAM roles

## Bootstrapping
- run per account (staging/production) to create the GitHub OIDC deployment role and (for staging only)
the Artifact bucket because it must exist before the first deployment.

## Deployments
- sam instead of samlocal
- aws cli instead of awslocal cli

# Tech Stack
- **Frontend:** React & Tailwind CSS
- **Backend:** Java 21

# Overview

`infra/samconfig.toml` is the single source of truth to configure the AWS region, stack name and environment variables.

For local development, use `just run-localstack` to spin up the localstack container, then `just deploy-stack local` 
to deploy the stack locally (install the `.zip`files with `just lambda install-all` or `just lambda install <module-name>`). 

- **`secrets/`**: Contains local dev secrets (e.g., LocalStack auth token).
- **`infra/`**: Contains SAM templates for the AWS architecture.
- **`bootstrap/`**: Contains AWS account setup.
- **`lambda/`**: Contains backend code running on AWS Lambda
    **`common-utils/`** common utils and shared code.
    - **`backend-api-lambda/`**: Contains the core platform logic.
    - **`sqs-dispatcher-lambda/`**: Takes build job from SQS queue and starts CodeBuild to build and push Dockerimage with usercode to ECR.
    - **`function-deployer-lambda/`**: Creates isolated Lambda functions per user from EventBridge event after CodeBuild finished in `sqs-dispatcher-lambda`.

# Contributing
## Trunk-based Development
- Create a feature branch
- Open a PR against `main`
- If all checks passed, a merge to main will trigger deployment against the staging environment
- Deployment to production is triggered manually after staging passed

## CI/CD pipeline
### CI
- CI checks happen after opening a PR against main
- only changed modules will get tested
- checks contain
    - building, linting, code analysis, unit tests, integration tests

### CD
- after a merge to main:
- staging deployment starts
    - build artifacts (`latest` and `main-sha` tag metadata)
    - upload artifacts to S3 for reproducability in production
    - smoke tests
- manual production deployment
    - an admin can start production deployments for deployments that passed staging via a release workflow
    - after that, another workflow will trigger production deployment from the release tag

# Other
## Maven Wrapper
```sh
cd lambda && mvn wrapper:wrapper
```

## Secrets
To encrypt the Localstack auth token, the `localstack-token.yaml`should have the format
```yaml
KEY: value
```
See [this](https://technotim.com/posts/secret-encryption-sops/) for more information on encryption.