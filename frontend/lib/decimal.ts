/**
 * Exact decimal arithmetic for the client-side What-If and the trade-off sentence (API_CONVENTIONS.md §4.1:
 * "use a decimal library or integer paise arithmetic. Never compare floating-point results directly").
 *
 * A value is an integer number of units at a decimal scale (value = units / 10^scale), held in a BigInt, so
 * addition, subtraction and multiplication are exact. Rounding is HALF_UP (away from zero on a tie), as in
 * the server's BigDecimal arithmetic.
 */
export class Decimal {
  private constructor(
    readonly units: bigint,
    readonly scale: number,
  ) {}

  static readonly ZERO = new Decimal(0n, 0);

  /**
   * Parses a JSON number or a decimal string. A JSON number with at most 15 significant digits converts
   * exactly, because `String(n)` gives the shortest decimal that round-trips to the same double.
   */
  static of(value: number | string): Decimal {
    const text = typeof value === "number" ? numberToPlainString(value) : value.trim();
    const match = /^([+-]?)(\d+)(?:\.(\d*))?$/.exec(text);
    if (!match) {
      throw new RangeError(`Not a plain decimal: ${text}`);
    }
    const [, sign, whole, fraction = ""] = match;
    const units = BigInt(whole + fraction);
    return new Decimal(sign === "-" ? -units : units, fraction.length);
  }

  static ofInteger(value: number | bigint): Decimal {
    return new Decimal(BigInt(value), 0);
  }

  add(other: Decimal): Decimal {
    const [a, b, scale] = align(this, other);
    return new Decimal(a + b, scale);
  }

  sub(other: Decimal): Decimal {
    const [a, b, scale] = align(this, other);
    return new Decimal(a - b, scale);
  }

  mul(other: Decimal): Decimal {
    return new Decimal(this.units * other.units, this.scale + other.scale);
  }

  neg(): Decimal {
    return new Decimal(-this.units, this.scale);
  }

  abs(): Decimal {
    return this.units < 0n ? this.neg() : this;
  }

  compare(other: Decimal): number {
    const [a, b] = align(this, other);
    return a === b ? 0 : a < b ? -1 : 1;
  }

  eq(other: Decimal): boolean {
    return this.compare(other) === 0;
  }

  gt(other: Decimal): boolean {
    return this.compare(other) > 0;
  }

  lt(other: Decimal): boolean {
    return this.compare(other) < 0;
  }

  gte(other: Decimal): boolean {
    return this.compare(other) >= 0;
  }

  lte(other: Decimal): boolean {
    return this.compare(other) <= 0;
  }

  isZero(): boolean {
    return this.units === 0n;
  }

  isNegative(): boolean {
    return this.units < 0n;
  }

  isPositive(): boolean {
    return this.units > 0n;
  }

  static max(a: Decimal, b: Decimal): Decimal {
    return a.gte(b) ? a : b;
  }

  /** Rounds half-up (away from zero on a tie) to the given number of decimal places. */
  round(scale: number): Decimal {
    return this.rescale(scale, "halfUp");
  }

  /** Rounds towards negative infinity to the given number of decimal places. */
  floor(scale: number): Decimal {
    return this.rescale(scale, "floor");
  }

  /** The smallest integer not less than this / divisor, for positive values (trips = ⌈kg ÷ capacity⌉). */
  ceilDiv(divisor: Decimal): bigint {
    if (!divisor.isPositive() || this.isNegative()) {
      throw new RangeError("ceilDiv needs a non-negative dividend and a positive divisor");
    }
    const [a, b] = align(this, divisor);
    return (a + b - 1n) / b;
  }

  /** this ÷ divisor, rounded towards negative infinity to the given scale. */
  divFloor(divisor: Decimal, scale: number): Decimal {
    if (divisor.isZero()) {
      throw new RangeError("Division by zero");
    }
    const [a, b] = align(this, divisor);
    const numerator = a * 10n ** BigInt(scale);
    let quotient = numerator / b;
    if ((numerator % b !== 0n) && (numerator < 0n) !== (b < 0n)) {
      quotient -= 1n;
    }
    return new Decimal(quotient, scale);
  }

  /** Plain decimal text with exactly `scale` places (no rounding when the scale already matches). */
  toFixed(scale: number): string {
    const value = this.scale === scale ? this : this.round(scale);
    const negative = value.units < 0n;
    const digits = (negative ? -value.units : value.units).toString().padStart(scale + 1, "0");
    const whole = scale === 0 ? digits : digits.slice(0, -scale);
    const fraction = scale === 0 ? "" : `.${digits.slice(-scale)}`;
    return `${negative ? "-" : ""}${whole}${fraction}`;
  }

  /** Plain decimal text without trailing zeros. */
  toString(): string {
    const text = this.toFixed(this.scale);
    return this.scale === 0 ? text : text.replace(/\.?0+$/, "");
  }

  toNumber(): number {
    return Number(this.toString());
  }

  private rescale(scale: number, mode: "halfUp" | "floor"): Decimal {
    if (scale >= this.scale) {
      return new Decimal(this.units * 10n ** BigInt(scale - this.scale), scale);
    }
    const factor = 10n ** BigInt(this.scale - scale);
    let quotient = this.units / factor;
    const remainder = this.units % factor;
    if (remainder !== 0n) {
      if (mode === "floor") {
        if (this.units < 0n) quotient -= 1n;
      } else {
        const twice = 2n * (remainder < 0n ? -remainder : remainder);
        if (twice >= factor) quotient += this.units < 0n ? -1n : 1n;
      }
    }
    return new Decimal(quotient, scale);
  }
}

function align(a: Decimal, b: Decimal): [bigint, bigint, number] {
  if (a.scale === b.scale) return [a.units, b.units, a.scale];
  if (a.scale > b.scale) return [a.units, b.units * 10n ** BigInt(a.scale - b.scale), a.scale];
  return [a.units * 10n ** BigInt(b.scale - a.scale), b.units, b.scale];
}

/** `String(n)` without exponent notation; API numbers are serialized without exponents (API_CONVENTIONS §4). */
function numberToPlainString(n: number): string {
  if (!Number.isFinite(n)) {
    throw new RangeError(`Not a finite number: ${n}`);
  }
  const text = String(n);
  if (!/e/i.test(text)) return text;
  const [mantissa, exponentText] = text.toLowerCase().split("e");
  const exponent = Number(exponentText);
  const negative = mantissa.startsWith("-");
  const [whole, fraction = ""] = mantissa.replace("-", "").split(".");
  const digits = whole + fraction;
  const point = whole.length + exponent;
  const plain =
    point <= 0
      ? `0.${"0".repeat(-point)}${digits}`
      : point >= digits.length
        ? digits + "0".repeat(point - digits.length)
        : `${digits.slice(0, point)}.${digits.slice(point)}`;
  return (negative ? "-" : "") + plain;
}
