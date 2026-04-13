import { describe, it, expect, vi, beforeEach } from "vitest";
import { apiFetch } from "./apiClient";
import { getCorrelationId, clearCorrelationId } from "./correlationId";

// Mock Amplify
vi.mock("aws-amplify/auth", () => ({
  fetchAuthSession: vi.fn().mockResolvedValue({
    tokens: {
      idToken: { toString: () => "mock-token" },
    },
  }),
}));

// Mock config
vi.mock("@/config", () => ({
  getApiBaseUrl: () => "http://localhost:8080",
}));

describe("apiClient", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    clearCorrelationId();
    global.fetch = vi.fn<typeof fetch>();
  });

  it("should capture X-Correlation-ID from response headers", async () => {
    const mockCorrelationId = "test-correlation-id";
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      headers: new Headers({
        "X-Correlation-ID": mockCorrelationId,
      }),
    } as Response);

    await apiFetch("/test");

    expect(getCorrelationId()).toBe(mockCorrelationId);
  });

  it("should include X-Correlation-ID in subsequent requests", async () => {
    const mockCorrelationId = "test-correlation-id";
    
    // Set the ID manually for the second request
    const { setCorrelationId } = await import("./correlationId");
    setCorrelationId(mockCorrelationId);
    
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      headers: new Headers(),
    } as Response);

    await apiFetch("/test-second");

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/test-second"),
      expect.objectContaining({
        headers: expect.objectContaining({
          "X-Correlation-ID": mockCorrelationId,
        }),
      })
    );
  });
});
