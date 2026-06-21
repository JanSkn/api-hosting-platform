export interface LogCacheEntry {
  rawLogs: { time: string; msg: string }[];
  visibleLogs: { time: string; msg: string }[];
  nextToken: string | undefined;
  isComplete: boolean;
}

export const logCache: Record<string, LogCacheEntry> = {};

export function clearLogCache() {
  for (const key in logCache) {
    delete logCache[key];
  }
}
