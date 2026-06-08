import { fetchAuthSession } from "aws-amplify/auth";
import { getApiBaseUrl } from "@/config";
import { getCorrelationId, setCorrelationId } from "./correlationId";

/**
 * Error thrown for non-2xx API responses. Carries the HTTP status and, when the
 * backend returns a JSON body of the shape { error, message }, the machine-readable
 * `code` and the human-readable `message`.
 */
export class ApiError extends Error {
  status: number;
  code?: string;

  constructor(status: number, message: string, code?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}

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
    let message = body || res.statusText;
    let code: string | undefined;

    // The backend returns errors as JSON { error: "<CODE>", message: "<text>" }.
    // Prefer the human-readable message so the UI can show something useful.
    try {
      const parsed = JSON.parse(body);
      if (parsed && typeof parsed === "object") {
        message = parsed.message || parsed.error || message;
        code = typeof parsed.error === "string" ? parsed.error : undefined;
      }
    } catch {
      // Body wasn't JSON – fall back to the raw text / status text.
    }

    throw new ApiError(res.status, message, code);
  }

  return res;
}
