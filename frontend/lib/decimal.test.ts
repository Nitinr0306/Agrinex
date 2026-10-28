import { describe, expect, it } from "vitest";
import { Decimal } from "./decimal";

describe("Decimal", () => {
  it("parses JSON numbers exactly through their shortest decimal form", () => {
    expect(Decimal.of(3037.4).toFixed(2)).toBe("3037.40");
    expect(Decimal.of(0.1).add(Decimal.of(0.2)).toString()).toBe("0.3");
    expect(Decimal.of(999999999999.99).toFixed(2)).toBe("999999999999.99");
    expect(Decimal.of(1e21).toString()).toBe("1000000000000000000000");
  });

  it("multiplies without rounding", () => {
    expect(Decimal.of("42.37").mul(Decimal.of("8.33")).toString()).toBe("352.9421");
  });

  it("rounds half-up away from zero, like the server's BigDecimal HALF_UP", () => {
    expect(Decimal.of("2.345").round(2).toFixed(2)).toBe("2.35");
    expect(Decimal.of("2.344").round(2).toFixed(2)).toBe("2.34");
    expect(Decimal.of("-2.345").round(2).toFixed(2)).toBe("-2.35");
    expect(Decimal.of("14663.5").round(0).toFixed(0)).toBe("14664");
  });

  it("floors towards negative infinity", () => {
    expect(Decimal.of("6.259").floor(2).toFixed(2)).toBe("6.25");
    expect(Decimal.of("-6.251").floor(2).toFixed(2)).toBe("-6.26");
  });

  it("computes trips as a ceiling division", () => {
    expect(Decimal.of(500).ceilDiv(Decimal.of(1000))).toBe(1n);
    expect(Decimal.of(1000).ceilDiv(Decimal.of(1000))).toBe(1n);
    expect(Decimal.of("1000.001").ceilDiv(Decimal.of(1000))).toBe(2n);
    expect(Decimal.of(2500).ceilDiv(Decimal.of("1000.00"))).toBe(3n);
  });

  it("divides with flooring at the requested scale", () => {
    expect(Decimal.of(800).divFloor(Decimal.of(128), 4).toFixed(4)).toBe("6.2500");
    expect(Decimal.of(450).divFloor(Decimal.of(128), 4).toFixed(4)).toBe("3.5156");
    expect(Decimal.of(-1).divFloor(Decimal.of(3), 2).toFixed(2)).toBe("-0.34");
  });

  it("compares across scales", () => {
    expect(Decimal.of("3000").eq(Decimal.of("3000.00"))).toBe(true);
    expect(Decimal.of("14476.00").lt(Decimal.of(14664))).toBe(true);
    expect(Decimal.max(Decimal.of(300), Decimal.of("336.00")).toFixed(2)).toBe("336.00");
  });

  it("rejects values that are not plain decimals", () => {
    expect(() => Decimal.of("1,000")).toThrow(RangeError);
    expect(() => Decimal.of(Number.NaN)).toThrow(RangeError);
  });
});
