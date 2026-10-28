-- V7: reference data for the two government datasets and the initial configuration versions.
-- Owner modules: price (source_datasets), ingestion (ingestion_source_settings),
--                transport (transport_assumptions), recommendation (recommendation_weights).
-- Source: docs/database/SEED_AND_REFERENCE_DATA.md sections 7 and 8.
--   * resource IDs and titles: spec section 1; evidence envelope titles (evidence section 19.1, 19.2);
--     dataset types ADR-0006 item 6;
--   * transport defaults: spec section 32 (8.00 per km, 300 minimum trip charge, 1,000 kg capacity);
--   * weights and freshness days: ADR-0024 item 1 (0.55, 0.15, 0.10, 0.10, 0.10; 1, 4, 10 days).
-- Every insert is keyed on a natural key or guarded, so re-running the statements changes nothing.

INSERT INTO source_datasets (resource_id, title, dataset_type) VALUES
    ('9ef84268-d588-465a-a308-a864a43d0070', 'Current Daily Price of Various Commodities from Various Markets (Mandi)', 'CURRENT_PRICE'),
    ('35985678-0d79-46b4-9ed6-6f13308a1d24', 'Variety-wise Daily Market Prices Data of Commodity', 'HISTORICAL_PRICE')
ON CONFLICT (resource_id) DO NOTHING;

-- One schedule setting per dataset, not paused (ADR-0020).
INSERT INTO ingestion_source_settings (source_dataset_id, is_paused, paused_reason)
SELECT id, false, NULL FROM source_datasets
ON CONFLICT (source_dataset_id) DO NOTHING;

INSERT INTO transport_assumptions (cost_per_km, minimum_trip_charge, vehicle_capacity_kg, is_active)
SELECT 8.00, 300.00, 1000.00, true
WHERE NOT EXISTS (SELECT 1 FROM transport_assumptions);

INSERT INTO recommendation_weights (w_net_realization, w_price, w_trend, w_freshness, w_transport_penalty,
                                    freshness_fresh_days, freshness_recent_days, freshness_stale_days, is_active)
SELECT 0.5500, 0.1500, 0.1000, 0.1000, 0.1000, 1, 4, 10, true
WHERE NOT EXISTS (SELECT 1 FROM recommendation_weights);
