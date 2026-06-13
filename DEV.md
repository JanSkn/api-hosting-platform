# Prerequisites

For local development, install:

* docker
* just
* aws-vault (only for special profiles; can skip)
* localstack
* samlocal
* sops
* awslocal cli
* playwright
* uv (only for Python scripts to bootstrap AWS IAM roles; can skip)

# Local Development

## Start LocalStack

Start the local AWS environment:

```sh
just run-localstack
```

## Deploy locally

Deploy the SAM stack to LocalStack:

```sh
just deploy-stack local
```

Install Lambda packages:

```sh
just lambda install-all
```

or for a single module:

```sh
just lambda install <module-name>
```

Local development uses:

* `samlocal` instead of `sam`
* `awslocal` instead of `aws`

# Environments

The project has four environments:

| Environment    | Purpose                   | Infrastructure               |
| -------------- | ------------------------- | ---------------------------- |
| **local**      | Developer environment     | LocalStack                   |
| **testing**    | CI integration tests      | LocalStack in GitHub Actions |
| **staging**    | Pre-production validation | AWS account                  |
| **production** | Production workload       | AWS account                  |

## Testing

The testing environment runs in CI:

* executed in GitHub Actions
* uses LocalStack
* runs automated integration tests

## Staging

Staging is deployed automatically after merging to `main`.

* runs on real AWS infrastructure
* used for smoke tests
* artifacts are stored for reproducible production deployments

Staging:

https://d1kcc3fdgope53.cloudfront.net/

## Production

Production deployment is manually triggered after staging succeeds.

* separate AWS account
* only validated artifacts are promoted
* deployment happens via release workflow

Production:

https://d1ldsga5pjglgd.cloudfront.net/

# Architecture

## Tech Stack

* **Frontend:** React & Tailwind CSS
* **Backend:** Java 21

## Repository Structure

```
secrets/
```

Contains local development secrets (e.g. LocalStack auth token).

```
infra/
```

Contains AWS SAM templates.

```
bootstrap/
```

Contains AWS account setup.

```
lambda/
```

Contains backend code running on AWS Lambda.

Structure:

* `common-utils/`
  Shared utilities and common code.

* `backend-api-lambda/`
  Core platform logic.

* `sqs-dispatcher-lambda/`
  Consumes build jobs from SQS and starts CodeBuild jobs to build and push Docker images to ECR.

* `function-deployer-lambda/`
  Creates isolated Lambda functions for users after CodeBuild completion.

# Configuration

`infra/samconfig.toml` is the single source of truth for:

* AWS region
* stack name
* environment variables

# Bootstrapping

Ran once per AWS account (`staging` / `production`) to create:

* GitHub OIDC deployment role
* Artifact bucket (staging only)

The artifact bucket must exist before the first deployment.

# Deployment

## AWS Deployment

For staging and production use:

* `sam` instead of `samlocal`
* `aws` instead of `awslocal`

## Staging Deployment

Triggered automatically after merge to `main`.

Steps:

1. Build artifacts

   * latest tag metadata
   * main SHA tag metadata

2. Upload artifacts to S3

3. Run smoke tests

## Production Deployment

Triggered manually:

1. Admin starts release workflow
2. Release tag is created
3. Production deployment workflow runs

# CI/CD

## Continuous Integration

CI runs after opening a PR against `main`.

Only changed modules are tested.

Checks include:

* build
* linting
* code analysis
* unit tests
* integration tests

## Continuous Deployment

Flow:

```
Pull Request
     |
     v
CI checks
     |
     v
Merge to main
     |
     v
Staging deployment
     |
     v
Manual production release
```

# Contributing

## Trunk-based Development

1. Create feature branch
2. Open PR against `main`
3. Wait for CI checks
4. Merge to `main`
5. Staging deployment starts automatically

# Other

## Maven Wrapper

```sh
cd lambda && mvn wrapper:wrapper
```

## Secrets

LocalStack token format:

```yaml
KEY: value
```

Encrypt using SOPS.

More information:
https://technotim.com/posts/secret-encryption-sops/
