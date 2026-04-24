# see https://github.com/awslabs/aws-lambda-web-adapter/tree/main/examples/fastapi
FROM public.ecr.aws/docker/library/python:3.11-slim-bookworm

COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.1 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /var/task

# dependencies for some python packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY . .

# for faster package installation
RUN pip install uv

RUN if [ -f "requirements.txt" ]; then \
        uv pip install --system -r requirements.txt; \
    elif [ -f "pyproject.toml" ]; then \
        uv pip install --system .; \
    fi

ENV PORT=8080
ENV APP_MODULE=main:app

# TODO use fastapi instead of uvicorn?
CMD exec uv run uvicorn --port=$PORT $APP_MODULE