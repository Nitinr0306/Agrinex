"use client";

import "./globals.css";

/**
 * A failure of the root layout itself: no language, session or styles from the app are available, so the message
 * is given in all three languages (role file 02 E19).
 */
export default function GlobalError({ error, retry }: { error: Error & { digest?: string }; retry: () => void }) {
  return (
    <html lang="en">
      <body>
        <title>AgriNex</title>
        <main className="mx-auto flex min-h-dvh max-w-xl flex-col justify-center gap-4 px-4">
          <p className="text-title font-bold text-net">AgriNex</p>
          <p className="text-body">Something went wrong on our side. Please try again.</p>
          <p className="text-body" lang="mr">आमच्या बाजूने काहीतरी चुकले. कृपया पुन्हा प्रयत्न करा.</p>
          <p className="text-body" lang="hi">हमारी ओर से कुछ गड़बड़ हुई। कृपया फिर से कोशिश करें।</p>
          <button
            type="button"
            onClick={() => retry()}
            className="min-h-12 w-fit rounded-control bg-action px-5 font-semibold text-inverse"
          >
            Try again · पुन्हा प्रयत्न करा · फिर से कोशिश करें
          </button>
          {error.digest ? <p className="text-caption text-ink-2">Reference: {error.digest}</p> : null}
        </main>
      </body>
    </html>
  );
}
