import { fetchAuthSession } from "aws-amplify/auth";
import { getApiBaseUrl } from "@/config";
import { getCorrelationId, setCorrelationId } from "./correlationId";

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

  const correlationId = getCorrelationId();
  if (correlationId) {
    headers["X-Correlation-ID"] = correlationId;
  }

  if (token) {
    headers["Authorization"] = token;
  }

  const res = await fetch(`${getApiBaseUrl()}/api/v1${path}`, {
    ...init,
    headers,
  });

  // Capture correlation ID from response header if present
  const respCorrelationId = res.headers.get("X-Correlation-ID");
  if (respCorrelationId) {
    setCorrelationId(respCorrelationId);
  }

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`API ${res.status}: ${body || res.statusText}`);
  }

  return res;
}
