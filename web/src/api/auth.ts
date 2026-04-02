import { USE_MOCK_DATA, API_BASE_URL } from "./config";
import { mockApi, type User } from "./mock-data";

export async function login(email: string, password: string): Promise<User> {
  if (USE_MOCK_DATA) return mockApi.login(email, password);
  const res = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) throw new Error("Login failed");
  return res.json();
}

export async function register(name: string, email: string, password: string): Promise<User> {
  if (USE_MOCK_DATA) return mockApi.register(name, email, password);
  const res = await fetch(`${API_BASE_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, email, password }),
  });
  if (!res.ok) throw new Error("Registration failed");
  return res.json();
}

export async function logout(): Promise<void> {
  if (USE_MOCK_DATA) return mockApi.logout();
  await fetch(`${API_BASE_URL}/auth/logout`, { method: "POST" });
}

export async function getCurrentUser(): Promise<User | null> {
  if (USE_MOCK_DATA) return mockApi.getCurrentUser();
  const res = await fetch(`${API_BASE_URL}/auth/me`);
  if (!res.ok) return null;
  return res.json();
}

export async function updateProfile(data: { name: string }): Promise<User> {
  if (USE_MOCK_DATA) return mockApi.updateProfile(data);
  const res = await fetch(`${API_BASE_URL}/auth/profile`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to update profile");
  return res.json();
}

export async function deleteAccount(): Promise<void> {
  if (USE_MOCK_DATA) return mockApi.deleteAccount();
  const res = await fetch(`${API_BASE_URL}/auth/account`, { method: "DELETE" });
  if (!res.ok) throw new Error("Failed to delete account");
}
