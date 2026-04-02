import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

export interface AppNotification {
  id: string;
  message: string;
  time: string;
  read: boolean;
  deploymentId?: string;
}

let mockNotifications: AppNotification[] = [
  {
    id: "n1",
    message: "user-authentication-service deployed successfully",
    time: "3 min ago",
    read: false,
    deploymentId: "1",
  },
  {
    id: "n2",
    message: "ml-inference-endpoint deployed successfully",
    time: "1 hour ago",
    read: false,
    deploymentId: "3",
  },
  {
    id: "n3",
    message: "data-pipeline-worker deployed successfully",
    time: "5 hours ago",
    read: true,
    deploymentId: "5",
  },
];

const delay = (ms = 200) => new Promise((r) => setTimeout(r, ms));

export function useNotifications() {
  return useQuery({
    queryKey: ["notifications"],
    queryFn: async () => {
      await delay();
      return [...mockNotifications];
    },
  });
}

export function useMarkNotificationsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      await delay();
      mockNotifications = mockNotifications.map((n) => ({ ...n, read: true }));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
}
