import { fetchAuthSession } from "aws-amplify/auth";
import { getApiBaseUrl } from "@/config";

/**
 * Authenticated fetch wrapper.
 * Automatically attaches the Amplify ID-token as Bearer Authorization header.
 * Throws on non-2xx responses.
 */
export async function apiFetch(
  path: string,
  init: RequestInit = {}
): Promise<Response> {
  const session = await fetchAuthSession();
  const token =
    session.tokens?.idToken?.toString() ??
    session.tokens?.accessToken?.toString();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init.headers as Record<string, string>),
  };

  if (token) {
    headers["Authorization"] = token;
  }

  const res = await fetch(`${getApiBaseUrl()}/api/v1${path}`, {
    ...init,
    headers,
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`API ${res.status}: ${body || res.statusText}`);
  }

  return res;
}
