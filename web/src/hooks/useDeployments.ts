import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  fetchDeployments,
  fetchDeployment,
  fetchBuildLogs,
  generateUploadUrl,
  triggerDeployment,
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
    queryFn: () => fetchBuildLogs(id),
    enabled: !!id,
  });
}

/**
 * Creates a deployment (upload-url step + trigger step).
 * Returns the deployment ID so the caller can navigate to /deployment/:id.
 */
export function useCreateDeployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (_data: {
      name: string;
      runtime: "node" | "python";
      source: string;
      envVars: { key: string; value: string }[];
    }) => {
      const { deploymentId } = await generateUploadUrl();
      await triggerDeployment(deploymentId);
      // Return a minimal object compatible with what NewDeployment.tsx expects
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
