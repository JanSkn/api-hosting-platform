let currentCorrelationId: string | null = null;

/**
 * Gets the current correlation ID stored in the frontend.
 */
export function getCorrelationId(): string | null {
  return currentCorrelationId;
}

/**
 * Sets the current correlation ID.
 */
export function setCorrelationId(id: string | null): void {
  currentCorrelationId = id;
}

/**
 * Clears the stored correlation ID.
 */
export function clearCorrelationId(): void {
  currentCorrelationId = null;
}
