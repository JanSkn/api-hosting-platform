import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  fetchDeployments,
  fetchDeployment,
  initializeDeployment,
  updateDeploymentStatus,
  generateUploadUrl,
  triggerDeployment,
  type DeploymentRuntime,
  type BuildLog,
} from "@/api/deployments";

export function useDeployments() {
  return useQuery({
    queryKey: ["deployments"],
    queryFn: fetchDeployments,
  });
}

export function useDeployment(id: string) {
  return useQuery({
    queryKey: ["deployments", id],
    queryFn: () => fetchDeployment(id),
    enabled: !!id,
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
  if (runtime === "node") return "NODEJS_18_X";
  if (runtime === "java") return "JAVA_17";
  // Defaulting to Node for now if unknown, or we could throw an error
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
      source: string;
      envVars: { key: string; value: string }[];
    }) => {
      // 1. Initialize
      const { deploymentId } = await initializeDeployment({
        name: data.name,
        runtime: mapRuntime(data.runtime),
      });

      // 2. Set status to UPLOADING
      await updateDeploymentStatus(deploymentId, "UPLOADING");

      // 3. Get Upload URL
      const { uploadUrl } = await generateUploadUrl(deploymentId);

      // 4. Perform S3 Upload
      // Note: In a real app, 'data.source' would be the file/blob to upload.
      // For now, we assume the upload is handled here or mocked.
      if (data.source) {
        await fetch(uploadUrl, {
          method: "PUT",
          body: data.source, // Assuming data.source is the zip content
          headers: {
            "Content-Type": "application/zip",
          },
        });
      }

      // 5. Trigger
      await triggerDeployment(deploymentId);

      return { id: deploymentId };
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
    },
  });
}

export function useDeleteDeployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (_id: string) => {
      // DELETE endpoint not yet exposed by backend – no-op for now
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
    },
  });
}
