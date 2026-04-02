import { USE_MOCK_DATA, API_BASE_URL } from "./config";
import { mockApi } from "./mock-data";
import type { Deployment } from "@/components/ProjectCard";
import type { BuildLog } from "./mock-data";

export async function fetchDeployments(): Promise<Deployment[]> {
  if (USE_MOCK_DATA) return mockApi.getDeployments();
  const res = await fetch(`${API_BASE_URL}/deployments`);
  if (!res.ok) throw new Error("Failed to fetch deployments");
  return res.json();
}

export async function fetchDeployment(id: string): Promise<Deployment | undefined> {
  if (USE_MOCK_DATA) return mockApi.getDeployment(id);
  const res = await fetch(`${API_BASE_URL}/deployments/${id}`);
  if (!res.ok) throw new Error("Failed to fetch deployment");
  return res.json();
}

export async function createDeployment(data: {
  name: string;
  runtime: "node" | "python";
  source: string;
  envVars: { key: string; value: string }[];
}): Promise<Deployment> {
  if (USE_MOCK_DATA) return mockApi.createDeployment(data);
  const res = await fetch(`${API_BASE_URL}/deployments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create deployment");
  return res.json();
}

export async function deleteDeployment(id: string): Promise<void> {
  if (USE_MOCK_DATA) return mockApi.deleteDeployment(id);
  const res = await fetch(`${API_BASE_URL}/deployments/${id}`, { method: "DELETE" });
  if (!res.ok) throw new Error("Failed to delete deployment");
}

export async function fetchBuildLogs(id: string): Promise<BuildLog[]> {
  if (USE_MOCK_DATA) return mockApi.getBuildLogs(id);
  const res = await fetch(`${API_BASE_URL}/deployments/${id}/logs`);
  if (!res.ok) throw new Error("Failed to fetch build logs");
  return res.json();
}
