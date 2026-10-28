/**
 * Client-side What-If (spec §17, §32; ADR-0026; FRONTEND_CONSUMPTION.md §3). Applies the full §32 formula to the
 * options of a comparison response with the user's hypothetical quantity, rate and radius, in exact decimal
 * arithmetic (API_CONVENTIONS.md §4.1), and ranks by estimated net realization only. Reported prices are never
 * changed and composite scores are never recomputed. Raising the radius above the comparison's is not handled
 * here: that is server mode (`simulateSellingOptions`).
 */
import type { ExcludedMarket, MarketComparisonResponse, ReasonCode, SellingOptionResponse, SimulateRequest } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";

/** Slider bounds (spec §17; ADR-0026 item 4). */
export const WHAT_IF_BOUNDS = {
  quantityKg: { min: 100, max: 5000 },
  ratePerKm: { min: 3, max: 20 },
  maxDistanceKm: { min: 25, max: 300 },
} as const;

const REASON_ORDER: readonly ReasonCode[] = [
  "PRICE_ADVANTAGE",
  "LOWER_TRANSPORT",
  "POSITIVE_TREND",
  "STABLE_TREND",
  "RECENT_DATA",
  "SUITABLE_DISTANCE",
  "NEGATIVE_TREND",
  "STALE_DATA",
];

export interface WhatIfInputs {
  quantityKg: Decimal;
  ratePerKm: Decimal;
  maxDistanceKm: number;
}

export interface WhatIfOption {
  option: SellingOptionResponse;
  rank: number;
  trips: bigint;
  transport: Decimal;
  gross: Decimal;
  net: Decimal;
  reasonCodes: ReasonCode[];
}

export interface Breakeven {
  /** Below this rate per km, P's estimated net realization would be higher than R's (floored to paise). */
  ratePerKm: Decimal;
  priceWinner: SellingOptionResponse;
  rankOne: SellingOptionResponse;
}

export interface WhatIfResult {
  /** "ok", or the two server empty states of ADR-0023 item 3 recomputed for the narrower radius. */
  state: "ok" | "noRecentData" | "noMarketInRange";
  options: WhatIfOption[];
  excluded: ExcludedMarket[];
  netWinnerId: string | null;
  priceWinnerId: string | null;
  rankingsDisagree: boolean;
  breakeven: Breakeven | null;
}

/** The comparison's own values: what "Reset to reported" returns to. */
export function originalInputs(base: MarketComparisonResponse): WhatIfInputs {
  return {
    quantityKg: quantityInKg(base),
    ratePerKm: Decimal.of(base.transportAssumptions.appliedRatePerKm),
    maxDistanceKm: base.maxDistanceKm,
  };
}

/** Values the user moved; null keeps the comparison's value (FRONTEND_CONSUMPTION §3). */
export interface WhatIfOverrides {
  ratePerKm: Decimal | null;
  quantityKg: Decimal | null;
  maxDistanceKm: number | null;
}

export const NO_OVERRIDES: WhatIfOverrides = { ratePerKm: null, quantityKg: null, maxDistanceKm: null };

/**
 * The server-mode request (ADR-0026 item 3): the raised radius plus only the values that differ from the original
 * comparison's. An omitted value means "keep the original value" (contract SimulateRequest), so a slider moved
 * back to where it started is not sent. Quantity is in kilograms.
 */
export function buildSimulateRequest(
  comparison: MarketComparisonResponse,
  overrides: WhatIfOverrides,
  maxDistanceKm: number,
): SimulateRequest {
  const original = originalInputs(comparison);
  const request: SimulateRequest = { recommendationId: comparison.recommendationId, maxDistanceKm };
  if (overrides.quantityKg && !overrides.quantityKg.eq(original.quantityKg)) {
    request.quantity = overrides.quantityKg.toNumber();
  }
  if (overrides.ratePerKm && !overrides.ratePerKm.eq(original.ratePerKm)) {
    request.transportRatePerKm = overrides.ratePerKm.toNumber();
  }
  return request;
}

export function quantityInKg(base: MarketComparisonResponse): Decimal {
  const quantity = Decimal.of(base.quantity);
  return base.quantityUnit === "KG" ? quantity : quantity.mul(Decimal.ofInteger(100));
}

export function recalculate(base: MarketComparisonResponse, inputs: WhatIfInputs): WhatIfResult {
  const capacity = Decimal.of(base.transportAssumptions.vehicleCapacityKg);
  const minimumTrip = Decimal.of(base.transportAssumptions.minimumTripCharge);
  const perQuintal = Decimal.of("0.01");

  const kept = base.options.filter((option) => option.distanceKm <= inputs.maxDistanceKm);
  const excluded = base.excludedMarkets.filter((market) => market.distanceKm <= inputs.maxDistanceKm);
  if (kept.length === 0) {
    return {
      state: excluded.length > 0 ? "noRecentData" : "noMarketInRange",
      options: [],
      excluded,
      netWinnerId: null,
      priceWinnerId: null,
      rankingsDisagree: false,
      breakeven: null,
    };
  }

  const trips = inputs.quantityKg.ceilDiv(capacity);
  const tripsDecimal = Decimal.ofInteger(trips);
  const computed = kept.map((option) => {
    const perTrip = Decimal.max(minimumTrip, Decimal.of(option.distanceKm).mul(inputs.ratePerKm));
    const transport = perTrip.mul(tripsDecimal).round(2);
    const gross = Decimal.of(option.price.modalPrice).mul(inputs.quantityKg).mul(perQuintal).round(2);
    return { option, trips, transport, gross, net: gross.sub(transport) };
  });

  computed.sort((a, b) =>
    b.net.compare(a.net)
    || a.option.distanceKm - b.option.distanceKm
    || compareNames(a.option.market.name, b.option.market.name));

  const advantage = topQuarterReasons(computed);
  const options: WhatIfOption[] = computed.map((entry, index) => ({
    ...entry,
    rank: index + 1,
    reasonCodes: reasonsWith(entry.option.reasonCodes, advantage.get(entry.option.market.id) ?? []),
  }));

  const rankOne = options[0];
  const priceWinner = [...options].sort((a, b) =>
    Decimal.of(b.option.price.modalPrice).compare(Decimal.of(a.option.price.modalPrice))
    || b.net.compare(a.net)
    || a.option.distanceKm - b.option.distanceKm
    || compareNames(a.option.market.name, b.option.market.name))[0];

  return {
    state: "ok",
    options,
    excluded,
    netWinnerId: rankOne.option.market.id,
    priceWinnerId: priceWinner.option.market.id,
    rankingsDisagree: priceWinner.option.market.id !== rankOne.option.market.id,
    breakeven: breakevenOf(rankOne, priceWinner, minimumTrip),
  };
}

/** Code-point order of market names (API_CONVENTIONS.md §5; the server's String.compareTo). */
function compareNames(a: string, b: string): number {
  return a < b ? -1 : a > b ? 1 : 0;
}

/**
 * PRICE_ADVANTAGE and LOWER_TRANSPORT over the kept options (ADR-0024 item 6): at least the modal at position
 * ⌈N/4⌉ in descending order; at most the transport at position ⌈N/4⌉ in ascending order.
 */
function topQuarterReasons(entries: { option: SellingOptionResponse; transport: Decimal }[]): Map<string, ReasonCode[]> {
  const position = Math.ceil(entries.length / 4) - 1;
  const modals = entries.map((entry) => Decimal.of(entry.option.price.modalPrice)).sort((a, b) => b.compare(a));
  const costs = entries.map((entry) => entry.transport).sort((a, b) => a.compare(b));
  const priceThreshold = modals[position];
  const costThreshold = costs[position];
  const result = new Map<string, ReasonCode[]>();
  for (const entry of entries) {
    const codes: ReasonCode[] = [];
    if (Decimal.of(entry.option.price.modalPrice).gte(priceThreshold)) codes.push("PRICE_ADVANTAGE");
    if (entry.transport.lte(costThreshold)) codes.push("LOWER_TRANSPORT");
    result.set(entry.option.market.id, codes);
  }
  return result;
}

/** Keeps the option's own codes, replaces the two set-dependent ones, in the order of the enumeration. */
function reasonsWith(original: ReasonCode[], recomputed: ReasonCode[]): ReasonCode[] {
  const own = original.filter((code) => code !== "PRICE_ADVANTAGE" && code !== "LOWER_TRANSPORT");
  const all = new Set<ReasonCode>([...own, ...recomputed]);
  return REASON_ORDER.filter((code) => all.has(code));
}

/**
 * The break-even rate between the rank-1 option R and the highest-price option P (spec §17 G), from the same
 * per-option values: f(r) = (grossP − grossR) − trips × (max(m, dP·r) − max(m, dR·r)) is non-increasing in r,
 * so the rate where it reaches 0 is unique. Shown only when it falls inside the simulator's rate range.
 */
function breakevenOf(rankOne: WhatIfOption, priceWinner: WhatIfOption, minimumTrip: Decimal): Breakeven | null {
  if (rankOne.option.market.id === priceWinner.option.market.id) return null;
  const grossGap = priceWinner.gross.sub(rankOne.gross);
  const dP = Decimal.of(priceWinner.option.distanceKm);
  const dR = Decimal.of(rankOne.option.distanceKm);
  if (!grossGap.isPositive() || !dP.gt(dR)) return null;

  const trips = Decimal.ofInteger(rankOne.trips);
  const m = minimumTrip;
  // Segment where only P pays more than the minimum: r = (gap + m·t) / (t·dP), valid while dR·r ≤ m.
  const numerator = grossGap.add(m.mul(trips));
  const insideSecondSegment = dR.isZero() || dR.mul(numerator).lte(m.mul(trips).mul(dP));
  const rate = insideSecondSegment
    ? numerator.divFloor(trips.mul(dP), 4)
    : grossGap.divFloor(trips.mul(dP.sub(dR)), 4);
  const display = rate.floor(2);
  const { min, max } = WHAT_IF_BOUNDS.ratePerKm;
  if (display.lt(Decimal.ofInteger(min)) || display.gt(Decimal.ofInteger(max))) return null;
  return { ratePerKm: display, priceWinner: priceWinner.option, rankOne: rankOne.option };
}
