import { describe, expect, it } from "vitest";
import { isValidEmail, isValidPhoneDigits, normalizeIdentifier, passwordProblem, safeNextPath } from "./identifiers";

describe("account input rules", () => {
  it("normalizes a typed Indian mobile number to E.164 and leaves an email as typed", () => {
    expect(normalizeIdentifier("98765 43210")).toBe("+919876543210");
    expect(normalizeIdentifier("919876543210")).toBe("+919876543210");
    expect(normalizeIdentifier("+91 98765-43210")).toBe("+919876543210");
    expect(normalizeIdentifier("  Farmer@Example.invalid ")).toBe("Farmer@Example.invalid");
    expect(normalizeIdentifier("12345")).toBe("12345");
  });

  it("accepts only ten digits starting with 6–9 after +91", () => {
    expect(isValidPhoneDigits("9876543210")).toBe(true);
    expect(isValidPhoneDigits("5876543210")).toBe(false);
    expect(isValidPhoneDigits("987654321")).toBe(false);
  });

  it("checks emails like the contract pattern", () => {
    expect(isValidEmail("a@b")).toBe(true);
    expect(isValidEmail("no-at-sign")).toBe(false);
    expect(isValidEmail("two@@signs")).toBe(false);
  });

  it("counts password characters and UTF-8 bytes (8–64 characters, at most 72 bytes)", () => {
    expect(passwordProblem("short")).toBe("TOO_SHORT");
    expect(passwordProblem("any characters ok")).toBeNull();
    expect(passwordProblem("x".repeat(65))).toBe("TOO_LONG");
    // 25 Devanagari letters are 25 characters but 75 bytes.
    expect(passwordProblem("क".repeat(25))).toBe("TOO_LONG");
    expect(passwordProblem("admin-password", 15)).toBe("TOO_SHORT");
  });

  it("follows only same-site paths after sign-in", () => {
    expect(safeNextPath("/alerts")).toBe("/alerts");
    expect(safeNextPath("//evil.example")).toBeNull();
    expect(safeNextPath("https://evil.example")).toBeNull();
    expect(safeNextPath(null)).toBeNull();
  });

  it("refuses paths that a browser would resolve to another origin", () => {
    // The URL parser strips tab and newline before parsing, so these become "//evil.example".
    expect(safeNextPath("/\t/evil.example")).toBeNull();
    expect(safeNextPath("/\n/evil.example")).toBeNull();
    expect(safeNextPath("/\r/evil.example")).toBeNull();
    // Browsers treat a backslash like a slash in special URLs.
    expect(safeNextPath("/\\evil.example")).toBeNull();
    expect(safeNextPath("/ /evil.example")).toBeNull();
  });

  it("keeps the query and fragment of a same-site path", () => {
    expect(safeNextPath("/alerts/new?crop=c1&market=m1")).toBe("/alerts/new?crop=c1&market=m1");
    expect(safeNextPath("/admin/runs/r1")).toBe("/admin/runs/r1");
  });
});
