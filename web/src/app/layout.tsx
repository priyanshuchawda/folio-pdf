import type { Metadata } from "next";
import { Syne, Source_Serif_4 } from "next/font/google";
import "./globals.css";

const syne = Syne({
  variable: "--font-display",
  subsets: ["latin"],
  weight: ["500", "600", "700", "800"],
});

const sourceSerif = Source_Serif_4({
  variable: "--font-body",
  subsets: ["latin"],
  weight: ["400", "500", "600"],
});

export const metadata: Metadata = {
  metadataBase: new URL("https://folio-pdf.vercel.app"),
  title: {
    default: "Folio — Lite PDF for tablets",
    template: "%s · Folio",
  },
  description:
    "Ultra-light PDF reader for low-RAM Android tablets. Pdfium engine, go-to-page, Drive-style scrubber. Opens Telegram & Drive PDFs without the bloat.",
  openGraph: {
    title: "Folio — Lite PDF for tablets",
    description:
      "Fast 1000+ page reading on Fire HD–class devices. Download the Android APK.",
    type: "website",
    url: "https://folio-pdf.vercel.app",
  },
  twitter: {
    card: "summary_large_image",
    title: "Folio — Lite PDF for tablets",
    description: "Ultra-light Android PDF reader. Download the APK.",
  },
  keywords: [
    "PDF reader",
    "Android",
    "tablet",
    "Fire HD",
    "Telegram PDF",
    "low RAM",
    "Pdfium",
  ],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${syne.variable} ${sourceSerif.variable}`}>{children}</body>
    </html>
  );
}
