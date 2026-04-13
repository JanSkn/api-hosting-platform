import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Index from "./Index";
import * as useDeploymentsHook from "@/hooks/useDeployments";
import type { Deployment } from "@/components/ProjectCard";

// Mock the hook and components
vi.mock("@/hooks/useDeployments", () => ({
  useDeployments: vi.fn(),
}));

vi.mock("@/components/DashboardLayout", () => ({
  DashboardLayout: ({ children }: { children: React.ReactNode }) => <div data-testid="dashboard-layout">{children}</div>,
}));

vi.mock("@/components/ProjectCard", () => ({
  ProjectCard: ({ deployment }: { deployment: Deployment }) => <div data-testid="project-card">{deployment.name}</div>,
}));

describe("Index (Dashboard) page", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should render loading state", () => {
    vi.mocked(useDeploymentsHook.useDeployments).mockReturnValue({
      data: undefined,
      isLoading: true,
    } as unknown as ReturnType<typeof useDeploymentsHook.useDeployments>);

    render(
      <BrowserRouter>
        <Index />
      </BrowserRouter>
    );

    // The component renders pulse animations when loading but also correctly maps elements.
    // It renders 6 dummy items
    const title = screen.getByText("My API Deployments");
    expect(title).toBeInTheDocument();
  });

  it("should render empty state when no deployments exist", () => {
    vi.mocked(useDeploymentsHook.useDeployments).mockReturnValue({
      data: [],
      isLoading: false,
    } as unknown as ReturnType<typeof useDeploymentsHook.useDeployments>);

    render(
      <BrowserRouter>
        <Index />
      </BrowserRouter>
    );

    expect(screen.getByText("No deployments yet")).toBeInTheDocument();
    expect(screen.getByText("Create your first deployment")).toBeInTheDocument();
  });

  it("should render a list of deployments", () => {
    const mockDeployments = [
      { deploymentId: "1", name: "Alpha API" },
      { deploymentId: "2", name: "Beta API" },
    ] as Deployment[];

    vi.mocked(useDeploymentsHook.useDeployments).mockReturnValue({
      data: mockDeployments,
      isLoading: false,
    } as unknown as ReturnType<typeof useDeploymentsHook.useDeployments>);

    render(
      <BrowserRouter>
        <Index />
      </BrowserRouter>
    );

    expect(screen.getByTestId("dashboard-layout")).toBeInTheDocument();
    expect(screen.getAllByTestId("project-card")).toHaveLength(2);
    expect(screen.getByText("Alpha API")).toBeInTheDocument();
    expect(screen.getByText("Beta API")).toBeInTheDocument();
    expect(screen.getByText("2 services deployed")).toBeInTheDocument();
  });
});
