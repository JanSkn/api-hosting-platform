import { apiFetch } from "./apiClient";
import type { Deployment } from "@/components/ProjectCard";

export interface BuildLog {
  time: string;
  msg: string;
}

export interface UploadUrlResponse {
  deploymentId: string;
  uploadUrl: string;
}

export async function fetchDeployments(): Promise<Deployment[]> {
  const res = await apiFetch("/deployments");
  return res.json();
}

export async function fetchDeployment(id: string): Promise<Deployment | undefined> {
  const res = await apiFetch(`/deployments/${id}`);
  return res.json();
}

export async function fetchDeploymentStatus(id: string): Promise<unknown> {
  const res = await apiFetch(`/deployments/${id}/status`);
  return res.json();
}

/** Step 1: creates a deployment entry + returns a pre-signed S3 upload URL */
export async function generateUploadUrl(): Promise<UploadUrlResponse> {
  const res = await apiFetch("/deployments/upload-url", { method: "POST" });
  return res.json();
}

/** Step 2: trigger the build/deploy pipeline after uploading the zip */
export async function triggerDeployment(deploymentId: string): Promise<void> {
  await apiFetch(`/deployments/${deploymentId}/trigger`, { method: "POST" });
}

/** Fetch build logs for a deployment */
export async function fetchBuildLogs(id: string): Promise<BuildLog[]> {
  const res = await apiFetch(`/deployments/${id}/logs`);
  return res.json();
}
