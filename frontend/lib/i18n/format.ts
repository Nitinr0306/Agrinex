/**
 * Number, money and date formatting for every language (role file 02 §11):
 * - PDD-06: Latin digits in all three languages, Indian digit grouping (₹1,02,450), rupee sign first.
 * - PDD-07: estimates to the whole rupee with "≈" (shown by the caller); reported prices exactly as reported,
 *   with paise only when non-zero (₹3,037.40); percentages to one decimal; distances to whole km.
 * - PDD-08: absolute dates ("2 Oct 2026") with localized month names; times 24-hour with "IST" (Asia/Kolkata).
 */
import { Decimal } from "@/lib/decimal";
import { intlLocale, type Locale } from "./config";

const MINUS = "−";
const IST = "Asia/Kolkata";

export interface Formatter {
  /** A reported amount as reported: ₹3,000 or ₹3,037.40. */
  reportedRupees(value: number | Decimal): string;
  /** A statistic to exactly two decimals: ₹3,390.00 (trend window averages). */
  rupees2(value: number | Decimal): string;
  /** An estimate rounded half-up to the whole rupee, without the "≈" (callers add it with <Approx>). */
  estimatedRupees(value: number | Decimal): string;
  /** A whole number with Indian grouping. */
  integer(value: number | bigint): string;
  /** A plain decimal with grouping and the given number of fraction digits at most. */
  decimal(value: number | Decimal, maxFractionDigits: number): string;
  /** Signed percentage to one decimal: +4.0%, −8.5%, 0.0%. */
  signedPercent(value: number): string;
  /** Whole kilometres (the "≈" and the unit word are added by the caller). */
  wholeKm(value: number): string;
  /** A calendar date (yyyy-MM-dd): "2 Oct 2026". */
  date(isoDate: string): string;
  /** An instant: "3 Oct 2026, 06:02 IST". */
  dateTime(isoInstant: string): string;
}

export function createFormatter(locale: Locale): Formatter {
  const tag = intlLocale(locale);
  const group0 = new Intl.NumberFormat(tag, { maximumFractionDigits: 0 });
  const group2 = new Intl.NumberFormat(tag, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const dateFormat = new Intl.DateTimeFormat(tag, { day: "numeric", month: "short", year: "numeric", timeZone: "UTC" });
  const instantDate = new Intl.DateTimeFormat(tag, { day: "numeric", month: "short", year: "numeric", timeZone: IST });
  const instantTime = new Intl.DateTimeFormat(tag, { hour: "2-digit", minute: "2-digit", hourCycle: "h23", timeZone: IST });

  const toDecimal = (value: number | Decimal) => (value instanceof Decimal ? value : Decimal.of(value));

  return {
    reportedRupees(value) {
      const exact = toDecimal(value).round(2);
      const hasPaise = !exact.eq(exact.round(0));
      return `₹${hasPaise ? group2.format(Number(exact.toFixed(2))) : group0.format(Number(exact.toFixed(0)))}`;
    },
    rupees2(value) {
      return `₹${group2.format(Number(toDecimal(value).round(2).toFixed(2)))}`;
    },
    estimatedRupees(value) {
      const whole = toDecimal(value).round(0);
      const text = group0.format(Number(whole.abs().toFixed(0)));
      return whole.isNegative() ? `${MINUS}₹${text}` : `₹${text}`;
    },
    integer(value) {
      return group0.format(typeof value === "bigint" ? Number(value) : value);
    },
    decimal(value, maxFractionDigits) {
      const rounded = toDecimal(value).round(maxFractionDigits);
      return new Intl.NumberFormat(tag, { maximumFractionDigits: maxFractionDigits }).format(Number(rounded.toString()));
    },
    signedPercent(value) {
      const rounded = Decimal.of(value).round(1);
      const text = rounded.abs().toFixed(1);
      if (rounded.isZero()) return `${text}%`;
      return `${rounded.isNegative() ? MINUS : "+"}${text}%`;
    },
    wholeKm(value) {
      return group0.format(Number(Decimal.of(value).round(0).toFixed(0)));
    },
    date(isoDate) {
      const [year, month, day] = isoDate.split("-").map(Number);
      return dateFormat.format(new Date(Date.UTC(year, month - 1, day)));
    },
    dateTime(isoInstant) {
      const instant = new Date(isoInstant);
      return `${instantDate.format(instant)}, ${instantTime.format(instant)} IST`;
    },
  };
}

/** Today's calendar date in India (yyyy-MM-dd); "today" in every freshness rule is the IST date (ADR-0007). */
export function todayInIndia(now: Date = new Date()): string {
  return new Intl.DateTimeFormat("en-CA", { timeZone: IST, year: "numeric", month: "2-digit", day: "2-digit" })
    .format(now);
}
