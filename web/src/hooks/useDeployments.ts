import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  fetchDeployments,
  fetchDeployment,
  initializeDeployment,
  updateDeploymentStatus,
  generateUploadUrl,
  triggerDeployment,
  deleteDeployment as apiDeleteDeployment,
  type DeploymentRuntime,
  type BuildLog,
} from "@/api/deployments";
import { clearCorrelationId } from "@/api/correlationId";

export function useDeployments() {
  return useQuery({
    queryKey: ["deployments"],
    queryFn: fetchDeployments,
    staleTime: 5 * 60 * 1000, // Keep list fresh for 5 mins
  });
}

export function useDeployment(id: string) {
  return useQuery({
    queryKey: ["deployments", id],
    queryFn: () => fetchDeployment(id),
    enabled: !!id,
    // If deployment is LIVE or FAILED, cache indefinitely.
    // Otherwise, refetch on mount/focus to check for status updates.
    staleTime: (query) => {
      const status = query.state.data?.status;
      if (status === "LIVE" || status === "FAILED") {
        return Infinity;
      }
      return 0;
    },
  });
}

export function useBuildLogs(id: string) {
  return useQuery({
    queryKey: ["buildLogs", id],
    queryFn: async (): Promise<BuildLog[]> => [], // Placeholder until backend supports logs
    enabled: !!id,
  });
}

function mapRuntime(runtime: string): DeploymentRuntime {
  if (runtime === "node" || runtime === "javascript") return "NODEJS_18_X";
  if (runtime === "java") return "JAVA_17";
  if (runtime === "python") return "PYTHON_3_12";
  return "NODEJS_18_X";
}

/**
 * Creates a deployment (initialize -> status:UPLOADING -> upload-url -> upload -> trigger).
 * Returns the deployment ID so the caller can navigate to /deployment/:id.
 */
export function useCreateDeployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (data: {
      name: string;
      runtime: "node" | "python" | "java";
      source: File | string;
      envVars: { key: string; value: string }[];
    }) => {
      // Start fresh correlation ID for each deployment flow
      clearCorrelationId();
      try {
        const isGithub = typeof data.source === "string" && data.source.includes("github.com");

        // 1. Initialize
        const { deploymentId } = await initializeDeployment({
          name: data.name,
          runtime: mapRuntime(data.runtime),
          githubUrl: isGithub ? (data.source as string) : undefined,
        });

        // If it's a GitHub URL, the backend will handle the download and upload during trigger
        if (isGithub) {
          await triggerDeployment(deploymentId);
          return { id: deploymentId };
        }

        // 2. Set status to UPLOADING
        await updateDeploymentStatus(deploymentId, "UPLOADING");

        // 3. Get Upload URL
        const { uploadUrl } = await generateUploadUrl(deploymentId);

        // 4. Perform S3 Upload (File only, GitHub handled above)
        let body: Blob | File | null = null;

        if (data.source instanceof File) {
          body = data.source;
        }

        if (body) {
          const response = await fetch(uploadUrl, {
            method: "PUT",
            body: body,
            headers: {
              "Content-Type": "application/zip",
            },
          });

          if (!response.ok) {
            throw new Error("Failed to upload source code to S3");
          }
        }

        // 5. Trigger
        await triggerDeployment(deploymentId);

        return { id: deploymentId };
      } finally {
        // Successfully triggered or failed, clear correlation ID for next transaction
        clearCorrelationId();
      }
    },
    onSuccess: (data) => {
      // Invalidate the full list
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
      // Also potentially prime or invalidate the specific new deployment if needed, 
      // though here we just return the ID for navigation.
      if (data?.id) {
        queryClient.invalidateQueries({ queryKey: ["deployments", data.id] });
      }
    },
  });
}

export function useDeleteDeployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      await apiDeleteDeployment(id);
    },
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
      queryClient.removeQueries({ queryKey: ["deployments", id] });
    },
  });
}
