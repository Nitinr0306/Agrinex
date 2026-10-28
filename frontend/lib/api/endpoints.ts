/**
 * One function per contract operation (contracts/openapi.yaml, 53 operations). Paths, methods and the
 * `security` of each operation are copied from the contract; nothing here is invented.
 */
import { apiRequest } from "./client";
import type {
  AddCommodityRequest,
  AddDistrictAliasRequest,
  AdminAuditAction,
  AdminAuditEventPage,
  AdminCommodityPage,
  AdminCommodityResponse,
  AdminMarketDetailResponse,
  AdminMarketPage,
  AdminMarketResponse,
  AlertEventPage,
  AlertPage,
  AlertResponse,
  AlertStatus,
  AuthResponse,
  CommodityListResponse,
  CompareRequest,
  CreateAlertRequest,
  DatasetScheduleState,
  DistrictAliasPage,
  DistrictAliasResponse,
  DistrictListResponse,
  ForecastResponse,
  GradeListResponse,
  IngestionErrorPage,
  IngestionErrorType,
  HistoricalCoverageResponse,
  IngestionHealthResponse,
  IngestionRunPage,
  IngestionRunResponse,
  IngestionRunStatus,
  LocalityListResponse,
  LoginRequest,
  MarketComparisonResponse,
  MarketDetailResponse,
  MarketReviewStatus,
  MergeMarketRequest,
  RegisterRequest,
  SetMarketCoordinatesRequest,
  SimulateRequest,
  SourceDataset,
  StateListResponse,
  SystemStatusResponse,
  TransportAssumptionPage,
  TransportAssumptionResponse,
  TransportAssumptionsUpdateRequest,
  TrendResponse,
  TriggerIngestionRequest,
  TriggerResponse,
  UpdateCommodityNamesRequest,
  VarietyListResponse,
  VerifyPriceUnitRequest,
  WeightsPage,
  WeightsResponse,
  WeightsUpdateRequest,
} from "./types";

type Signal = AbortSignal | undefined;
const seg = encodeURIComponent;
const AUTH_RESPONSE = ["accessToken", "tokenType", "expiresIn", "user"] as const;
const PAGE = ["items", "page", "size", "totalItems", "totalPages"] as const;

export interface PageQuery {
  page?: number;
  size?: number;
}

/* Auth (spec §28; ADR-0016, ADR-0047, ADR-0048). refreshSession and logout live in lib/auth/session-store. */
export const authApi = {
  registerFarmer: (body: RegisterRequest) =>
    apiRequest<AuthResponse>("/auth/register", { method: "POST", body, auth: "none", expect: AUTH_RESPONSE }),
  login: (body: LoginRequest) =>
    apiRequest<AuthResponse>("/auth/login", { method: "POST", body, auth: "none", expect: AUTH_RESPONSE }),
  requestPasswordReset: (identifier: string) =>
    apiRequest<void>("/auth/password-reset/request", { method: "POST", body: { identifier }, auth: "none" }),
  confirmPasswordReset: (token: string, newPassword: string) =>
    apiRequest<void>("/auth/password-reset/confirm", { method: "POST", body: { token, newPassword }, auth: "none" }),
  confirmEmailVerification: (token: string) =>
    apiRequest<void>("/auth/email-verification/confirm", { method: "POST", body: { token }, auth: "none" }),
  resendEmailVerification: () =>
    apiRequest<void>("/auth/email-verification/resend", { method: "POST", auth: "required" }),
};

/* Geography and commodities (anonymous reference data). */
export const referenceApi = {
  listStates: (signal?: Signal) =>
    apiRequest<StateListResponse>("/geography/states", { auth: "none", signal, expect: ["items"] }),
  listDistricts: (stateId: string, signal?: Signal) =>
    apiRequest<DistrictListResponse>(`/geography/states/${seg(stateId)}/districts`, {
      auth: "none",
      signal,
      expect: ["items"],
    }),
  listLocalities: (districtId: string, signal?: Signal) =>
    apiRequest<LocalityListResponse>(`/geography/districts/${seg(districtId)}/localities`, {
      auth: "none",
      signal,
      expect: ["items"],
    }),
  listCommodities: (signal?: Signal) =>
    apiRequest<CommodityListResponse>("/commodities", { auth: "none", signal, expect: ["items"] }),
  listVarieties: (commodityId: string, signal?: Signal) =>
    apiRequest<VarietyListResponse>(`/commodities/${seg(commodityId)}/varieties`, {
      auth: "none",
      signal,
      expect: ["items"],
    }),
  listGrades: (commodityId: string, signal?: Signal) =>
    apiRequest<GradeListResponse>(`/commodities/${seg(commodityId)}/grades`, {
      auth: "none",
      signal,
      expect: ["items"],
    }),
};

export interface PriceFilter {
  commodityId?: string;
  varietyId?: string | null;
  gradeId?: string | null;
}

/* Market prices (anonymous). */
export const marketApi = {
  getMarketDetail: (marketId: string, filter: PriceFilter, signal?: Signal) =>
    apiRequest<MarketDetailResponse>(`/markets/${seg(marketId)}`, {
      auth: "none",
      signal,
      query: { commodityId: filter.commodityId, varietyId: filter.varietyId, gradeId: filter.gradeId },
      expect: ["market", "active", "approximateMarketLocation", "latestPrices", "noDataReason", "sourceStatus"],
    }),
  getMarketTrend: (marketId: string, filter: PriceFilter & { commodityId: string; days: 7 | 30 }, signal?: Signal) =>
    apiRequest<TrendResponse>(`/markets/${seg(marketId)}/trends`, {
      auth: "none",
      signal,
      query: {
        commodityId: filter.commodityId,
        days: filter.days,
        varietyId: filter.varietyId,
        gradeId: filter.gradeId,
      },
      expect: ["market", "commodity", "days", "window", "trendDirection", "changePercent7Day", "observations",
        "sourceStatus"],
    }),
  getMarketForecast: (marketId: string, commodityId: string, signal?: Signal) =>
    apiRequest<ForecastResponse>(`/markets/${seg(marketId)}/forecast`, {
      auth: "none",
      signal,
      query: { commodityId },
      expect: ["available", "kind", "forecasts"],
    }),
};

const COMPARISON = ["recommendationId", "scenario", "commodity", "quantity", "quantityUnit", "origin", "maxDistanceKm",
  "transportAssumptions", "decisionBasis", "priceOnlyWinnerId", "netRealizationWinnerId", "rankingsDisagree",
  "options", "excludedMarkets", "noDataReason", "sourceStatus"] as const;

/* Selling options (anonymous; an optional token links the search to the profile). */
export const sellingOptionsApi = {
  compare: (body: CompareRequest, signal?: Signal) =>
    apiRequest<MarketComparisonResponse>("/selling-options/compare", {
      method: "POST",
      body,
      auth: "optional",
      signal,
      expect: COMPARISON,
    }),
  simulate: (body: SimulateRequest, signal?: Signal) =>
    apiRequest<MarketComparisonResponse>("/selling-options/simulate", {
      method: "POST",
      body,
      auth: "optional",
      signal,
      expect: COMPARISON,
    }),
};

/* Alerts (FARMER or ADMIN; spec §34; ADR-0019). */
export const alertApi = {
  listAlerts: (query: PageQuery & { status?: AlertStatus }, signal?: Signal) =>
    apiRequest<AlertPage>("/alerts", { auth: "required", signal, query: { ...query }, expect: PAGE }),
  createAlert: (body: CreateAlertRequest) =>
    apiRequest<AlertResponse>("/alerts", {
      method: "POST",
      body,
      auth: "required",
      expect: ["id", "alertType", "status", "commodity", "thresholdValue"],
    }),
  updateAlertStatus: (alertId: string, status: AlertStatus) =>
    apiRequest<AlertResponse>(`/alerts/${seg(alertId)}`, {
      method: "PATCH",
      body: { status },
      auth: "required",
      expect: ["id", "status"],
    }),
  deleteAlert: (alertId: string) => apiRequest<void>(`/alerts/${seg(alertId)}`, { method: "DELETE", auth: "required" }),
  listAlertEvents: (query: PageQuery & { unreadOnly?: boolean }, signal?: Signal) =>
    apiRequest<AlertEventPage>("/alerts/events", {
      auth: "required",
      signal,
      query: { ...query },
      expect: [...PAGE, "sourceStatus"],
    }),
  markAlertEventRead: (eventId: string) =>
    apiRequest<void>(`/alerts/events/${seg(eventId)}/read`, { method: "POST", auth: "required" }),
};

/* Admin (ADMIN; ADR-0020). */
export const adminApi = {
  getSystemStatus: (signal?: Signal) =>
    apiRequest<SystemStatusResponse>("/admin/system/status", {
      auth: "required",
      signal,
      expect: ["generatedAt", "components", "governmentApiKey", "schedule", "coverage", "datasets"],
    }),
  getIngestionHealth: (signal?: Signal) =>
    apiRequest<IngestionHealthResponse>("/admin/ingestion/health", {
      auth: "required",
      signal,
      expect: ["generatedAt", "datasets", "marketFreshness"],
    }),
  getHistoricalCoverage: (signal?: Signal) =>
    apiRequest<HistoricalCoverageResponse>("/admin/ingestion/historical-coverage", {
      auth: "required",
      signal,
      expect: ["generatedAt", "coverageFrom", "coverageTo", "counts", "incompleteDates", "runningRun"],
    }),
  listIngestionRuns: (query: PageQuery & { datasetType?: SourceDataset; status?: IngestionRunStatus }, signal?: Signal) =>
    apiRequest<IngestionRunPage>("/admin/ingestion/runs", { auth: "required", signal, query: { ...query }, expect: PAGE }),
  getIngestionRun: (runId: string, signal?: Signal) =>
    apiRequest<IngestionRunResponse>(`/admin/ingestion/runs/${seg(runId)}`, {
      auth: "required",
      signal,
      expect: ["id", "datasetType", "status", "startedAt"],
    }),
  listIngestionRunErrors: (
    runId: string,
    query: PageQuery & { errorType?: IngestionErrorType; errorCode?: string },
    signal?: Signal,
  ) =>
    apiRequest<IngestionErrorPage>(`/admin/ingestion/runs/${seg(runId)}/errors`, {
      auth: "required",
      signal,
      query: { ...query },
      expect: PAGE,
    }),
  triggerIngestion: (body: TriggerIngestionRequest) =>
    apiRequest<TriggerResponse>("/admin/ingestion/trigger", {
      method: "POST",
      body,
      auth: "required",
      expect: ["runId", "datasetType", "status", "startedAt"],
    }),
  pauseScheduledIngestion: (datasetType: SourceDataset, reason: string) =>
    apiRequest<DatasetScheduleState>(`/admin/ingestion/datasets/${seg(datasetType)}/pause`, {
      method: "POST",
      body: { reason },
      auth: "required",
      expect: ["datasetType", "paused"],
    }),
  resumeScheduledIngestion: (datasetType: SourceDataset) =>
    apiRequest<DatasetScheduleState>(`/admin/ingestion/datasets/${seg(datasetType)}/resume`, {
      method: "POST",
      auth: "required",
      expect: ["datasetType", "paused"],
    }),
  getTransportAssumptions: (signal?: Signal) =>
    apiRequest<TransportAssumptionResponse>("/admin/config/transport-assumptions", {
      auth: "required",
      signal,
      expect: ["versionId", "ratePerKm", "minimumTripCharge", "vehicleCapacityKg"],
    }),
  updateTransportAssumptions: (body: TransportAssumptionsUpdateRequest) =>
    apiRequest<TransportAssumptionResponse>("/admin/config/transport-assumptions", {
      method: "PUT",
      body,
      auth: "required",
      expect: ["versionId"],
    }),
  listTransportAssumptionVersions: (query: PageQuery, signal?: Signal) =>
    apiRequest<TransportAssumptionPage>("/admin/config/transport-assumptions/versions", {
      auth: "required",
      signal,
      query: { ...query },
      expect: PAGE,
    }),
  getRecommendationWeights: (signal?: Signal) =>
    apiRequest<WeightsResponse>("/admin/config/recommendation-weights", {
      auth: "required",
      signal,
      expect: ["versionId", "wNetRealization", "freshnessFreshDays"],
    }),
  updateRecommendationWeights: (body: WeightsUpdateRequest) =>
    apiRequest<WeightsResponse>("/admin/config/recommendation-weights", {
      method: "PUT",
      body,
      auth: "required",
      expect: ["versionId"],
    }),
  listRecommendationWeightVersions: (query: PageQuery, signal?: Signal) =>
    apiRequest<WeightsPage>("/admin/config/recommendation-weights/versions", {
      auth: "required",
      signal,
      query: { ...query },
      expect: PAGE,
    }),
  listAdminMarkets: (
    query: PageQuery & { reviewStatus?: MarketReviewStatus; active?: boolean; districtId?: string; search?: string },
    signal?: Signal,
  ) => apiRequest<AdminMarketPage>("/admin/markets", { auth: "required", signal, query: { ...query }, expect: PAGE }),
  getAdminMarket: (marketId: string, signal?: Signal) =>
    apiRequest<AdminMarketDetailResponse>(`/admin/markets/${seg(marketId)}`, {
      auth: "required",
      signal,
      expect: ["id", "name", "reviewStatus", "aliases"],
    }),
  verifyMarket: (marketId: string) =>
    apiRequest<AdminMarketResponse>(`/admin/markets/${seg(marketId)}/verify`, {
      method: "POST",
      auth: "required",
      expect: ["id", "reviewStatus"],
    }),
  mergeMarket: (marketId: string, body: MergeMarketRequest) =>
    apiRequest<AdminMarketResponse>(`/admin/markets/${seg(marketId)}/merge`, {
      method: "POST",
      body,
      auth: "required",
      expect: ["id", "reviewStatus"],
    }),
  deactivateMarket: (marketId: string) =>
    apiRequest<AdminMarketResponse>(`/admin/markets/${seg(marketId)}/deactivate`, {
      method: "POST",
      auth: "required",
      expect: ["id", "active"],
    }),
  reactivateMarket: (marketId: string) =>
    apiRequest<AdminMarketResponse>(`/admin/markets/${seg(marketId)}/reactivate`, {
      method: "POST",
      auth: "required",
      expect: ["id", "active"],
    }),
  setMarketCoordinates: (marketId: string, body: SetMarketCoordinatesRequest) =>
    apiRequest<AdminMarketResponse>(`/admin/markets/${seg(marketId)}/coordinates`, {
      method: "PUT",
      body,
      auth: "required",
      expect: ["id", "coordinates"],
    }),
  listAdminCommodities: (query: PageQuery & { unitVerified?: boolean; search?: string }, signal?: Signal) =>
    apiRequest<AdminCommodityPage>("/admin/commodities", { auth: "required", signal, query: { ...query }, expect: PAGE }),
  addCommodity: (body: AddCommodityRequest) =>
    apiRequest<AdminCommodityResponse>("/admin/commodities", {
      method: "POST",
      body,
      auth: "required",
      expect: ["id", "name"],
    }),
  verifyCommodityPriceUnit: (commodityId: string, body: VerifyPriceUnitRequest) =>
    apiRequest<AdminCommodityResponse>(`/admin/commodities/${seg(commodityId)}/price-unit`, {
      method: "PUT",
      body,
      auth: "required",
      expect: ["id", "priceUnit"],
    }),
  updateCommodityNames: (commodityId: string, body: UpdateCommodityNamesRequest) =>
    apiRequest<AdminCommodityResponse>(`/admin/commodities/${seg(commodityId)}/names`, {
      method: "PUT",
      body,
      auth: "required",
      expect: ["id", "nameHi", "nameMr"],
    }),
  listDistrictAliases: (query: PageQuery & { districtId?: string }, signal?: Signal) =>
    apiRequest<DistrictAliasPage>("/admin/district-aliases", {
      auth: "required",
      signal,
      query: { ...query },
      expect: PAGE,
    }),
  addDistrictAlias: (body: AddDistrictAliasRequest) =>
    apiRequest<DistrictAliasResponse>("/admin/district-aliases", {
      method: "POST",
      body,
      auth: "required",
      expect: ["id", "district"],
    }),
  listAdminAuditEvents: (query: PageQuery & { action?: AdminAuditAction }, signal?: Signal) =>
    apiRequest<AdminAuditEventPage>("/admin/audit-events", {
      auth: "required",
      signal,
      query: { ...query },
      expect: PAGE,
    }),
};
