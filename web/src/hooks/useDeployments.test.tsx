import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useDeployments } from "./useDeployments";
import * as api from "@/api/deployments";

// Mock the API module
vi.mock("@/api/deployments", () => ({
  fetchDeployments: vi.fn(),
}));

const queryClient = new QueryClient();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const wrapper = ({ children }: { children: any }) => (
  <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
);

describe("useDeployments hook", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryClient.clear();
  });

  it("should fetch deployments successfully", async () => {
    const mockDeployments = [
      { deploymentId: "1", name: "Test API", runtime: "NODEJS_18_X", status: "LIVE" },
    ];
    
    (api.fetchDeployments as any).mockResolvedValue(mockDeployments);

    const { result } = renderHook(() => useDeployments(), { wrapper });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockDeployments);
    expect(api.fetchDeployments).toHaveBeenCalledTimes(1);
  });
});
