# see https://github.com/aws/aws-lambda-web-adapter/blob/main/examples/expressjs
FROM public.ecr.aws/docker/library/node:20-slim

COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.1 /lambda-adapter /opt/extensions/lambda-adapter

EXPOSE 8080
WORKDIR "/var/task"

# use wildcard to ensure both package.json AND package-lock.json are copied if they exist
COPY package*.json ./

RUN npm install --omit=dev

COPY . .

CMD ["node", "index.js"]
