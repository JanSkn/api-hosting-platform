import { apiFetch } from "./apiClient";
import type { Deployment } from "@/components/ProjectCard";

export interface BuildLog {
  time: string;
  msg: string;
}

export type DeploymentRuntime = "JAVA_17" | "NODEJS_18_X" | "PYTHON_3_12";
export type DeploymentStatus = "INITIALIZED" | "UPLOADING" | "IN_PROGRESS" | "FAILED" | "LIVE";

export interface CreateDeploymentRequest {
  name: string;
  runtime: DeploymentRuntime;
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

/** Step 4: Trigger the build/deploy pipeline after uploading the zip */
export async function triggerDeployment(deploymentId: string): Promise<void> {
  await apiFetch(`/deployments/${deploymentId}/trigger`, { method: "POST" });
}
