/**
 * One option as the result screens draw it, from either a server response (reported estimates) or the client
 * What-If (hypothetical estimates). The same ledger and sheet render both; `hypothetical` switches on the
 * reserved What-If treatment (role file 02 §6).
 */
import type { MarketComparisonResponse, ReasonCode, SellingOptionResponse } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";
import { labelsFor, type OptionLabels } from "./labels";
import type { WhatIfInputs, WhatIfResult } from "./whatif";

export interface OptionView {
  option: SellingOptionResponse;
  rank: number;
  trips: bigint;
  transport: Decimal;
  gross: Decimal;
  net: Decimal;
  reasonCodes: ReasonCode[];
  labels: OptionLabels;
  hypothetical: boolean;
  /** The comparison's own estimates, shown as "was" next to a hypothetical value. */
  reported: { transport: Decimal; net: Decimal } | null;
  /** The comparison's variety and grade filter: the series the option's trend was computed on. */
  filter: ComparisonFilter;
}

/**
 * The variety and grade filter a comparison applied, null meaning "any" (contract: MarketComparisonResponse
 * variety and grade). An option's trendDirection and changePercent7Day are computed on the daily series with this
 * filter, so its trend link must use it too, never the variety of the option's own representative observation.
 */
export interface ComparisonFilter {
  varietyId: string | null;
  gradeId: string | null;
}

export function filterOf(response: MarketComparisonResponse): ComparisonFilter {
  return { varietyId: response.variety?.id ?? null, gradeId: response.grade?.id ?? null };
}

export interface TransportBasis {
  ratePerKm: Decimal;
  minimumTripCharge: Decimal;
  vehicleCapacityKg: Decimal;
  quantityKg: Decimal;
  quantityInQuintal: Decimal;
  hypothetical: boolean;
}

export function optionsFromResponse(response: MarketComparisonResponse): OptionView[] {
  return response.options.map((option) => ({
    option,
    rank: option.rank,
    trips: BigInt(option.estimates.trips),
    transport: Decimal.of(option.estimates.estimatedTransportCost),
    gross: Decimal.of(option.estimates.estimatedGrossValue),
    net: Decimal.of(option.estimates.estimatedNetRealization),
    reasonCodes: option.reasonCodes,
    labels: labelsFor(response, option),
    hypothetical: false,
    reported: null,
    filter: filterOf(response),
  }));
}

/** The views of a What-If on `base`, the comparison it recalculates. */
export function optionsFromWhatIf(result: WhatIfResult, base: MarketComparisonResponse): OptionView[] {
  const filter = filterOf(base);
  const maxNet = result.options.reduce<Decimal | null>((max, entry) => (max === null || entry.net.gt(max) ? entry.net : max), null);
  return result.options.map((entry) => ({
    option: entry.option,
    rank: entry.rank,
    trips: entry.trips,
    transport: entry.transport,
    gross: entry.gross,
    net: entry.net,
    reasonCodes: entry.reasonCodes,
    labels: {
      recommended: false,
      whatIfTop: entry.rank === 1,
      highestPrice: entry.option.market.id === result.priceWinnerId,
      highestNet: maxNet !== null && entry.net.eq(maxNet),
    },
    hypothetical: true,
    reported: {
      transport: Decimal.of(entry.option.estimates.estimatedTransportCost),
      net: Decimal.of(entry.option.estimates.estimatedNetRealization),
    },
    filter,
  }));
}

export function basisFromResponse(response: MarketComparisonResponse): TransportBasis {
  const quantity = Decimal.of(response.quantity);
  const quantityKg = response.quantityUnit === "KG" ? quantity : quantity.mul(Decimal.ofInteger(100));
  return {
    ratePerKm: Decimal.of(response.transportAssumptions.appliedRatePerKm),
    minimumTripCharge: Decimal.of(response.transportAssumptions.minimumTripCharge),
    vehicleCapacityKg: Decimal.of(response.transportAssumptions.vehicleCapacityKg),
    quantityKg,
    quantityInQuintal: Decimal.of(response.quantityInQuintal),
    hypothetical: response.scenario.kind === "SIMULATION",
  };
}

export function basisFromWhatIf(response: MarketComparisonResponse, inputs: WhatIfInputs): TransportBasis {
  return {
    ratePerKm: inputs.ratePerKm,
    minimumTripCharge: Decimal.of(response.transportAssumptions.minimumTripCharge),
    vehicleCapacityKg: Decimal.of(response.transportAssumptions.vehicleCapacityKg),
    quantityKg: inputs.quantityKg,
    quantityInQuintal: inputs.quantityKg.mul(Decimal.of("0.01")),
    hypothetical: true,
  };
}

/** Whether the minimum trip charge decided this option's cost (per trip: max(minimum, distance × rate)). */
export function minimumApplies(view: OptionView, basis: TransportBasis): boolean {
  return Decimal.of(view.option.distanceKm).mul(basis.ratePerKm).lt(basis.minimumTripCharge);
}
