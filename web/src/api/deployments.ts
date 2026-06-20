import { apiFetch } from "./apiClient";

export interface Deployment {
  deploymentId: string;
  name: string;
  status: DeploymentStatus;
  apiUri: string;
  createdAt: number;
  runtime: DeploymentRuntime;
}

export interface BuildLog {
  time: string;
  msg: string;
}

export type DeploymentRuntime =
  | "JAVA_11"
  | "JAVA_17"
  | "JAVA_21"
  | "NODEJS_18_X"
  | "NODEJS_20_X"
  | "NODEJS_22_X"
  | "PYTHON_3_10"
  | "PYTHON_3_11"
  | "PYTHON_3_12"
  | "PYTHON_3_13"
  | "PYTHON_3_14";
export type DeploymentStatus = "INITIALIZED" | "UPLOADING" | "IN_PROGRESS" | "FAILED" | "LIVE";

export interface EnvironmentVariable {
  key: string;
  value: string;
  isSecret: boolean;
}

export interface CreateDeploymentRequest {
  name: string;
  runtime: DeploymentRuntime;
  githubUrl?: string;
  environmentVariables?: EnvironmentVariable[];
}

export interface CreateDeploymentResponse {
  deploymentId: string;
}

export interface UploadUrlResponse {
  uploadUrl: string;
  expiresInSeconds: number;
}

export async function fetchDeployments(): Promise<Deployment[]> {
  const res = await apiFetch("/deployments");
  return res.json();
}

export async function fetchDeployment(id: string): Promise<Deployment | undefined> {
  const res = await apiFetch(`/deployments/${id}`);
  return res.json();
}

/** Step 1: Initialize a new deployment */
export async function initializeDeployment(
  request: CreateDeploymentRequest
): Promise<CreateDeploymentResponse> {
  const res = await apiFetch("/deployments/initialize", {
    method: "POST",
    body: JSON.stringify(request),
  });
  return res.json();
}

/** Step 2: Update deployment status (e.g. to UPLOADING before S3 upload) */
export async function updateDeploymentStatus(
  deploymentId: string,
  status: DeploymentStatus
): Promise<void> {
  await apiFetch(`/deployments/${deploymentId}/status?status=${status}`, {
    method: "PATCH",
  });
}

/** Step 3: Get a pre-signed S3 upload URL for a specific deployment */
export async function generateUploadUrl(deploymentId: string): Promise<UploadUrlResponse> {
  const res = await apiFetch(`/deployments/upload-url?deploymentId=${deploymentId}`, {
    method: "GET",
  });
  return res.json();
}

export async function triggerDeployment(deploymentId: string): Promise<void> {
  await apiFetch(`/deployments/${deploymentId}/trigger`, { method: "POST" });
}

export async function deleteDeployment(deploymentId: string): Promise<void> {
  await apiFetch(`/deployments/${deploymentId}`, { method: "DELETE" });
}

export interface CloudWatchLogsResponse {
  events: { message: string; timestamp: number }[];
  nextToken: string | null;
}

export interface DeploymentLogsUrlResponse {
  uploadUrls: string[];
}

export async function fetchCloudWatchLogs(
  deploymentId: string,
  nextToken?: string
): Promise<CloudWatchLogsResponse> {
  const url = `/deployments/logs?deploymentId=${deploymentId}${nextToken ? `&nextToken=${encodeURIComponent(nextToken)}` : ""}`;
  const res = await apiFetch(url);
  return res.json();
}

