import { IBM_Plex_Mono, IBM_Plex_Sans, IBM_Plex_Sans_Devanagari } from "next/font/google";

/*
 * The approved Hisaab typefaces (role file 02 §1.1, §14.2): IBM Plex Sans and IBM Plex Sans Devanagari for text,
 * IBM Plex Mono for figures. next/font downloads them at build time and serves them from this origin (no
 * third-party request at runtime, so the CSP keeps font-src 'self'), with font-display: swap so text never waits
 * for a font (role file 02 §13).
 */
export const plexSans = IBM_Plex_Sans({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-plex-sans",
});

export const plexDevanagari = IBM_Plex_Sans_Devanagari({
  subsets: ["devanagari"],
  weight: ["400", "500", "600"],
  display: "swap",
  variable: "--font-plex-deva",
  // Only Marathi and Hindi screens need it; the browser fetches it when Devanagari text appears.
  preload: false,
});

export const plexMono = IBM_Plex_Mono({
  subsets: ["latin"],
  weight: ["400", "500", "600"],
  display: "swap",
  variable: "--font-plex-mono",
});
