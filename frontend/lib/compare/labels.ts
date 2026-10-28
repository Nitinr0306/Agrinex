/**
 * Recommendation labels and the winner-callout trade-off (ADR-0045; FRONTEND_CONSUMPTION.md §2.1). Every label is
 * computed from response fields only, so it is true for every response; none of them re-ranks anything, and the
 * composite score is never shown or recomputed.
 */
import type { MarketComparisonResponse, ReasonCode, SellingOptionResponse } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";

export interface OptionLabels {
  /** "Recommended": rank 1 of a comparison (a simulation's rank 1 is "Highest estimated net (what-if)"). */
  recommended: boolean;
  /** "Highest estimated net (what-if)": rank 1 when the ranking is by estimated net realization only. */
  whatIfTop: boolean;
  /** "Highest price": the option of `priceOnlyWinnerId`. */
  highestPrice: boolean;
  /** "Highest estimated net realization": every option whose estimate equals the maximum (ties: all). */
  highestNet: boolean;
}

export function labelsFor(response: MarketComparisonResponse, option: SellingOptionResponse): OptionLabels {
  const simulation = response.scenario.kind === "SIMULATION";
  const maxNet = response.options.reduce<Decimal | null>((max, candidate) => {
    const net = Decimal.of(candidate.estimates.estimatedNetRealization);
    return max === null || net.gt(max) ? net : max;
  }, null);
  return {
    recommended: !simulation && option.rank === 1,
    whatIfTop: simulation && option.rank === 1,
    highestPrice: option.market.id === response.priceOnlyWinnerId,
    highestNet: maxNet !== null && Decimal.of(option.estimates.estimatedNetRealization).eq(maxNet),
  };
}

export type TradeOffKind = "more" | "notMore" | "equal";

export interface TradeOff {
  kind: TradeOffKind;
  /** P: the highest-price option. */
  priceWinner: SellingOptionResponse;
  /** R: the rank-1 option. */
  rankOne: SellingOptionResponse;
  /** P's gross − R's gross (exact). */
  grossDiff: Decimal;
  /** P's transport − R's transport (exact). */
  transportDiff: Decimal;
  /** R's net − P's net (exact); its sign selects the wording. */
  netDiff: Decimal;
  /** P's caution reasons, named when R's estimate is not higher. */
  cautions: ReasonCode[];
  /** The arithmetic line is shown only when P's estimated transport is higher, as its wording states. */
  showArithmetic: boolean;
}

const CAUTIONS: readonly ReasonCode[] = ["NEGATIVE_TREND", "STALE_DATA"];

/** The trade-off of the winner callout; null unless `rankingsDisagree` and both options are present. */
export function tradeOffOf(response: MarketComparisonResponse): TradeOff | null {
  if (!response.rankingsDisagree || response.options.length === 0) return null;
  const rankOne = response.options[0];
  const priceWinner = response.options.find((option) => option.market.id === response.priceOnlyWinnerId);
  if (!priceWinner || priceWinner.market.id === rankOne.market.id) return null;

  const grossDiff = Decimal.of(priceWinner.estimates.estimatedGrossValue)
    .sub(Decimal.of(rankOne.estimates.estimatedGrossValue));
  const transportDiff = Decimal.of(priceWinner.estimates.estimatedTransportCost)
    .sub(Decimal.of(rankOne.estimates.estimatedTransportCost));
  const netDiff = Decimal.of(rankOne.estimates.estimatedNetRealization)
    .sub(Decimal.of(priceWinner.estimates.estimatedNetRealization));
  const kind: TradeOffKind = netDiff.isPositive() ? "more" : netDiff.isZero() ? "equal" : "notMore";
  return {
    kind,
    priceWinner,
    rankOne,
    grossDiff,
    transportDiff,
    netDiff,
    cautions: kind === "more" ? [] : priceWinner.reasonCodes.filter((code) => CAUTIONS.includes(code)),
    showArithmetic: transportDiff.isPositive(),
  };
}

/** Whether a reason code is a caution (shown with the caution treatment) rather than a positive reason. */
export function isCaution(code: ReasonCode): boolean {
  return CAUTIONS.includes(code);
}
