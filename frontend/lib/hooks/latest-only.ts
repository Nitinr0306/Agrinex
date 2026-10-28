/**
 * Orders asynchronous requests so that only the newest may update the screen: an older answer that arrives
 * late is dropped, and a second request for the same key while the first is pending is not sent. Used by the
 * What-If server mode, where each slider release can start a request (found in Task 12).
 */
export interface LatestOnly<K> {
  /** A ticket for a new request, or null when a request for the same key is already pending. */
  start(key: K): { isLatest: () => boolean; done: () => void } | null;
  /** Makes every pending request stale (for example on reset). */
  cancel(): void;
}

export function latestOnly<K>(): LatestOnly<K> {
  let current = 0;
  let pending: { id: number; key: K } | null = null;
  return {
    start(key) {
      if (pending !== null && pending.key === key && pending.id === current) return null;
      const id = ++current;
      pending = { id, key };
      return {
        isLatest: () => id === current,
        done: () => {
          if (pending?.id === id) pending = null;
        },
      };
    },
    cancel() {
      current++;
      pending = null;
    },
  };
}
