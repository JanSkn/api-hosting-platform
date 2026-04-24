# used by backend-ci.yml/frontend-ci.yml or use from root with just <module-name> <recipe-name>
mod lambda "lambda"
mod common-utils "lambda/common-utils"
mod backend-api-lambda "lambda/backend-api-lambda"
mod sqs-dispatcher-lambda "lambda/sqs-dispatcher-lambda"
mod function-deployer-lambda "lambda/function-deployer-lambda"
mod web "web"

assume profile:
    aws-vault exec {{ profile }} -d 8h

encrypt-localstack-token:
    sops -e -i secrets/localstack-token.yaml

run-localstack:
    sops exec-env secrets/localstack-token.yaml 'docker compose -f localstack/docker-compose.yml up -d'

delete-stack stack-name region:
    awslocal cloudformation delete-stack --stack-name {{ stack-name }} --region {{ region }}

deploy-stack environment:
    cd infra && chmod +x deploy-stack.sh && ./deploy-stack.sh {{ environment }}