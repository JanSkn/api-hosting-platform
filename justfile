# used by backend-ci.yml or use from root with just <module-name> <recipe-name>
mod common-utils "lambda/common-utils"
mod core-backend-lambda "lambda/core-backend-lambda"
mod build-orchestrator-lambda "lambda/build-orchestrator-lambda"
mod web "web"

assume profile:
    aws-vault exec {{ profile }} -d 8h

encrypt-localstack-token:
    sops -e -i secrets/localstack-token.yaml

build:
    cd lambda && ./mvnw clean package

run-localstack:
    sops exec-env secrets/localstack-token.yaml 'docker compose -f localstack/docker-compose.yml up -d'

delete-stack stack-name:
    awslocal cloudformation delete-stack --stack-name {{ stack-name }}

deploy-stack environment:
    cd infra && chmod +x deploy-stack.sh && ./deploy-stack.sh {{ environment }}