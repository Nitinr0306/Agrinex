/** Container health check: `/api/health` answers `{"status":"ok"}` (spec §45; DEPLOYMENT_ARCHITECTURE.md). */
export function GET() {
  return Response.json({ status: "ok" }, { headers: { "Cache-Control": "no-store" } });
}
