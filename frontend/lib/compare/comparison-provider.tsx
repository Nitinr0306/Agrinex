"use client";

import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import type {
  CommodityResponse,
  CompareRequest,
  DistrictResponse,
  MarketComparisonResponse,
  SourceSyncStatus,
} from "@/lib/api/types";
import { emptyInput, type CompareInput } from "./input";
import type { WhatIfInputs } from "./whatif";

/** Names the screens show next to a comparison; the response itself carries no origin names. */
export interface ComparisonContext {
  district: DistrictResponse;
  commodity: CommodityResponse;
}

export interface ComparisonResult {
  request: CompareRequest;
  /** The latest comparison response, or a server-mode simulation that became the new base (ADR-0026 item 3). */
  response: MarketComparisonResponse;
  context: ComparisonContext;
}

/** "Use these numbers on the comparison": results keep the hypothetical treatment until reset (PDD-11). */
export interface AppliedWhatIf {
  inputs: WhatIfInputs;
  base: MarketComparisonResponse;
}

interface ComparisonState {
  input: CompareInput;
  setInput: (update: (current: CompareInput) => CompareInput) => void;
  result: ComparisonResult | null;
  setResult: (result: ComparisonResult | null) => void;
  applied: AppliedWhatIf | null;
  setApplied: (applied: AppliedWhatIf | null) => void;
  /** The latest source status any price response carried this session (home "about the prices"). */
  lastSourceStatus: SourceSyncStatus | null;
  noteSourceStatus: (status: SourceSyncStatus) => void;
}

const Context = createContext<ComparisonState | null>(null);

/**
 * In-memory comparison state for this browser session (FRONTEND_ARCHITECTURE §4.6): never written to storage, so
 * What-If overrides are session-only (spec §32) and no stored price is ever shown later as current.
 */
export function ComparisonProvider({ children }: { children: ReactNode }) {
  const [input, setInputState] = useState<CompareInput>(emptyInput);
  const [result, setResultState] = useState<ComparisonResult | null>(null);
  const [applied, setApplied] = useState<AppliedWhatIf | null>(null);
  const [lastSourceStatus, setLastSourceStatus] = useState<SourceSyncStatus | null>(null);

  const setInput = useCallback((update: (current: CompareInput) => CompareInput) => setInputState(update), []);
  const setResult = useCallback((next: ComparisonResult | null) => {
    setResultState(next);
    setApplied(null);
    if (next) setLastSourceStatus(next.response.sourceStatus);
  }, []);

  const value = useMemo<ComparisonState>(() => ({
    input,
    setInput,
    result,
    setResult,
    applied,
    setApplied,
    lastSourceStatus,
    noteSourceStatus: setLastSourceStatus,
  }), [input, setInput, result, setResult, applied, lastSourceStatus]);

  return <Context.Provider value={value}>{children}</Context.Provider>;
}

export function useComparison(): ComparisonState {
  const value = useContext(Context);
  if (!value) throw new Error("useComparison must be used inside ComparisonProvider");
  return value;
}
