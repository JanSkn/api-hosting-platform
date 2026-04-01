# APIForge 🚀 - [![Better Stack Badge](https://uptime.betterstack.com/status-badges/v1/monitor/2iq7g.svg)](https://apiforge.betteruptime.com)

**APIForge** is a Platform-as-a-Service (PaaS) for hosting APIs — designed to make deployment as simple as possible.

Just provide a GitHub repository URL or upload a ZIP file, and your API will be deployed in minutes.

Read the documentation [here](web/src/docs/documentation.md).

---

## ✨ Features

* ⚡ **Fast Deployments** – Deploy APIs in just a few minutes
* 🔗 **GitHub Integration** – Use a repository URL directly
* 📦 **ZIP Upload Support** – Upload your code manually
* 📊 **Deployment Status Tracking** – Monitor progress in real time
* 🧱 **Multi-Stack Support** – Choose your runtime:
  * Java
  * Python
  * JavaScript (Node.js)
* 🔐 **Secure Authentication** – Managed user accounts
* 🌐 **Per-User API Endpoints** – Each deployment gets its own URL

---

## 🛠️ How It Works

1. Log into the dashboard
2. Provide a GitHub repository URL or upload a ZIP file
3. Select your preferred runtime (Java, Python, or JavaScript)
4. Start the deployment
5. Track the deployment status in real time
6. Access your live API via a generated endpoint

---

## Development README

For setup, see [this](DEV.md).

---

## ☁️ Architecture Overview

APIForge is fully built on AWS and leverages a serverless architecture for scalability and efficiency.

### Core Components

* **AWS Lambda**
  * Core backend logic
  * Deployment execution workers

* **Amazon API Gateway**
  * REST API to connect to Lambda
  * Websocket API for status, notifications, logs

* **Amazon SQS**
  * Queues deployment jobs

* **Amazon ECR**
  * Images per deployed user API 

* **AWS CodeBuild**
  * Builds Docker images from user code

* **Amazon S3**
  * Stores uploaded source code and artifacts

* **Amazon DynamoDB**
  * Stores metadata (deployments, users, status)

* **Amazon Cognito**
  * Handles authentication and user management

## 📊 Dashboard

The APIForge dashboard allows you to:

* View all deployments
* Track deployment status
* Manage your APIs
* Trigger new deployments
