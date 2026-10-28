import type { AlertType } from "@/lib/api/types";
import type { Formatter } from "@/lib/i18n/format";
import type { Translate } from "@/lib/i18n/i18n-provider";

export const ALERT_TYPES: readonly AlertType[] = ["PRICE_ABOVE", "PRICE_BELOW", "PERCENTAGE_RISE", "PERCENTAGE_FALL"];

export function isPriceAlert(type: AlertType): boolean {
  return type === "PRICE_ABOVE" || type === "PRICE_BELOW";
}

/** A threshold as the farmer typed it: ₹ per quintal, or a percentage magnitude (contract: up to 4 decimals). */
export function formatThreshold(type: AlertType, value: number, fmt: Formatter): string {
  return isPriceAlert(type) ? `₹${fmt.decimal(value, 4)}` : `${fmt.decimal(value, 4)}%`;
}

/** The rule in words: "Price at or above ₹3,000 per quintal" (role file 02 S11). */
export function ruleText(type: AlertType, value: number, t: Translate, fmt: Formatter): string {
  return t(`alerts.type.${type}`, { threshold: formatThreshold(type, value, fmt) });
}
