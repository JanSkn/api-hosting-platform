import type { Deployment } from "@/components/ProjectCard";

export interface BuildLog {
  time: string;
  msg: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
}

let mockDeployments: Deployment[] = [
  {
    id: "1",
    name: "user-authentication-service",
    status: "live",
    url: "https://a1b2c3.lambda-url.us-east-1.on.aws",
    lastDeployed: "Deployed 3 min ago",
    runtime: "node",
  },
  {
    id: "2",
    name: "payment-gateway-api",
    status: "building",
    url: "https://d4e5f6.lambda-url.eu-west-1.on.aws",
    lastDeployed: "Deployed 12 min ago",
    runtime: "node",
  },
  {
    id: "3",
    name: "ml-inference-endpoint",
    status: "live",
    url: "https://g7h8i9.lambda-url.us-west-2.on.aws",
    lastDeployed: "Deployed 1 hour ago",
    runtime: "python",
  },
  {
    id: "4",
    name: "notification-service",
    status: "error",
    url: "https://j0k1l2.lambda-url.us-east-1.on.aws",
    lastDeployed: "Deployed 2 hours ago",
    runtime: "node",
  },
  {
    id: "5",
    name: "data-pipeline-worker",
    status: "live",
    url: "https://m3n4o5.lambda-url.ap-south-1.on.aws",
    lastDeployed: "Deployed 5 hours ago",
    runtime: "python",
  },
  {
    id: "6",
    name: "image-resize-api",
    status: "live",
    url: "https://p6q7r8.lambda-url.us-east-1.on.aws",
    lastDeployed: "Deployed 1 day ago",
    runtime: "node",
  },
];

const mockBuildLogs: BuildLog[] = [
  { time: "10:31:02", msg: "Build started..." },
  { time: "10:31:03", msg: "Validating configuration..." },
  { time: "10:31:05", msg: "Cloning repository..." },
  { time: "10:31:08", msg: "Installing dependencies..." },
  { time: "10:31:12", msg: "Dependencies installed (42 packages)" },
  { time: "10:31:15", msg: "Building Docker image..." },
  { time: "10:31:28", msg: "Image built successfully (128MB)" },
  { time: "10:31:30", msg: "Pushing image to ECR..." },
  { time: "10:31:45", msg: "Image pushed to registry" },
  { time: "10:31:48", msg: "Provisioning Lambda function..." },
  { time: "10:31:55", msg: "Configuring API Gateway..." },
  { time: "10:32:02", msg: "Running health checks..." },
  { time: "10:32:08", msg: "Health check passed ✓" },
  { time: "10:32:10", msg: "Deployment complete!" },
];

let mockCurrentUser: User | null = {
  id: "user-1",
  name: "John Doe",
  email: "john@example.com",
};

// Simulate network delay
const delay = (ms = 300) => new Promise((r) => setTimeout(r, ms));

export const mockApi = {
  // Deployments
  async getDeployments(): Promise<Deployment[]> {
    await delay();
    return [...mockDeployments];
  },

  async getDeployment(id: string): Promise<Deployment | undefined> {
    await delay();
    return mockDeployments.find((d) => d.id === id);
  },

  async createDeployment(data: {
    name: string;
    runtime: "node" | "python";
    source: string;
    envVars: { key: string; value: string }[];
  }): Promise<Deployment> {
    await delay(500);
    const newDeployment: Deployment = {
      id: `dep-${Date.now()}`,
      name: data.name,
      status: "building",
      url: `https://${Math.random().toString(36).slice(2, 8)}.lambda-url.us-east-1.on.aws`,
      lastDeployed: "Deployed just now",
      runtime: data.runtime,
    };
    mockDeployments = [newDeployment, ...mockDeployments];
    return newDeployment;
  },

  async deleteDeployment(id: string): Promise<void> {
    await delay(400);
    mockDeployments = mockDeployments.filter((d) => d.id !== id);
  },

  async getBuildLogs(_id: string): Promise<BuildLog[]> {
    await delay();
    return [...mockBuildLogs];
  },

  // Auth
  async login(email: string, _password: string): Promise<User> {
    await delay(500);
    mockCurrentUser = { id: "user-1", name: "John Doe", email };
    return mockCurrentUser;
  },

  async register(name: string, email: string, _password: string): Promise<User> {
    await delay(500);
    mockCurrentUser = { id: `user-${Date.now()}`, name, email };
    return mockCurrentUser;
  },

  async logout(): Promise<void> {
    await delay(200);
    mockCurrentUser = null;
  },

  async getCurrentUser(): Promise<User | null> {
    await delay();
    return mockCurrentUser;
  },

  // Account
  async updateProfile(data: { name: string }): Promise<User> {
    await delay(400);
    if (mockCurrentUser) {
      mockCurrentUser = { ...mockCurrentUser, ...data };
    }
    return mockCurrentUser!;
  },

  async deleteAccount(): Promise<void> {
    await delay(500);
    mockCurrentUser = null;
  },
};
