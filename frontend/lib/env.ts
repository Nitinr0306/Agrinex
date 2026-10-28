/**
 * Public build-time configuration. `NEXT_PUBLIC_*` values are compiled into browser code, so they never hold a
 * secret (spec §47 Q21). The frontend has no secret at all.
 */
export const MOCK_MODE = process.env.NEXT_PUBLIC_USE_MOCK_API === "true";
