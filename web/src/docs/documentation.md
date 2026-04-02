# Documentation

## Getting Started

Deploy your first API in under a minute. Upload a `.zip` file containing your handler code or import directly from a public GitHub repository. **APIForge** will automatically build a container image and deploy it to a publicy available URL.

## Supported Runtimes

- **Node.js 20.x** — CommonJS & ESM supported
- **Python 3.10/3.11/3.12/3.13/3.14** 
    - Supported framework: FastAPI
    - Installer checks in this order: `requirements.txt` > `pyproject.toml`
    - Note: Only essential build tools are installed; additional system libraries (e.g., `libqv-dev`) are not supported at the moment

## Project Structure

```
my-api/
├── handler.js    # Entry point (Node.js)
├── package.json  # Dependencies
└── .env.example  # Environment variables
```

## Environment Variables

Define environment variables in the deployment settings. They are encrypted at rest and injected into your function at runtime. Never commit secrets to your repository.

## API Limits

| Resource | Limit |
|---|---|
| Request timeout | 30 seconds |
| Payload size | 6 MB |
| Concurrent executions | 1,000 |
