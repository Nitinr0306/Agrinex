/**
 * Names for the contract's schemas. Every type here is generated from `contracts/openapi.yaml` (contract 1.1.0)
 * into `schema.d.ts` by `npm run generate:api-types`; nothing is hand-modelled, so the frontend cannot drift
 * from the contract without a type error.
 */
import type { components } from "./schema";

type Schemas = components["schemas"];

export type Uuid = string;
export type CalendarDate = string;
export type Timestamp = string;

export type ApiErrorResponse = Schemas["ApiErrorResponse"];
export type ErrorCode = Schemas["ErrorCode"];
export type FieldError = Schemas["FieldError"];
export type FieldErrorCode = Schemas["FieldErrorCode"];

export type Role = Schemas["Role"];
export type RegisterRequest = Schemas["RegisterRequest"];
export type LoginRequest = Schemas["LoginRequest"];
export type AuthResponse = Schemas["AuthResponse"];
export type AuthenticatedUser = Schemas["AuthenticatedUser"];
export type PasswordResetRequest = Schemas["PasswordResetRequest"];
export type PasswordResetConfirmRequest = Schemas["PasswordResetConfirmRequest"];
export type EmailVerificationConfirmRequest = Schemas["EmailVerificationConfirmRequest"];

export type StateRef = Schemas["StateRef"];
export type DistrictRef = Schemas["DistrictRef"];
export type MarketRef = Schemas["MarketRef"];
export type MarketSummary = Schemas["MarketSummary"];
export type CommodityRef = Schemas["CommodityRef"];
export type VarietyRef = Schemas["VarietyRef"];
export type GradeRef = Schemas["GradeRef"];
export type GeoPoint = Schemas["GeoPoint"];
export type QuantityUnit = Schemas["QuantityUnit"];

export type StateResponse = Schemas["StateResponse"];
export type DistrictResponse = Schemas["DistrictResponse"];
export type LocalityResponse = Schemas["LocalityResponse"];
export type StateListResponse = Schemas["StateListResponse"];
export type DistrictListResponse = Schemas["DistrictListResponse"];
export type LocalityListResponse = Schemas["LocalityListResponse"];
export type CommodityResponse = Schemas["CommodityResponse"];
export type VarietyResponse = Schemas["VarietyResponse"];
export type GradeResponse = Schemas["GradeResponse"];
export type CommodityListResponse = Schemas["CommodityListResponse"];
export type VarietyListResponse = Schemas["VarietyListResponse"];
export type GradeListResponse = Schemas["GradeListResponse"];

export type PriceQuote = Schemas["PriceQuote"];
export type FreshnessStatus = Schemas["FreshnessStatus"];
export type TrendDirection = Schemas["TrendDirection"];
export type SourceDataset = Schemas["SourceDataset"];
export type SourceSyncStatus = Schemas["SourceSyncStatus"];
export type MarketLatestPrice = Schemas["MarketLatestPrice"];
export type MarketDetailResponse = Schemas["MarketDetailResponse"];
export type TrendResponse = Schemas["TrendResponse"];
export type TrendWindow = Schemas["TrendWindow"];
export type ForecastResponse = Schemas["ForecastResponse"];
export type ForecastSeries = Schemas["ForecastSeries"];

export type CompareRequest = Schemas["CompareRequest"];
export type SimulateRequest = Schemas["SimulateRequest"];
export type MarketComparisonResponse = Schemas["MarketComparisonResponse"];
export type SellingOptionResponse = Schemas["SellingOptionResponse"];
export type ExcludedMarket = Schemas["ExcludedMarket"];
export type ReasonCode = Schemas["ReasonCode"];
export type TransportAssumptionsApplied = Schemas["TransportAssumptionsApplied"];
export type SellingEstimates = Schemas["SellingEstimates"];

export type AlertType = Schemas["AlertType"];
export type AlertStatus = Schemas["AlertStatus"];
export type CreateAlertRequest = Schemas["CreateAlertRequest"];
export type AlertResponse = Schemas["AlertResponse"];
export type AlertPage = Schemas["AlertPage"];
export type AlertEventResponse = Schemas["AlertEventResponse"];
export type AlertEventPage = Schemas["AlertEventPage"];

export type SystemStatusResponse = Schemas["SystemStatusResponse"];
export type IngestionHealthResponse = Schemas["IngestionHealthResponse"];
export type DatasetHealth = Schemas["DatasetHealth"];
export type HistoricalCoverageResponse = Schemas["HistoricalCoverageResponse"];
export type ReportDateCoverage = Schemas["ReportDateCoverage"];
export type ReportDateStatus = Schemas["ReportDateStatus"];
export type IngestionRunResponse = Schemas["IngestionRunResponse"];
export type IngestionRunPage = Schemas["IngestionRunPage"];
export type IngestionRunStatus = Schemas["IngestionRunStatus"];
export type IngestionErrorResponse = Schemas["IngestionErrorResponse"];
export type IngestionErrorPage = Schemas["IngestionErrorPage"];
export type IngestionErrorType = Schemas["IngestionErrorType"];
export type TriggerIngestionRequest = Schemas["TriggerIngestionRequest"];
export type TriggerResponse = Schemas["TriggerResponse"];
export type DatasetScheduleState = Schemas["DatasetScheduleState"];
export type TransportAssumptionsUpdateRequest = Schemas["TransportAssumptionsUpdateRequest"];
export type TransportAssumptionResponse = Schemas["TransportAssumptionResponse"];
export type TransportAssumptionPage = Schemas["TransportAssumptionPage"];
export type WeightsUpdateRequest = Schemas["WeightsUpdateRequest"];
export type WeightsResponse = Schemas["WeightsResponse"];
export type WeightsPage = Schemas["WeightsPage"];
export type MarketReviewStatus = Schemas["MarketReviewStatus"];
export type AdminMarketResponse = Schemas["AdminMarketResponse"];
export type AdminMarketDetailResponse = Schemas["AdminMarketDetailResponse"];
export type AdminMarketPage = Schemas["AdminMarketPage"];
export type MergeMarketRequest = Schemas["MergeMarketRequest"];
export type SetMarketCoordinatesRequest = Schemas["SetMarketCoordinatesRequest"];
export type AdminCommodityResponse = Schemas["AdminCommodityResponse"];
export type AdminCommodityPage = Schemas["AdminCommodityPage"];
export type AddCommodityRequest = Schemas["AddCommodityRequest"];
export type VerifyPriceUnitRequest = Schemas["VerifyPriceUnitRequest"];
export type UpdateCommodityNamesRequest = Schemas["UpdateCommodityNamesRequest"];
export type DistrictAliasResponse = Schemas["DistrictAliasResponse"];
export type DistrictAliasPage = Schemas["DistrictAliasPage"];
export type AddDistrictAliasRequest = Schemas["AddDistrictAliasRequest"];
export type AdminAuditAction = Schemas["AdminAuditAction"];
export type AdminAuditEventResponse = Schemas["AdminAuditEventResponse"];
export type AdminAuditEventPage = Schemas["AdminAuditEventPage"];

/** The price unit every comparison and alert uses (ADR-0008; CommodityInfo.RUPEES_PER_QUINTAL). */
export const RUPEES_PER_QUINTAL = "Rs./Quintal";
