import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

export interface AppNotification {
  id: string;
  message: string;
  time: string;
  read: boolean;
  deploymentId?: string;
}

// No backend notification endpoint exists yet – return empty list.
export function useNotifications() {
  return useQuery<AppNotification[]>({
    queryKey: ["notifications"],
    queryFn: async () => [],
    staleTime: Infinity,
  });
}

export function useMarkNotificationsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {},
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
}
