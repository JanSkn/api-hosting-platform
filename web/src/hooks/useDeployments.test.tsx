import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useDeployments, useCreateDeployment } from "./useDeployments";
import * as api from "@/api/deployments";
import * as correlationIdModule from "@/api/correlationId";
import React from "react";

// Mock the API module
vi.mock("@/api/deployments", () => ({
  fetchDeployments: vi.fn(),
  initializeDeployment: vi.fn(),
  triggerDeployment: vi.fn(),
  updateDeploymentStatus: vi.fn(),
  generateUploadUrl: vi.fn(),
}));

vi.mock("@/api/correlationId", () => ({
  clearCorrelationId: vi.fn(),
  getCorrelationId: vi.fn(),
  setCorrelationId: vi.fn(),
}));

const queryClient = new QueryClient();

const wrapper = ({ children }: { children: React.ReactNode }) => (
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
    ] as unknown as Awaited<ReturnType<typeof api.fetchDeployments>>;

    vi.mocked(api.fetchDeployments).mockResolvedValue(mockDeployments);

    const { result } = renderHook(() => useDeployments(), { wrapper });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockDeployments);
    expect(api.fetchDeployments).toHaveBeenCalledTimes(1);
  });
});

describe("useCreateDeployment hook", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryClient.clear();
  });

  it("should clear correlation ID before and after deployment flow", async () => {
    const mockDeploymentId = "test-123";
    vi.mocked(api.initializeDeployment).mockResolvedValue({ deploymentId: mockDeploymentId });
    vi.mocked(api.triggerDeployment).mockResolvedValue(undefined as unknown as void);

    const { result } = renderHook(() => useCreateDeployment(), { wrapper });

    await result.current.mutateAsync({
      name: "test-api",
      runtime: "node",
      source: "https://github.com/user/repo",
      envVars: [],
    });

    // Should clear at the start
    expect(correlationIdModule.clearCorrelationId).toHaveBeenCalled();

    // Should call initialize
    expect(api.initializeDeployment).toHaveBeenCalledWith(expect.objectContaining({
      name: "test-api"
    }));

    // Should call trigger
    expect(api.triggerDeployment).toHaveBeenCalledWith(mockDeploymentId);

    // Should clear at the end
    expect(correlationIdModule.clearCorrelationId).toHaveBeenCalledTimes(2);
  });
});
