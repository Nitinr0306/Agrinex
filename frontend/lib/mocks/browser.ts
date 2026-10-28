import { setupWorker } from "msw/browser";
import { buildHandlers, captureScenarioChoice } from "./handlers";

/** Starts the mock-mode service worker; resolves once requests are intercepted. */
export async function startMockWorker(): Promise<void> {
  captureScenarioChoice(window.location.search);
  const worker = setupWorker(...buildHandlers());
  await worker.start({
    onUnhandledRequest: "bypass",
    serviceWorker: { url: "/mockServiceWorker.js" },
    quiet: true,
  });
}
