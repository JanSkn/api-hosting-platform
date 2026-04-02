import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  fetchDeployments,
  fetchDeployment,
  createDeployment,
  deleteDeployment,
  fetchBuildLogs,
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

export function useCreateDeployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createDeployment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
    },
  });
}

export function useDeleteDeployment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteDeployment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
    },
  });
}
