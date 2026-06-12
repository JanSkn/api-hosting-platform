# Documentation

## Getting Started

Deploy your first API in under a minute. Upload a `.zip` file containing your handler code or import directly from a public GitHub repository. **APIForge** will automatically build, optimize, and deploy your application to a publicly available URL.

## Supported Runtimes & Frameworks

### Node.js (v18/v20/v22)

* **Supported:** Any HTTP framework (Express, Fastify, Koa, NestJS, etc.), using both CommonJS and ESM.
* **Port Configuration:** Your application must listen on the port `8080`.

### Python (v3.10/v3.11/v3.12/v3.13/v3.14)

* **Supported Framework:** FastAPI (or any ASGI-compatible framework).
* **Build System:** Dependencies are resolved automatically via `requirements.txt` or `pyproject.toml`.
* **System Libraries:** A standard build environment (`build-essential`) is provided. Custom OS-level libraries (like `ffmpeg` or special database drivers that require `apt-get`) are currently not supported.

---

## Project Structure

To ensure APIForge can run your application without any code changes, your project must follow these simple structural conventions:

### Node.js Setup

1. Your **`package.json`** (and ideally `package-lock.json`) must be in the **root directory**.
2. Your main entry point file must be named **`index.js`** and located in the root directory.
3. Your server must listen on `process.env.PORT`.

```javascript
// index.js Example
const express = require('express');
const app = express();
const port = 8080;

app.get('/', (req, res) => res.send('Hello from ApiForge!'));

app.listen(port, () => console.log(`Server running on port ${port}`));

```

### Python Setup

1. Your dependency file (**`requirements.txt`** or **`pyproject.toml`**) must be in the **root directory**. Note that `uvicorn` as an ASGI server is a required dependency.
2. Your main application file must be named **`main.py`** and located in the root directory.
3. Inside `main.py`, your FastAPI instance variable must be named **`app`**.

```python
# main.py Example
from fastapi import FastAPI

app = FastAPI() # Variable must be named 'app'

@app.get("/")
def read_root():
    return {"message": "Hello from ApiForge!"}

```

---

## Environment Variables

You can define custom environment variables directly in your project's deployment settings on the APIForge dashboard.

* Secret variables are encrypted at rest and injected into your runtime environment automatically. Note that they will not be rotated
* **Never** commit secrets, passwords, or `.env` files to your repository.
* **Note for Node.js:** The `PORT` variable is reserved by the platform and automatically managed for you.

---

## API Limits

| Resource | Limit | Description |
| --- | --- | --- |
| Request Timeout | 5 seconds | Maximum time your API has to respond to an incoming request. |
| Payload Size | 6 MB | Maximum size for incoming HTTP request bodies and responses. |
| Concurrency | 1,000 | Maximum number of simultaneous requests handled before throttling. |