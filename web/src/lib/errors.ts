/**
 * Extracts a human-readable message from an unknown error value.
 *
 * Handles ApiError / native Error instances (incl. Amplify auth errors, which are
 * Error subclasses with a populated `.message`) as well as plain strings, and
 * falls back to a generic message so the UI never shows an empty toast.
 */
export function getErrorMessage(
  error: unknown,
  fallback = "Something went wrong. Please try again."
): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  if (typeof error === "string" && error.trim()) {
    return error;
  }
  return fallback;
}