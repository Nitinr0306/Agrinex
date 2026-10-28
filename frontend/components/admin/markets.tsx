"use client";

import Link from "next/link";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, Notice, Skeleton } from "@/components/ui/feedback";
import { Field, Select, TextInput } from "@/components/ui/form";
import { adminApi } from "@/lib/api/endpoints";
import type { AdminMarketResponse, MarketReviewStatus } from "@/lib/api/types";
import { useQuery } from "@/lib/hooks/use-query";
import { AdminError, AdminPage, AdminTable, KeyValues, Pager, td, th, useAdminFormat } from "./admin-ui";

const COORDINATE = /^-?\d{1,3}(\.\d{1,6})?$/;
const UUID = /^[0-9a-fA-F-]{36}$/;

/** A04 markets: review provisional markets registered by ingestion (ADR-0014). */
export function MarketsList() {
  const fmt = useAdminFormat();
  const [reviewStatus, setReviewStatus] = useState<MarketReviewStatus | "">("PROVISIONAL");
  const [active, setActive] = useState<"" | "true" | "false">("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const query = search.trim();
  const markets = useQuery(`admin-markets:${reviewStatus}:${active}:${query}:${page}`, (signal) =>
    adminApi.listAdminMarkets({
      page,
      size: 20,
      reviewStatus: reviewStatus || undefined,
      active: active === "" ? undefined : active === "true",
      search: query || undefined,
    }, signal));

  return (
    <AdminPage title="Markets">
      <div className="grid gap-3 tablet:grid-cols-3">
        <Field label="Review status">
          {({ inputId }) => (
            <Select id={inputId} value={reviewStatus} onChange={(event) => { setReviewStatus(event.target.value as MarketReviewStatus | ""); setPage(0); }}>
              <option value="">All</option>
              <option value="PROVISIONAL">PROVISIONAL</option>
              <option value="VERIFIED">VERIFIED</option>
              <option value="MERGED">MERGED</option>
            </Select>
          )}
        </Field>
        <Field label="Active">
          {({ inputId }) => (
            <Select id={inputId} value={active} onChange={(event) => { setActive(event.target.value as "" | "true" | "false"); setPage(0); }}>
              <option value="">All</option>
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </Select>
          )}
        </Field>
        <Field label="Search by name">
          {({ inputId }) => <TextInput id={inputId} type="search" value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} />}
        </Field>
      </div>
      {markets.status === "error" ? <AdminError error={markets.error} onRetry={markets.retry} /> : null}
      {!markets.data ? <Skeleton className="h-48" /> : (
        <AdminTable
          caption="Markets"
          head={(
            <tr>
              <th scope="col" className={th}>Market</th>
              <th scope="col" className={th}>District · state</th>
              <th scope="col" className={th}>Review</th>
              <th scope="col" className={th}>Active</th>
              <th scope="col" className={th}>Coordinates</th>
              <th scope="col" className={th}>Registered</th>
            </tr>
          )}
        >
          {markets.data.items.length === 0 ? <tr><td className={td} colSpan={6}>No markets match.</td></tr> : markets.data.items.map((market) => (
            <tr key={market.id}>
              <td className={td}><Link className="font-semibold text-net underline" href={`/admin/markets/${market.id}`}>{market.name}</Link></td>
              <td className={td}>{market.district.name} · {market.state.name}</td>
              <td className={td}>{market.reviewStatus}</td>
              <td className={td}>{market.active ? "Yes" : "No"}</td>
              <td className={`${td} figure`}>{market.coordinates ? `${market.coordinates.lat}, ${market.coordinates.lon}${market.coordinates.approximate ? " (approximate)" : ""}` : "None"}</td>
              <td className={td}>{fmt.dateTime(market.createdAt)}</td>
            </tr>
          ))}
        </AdminTable>
      )}
      {markets.data ? <Pager page={page} totalPages={markets.data.totalPages} onPage={setPage} /> : null}
    </AdminPage>
  );
}

/** A04 one market: aliases and the curation actions (verify, merge, deactivate, reactivate, coordinates). */
export function MarketCuration({ marketId }: { marketId: string }) {
  const fmt = useAdminFormat();
  const [version, setVersion] = useState(0);
  const market = useQuery(`admin-market:${marketId}:${version}`, (signal) => adminApi.getAdminMarket(marketId, signal));
  const reload = () => setVersion((value) => value + 1);
  const data = market.data;

  return (
    <AdminPage title={data?.name ?? "Market"}>
      {market.status === "error" ? <AdminError error={market.error} onRetry={market.retry} /> : null}
      {!data ? <Skeleton className="h-48" /> : (
        <>
          <Card className="px-4 py-4">
            <KeyValues
              rows={[
                ["Id", data.id],
                ["Slug", data.slug],
                ["District · state", `${data.district.name} · ${data.state.name}`],
                ["Review status", data.reviewStatus],
                ["Active", data.active ? "Yes" : "No"],
                ["Merged into", data.mergedIntoMarketId ?? "—"],
                ["Coordinates", data.coordinates ? `${data.coordinates.lat}, ${data.coordinates.lon}` : "None"],
                ["Coordinate source", data.coordinates ? `${data.coordinates.source}${data.coordinates.approximate ? " (approximate)" : ""}` : "—"],
                ["Latest report date", data.latestReportDate ? fmt.date(data.latestReportDate) : "—"],
                ["Registered", fmt.dateTime(data.createdAt)],
              ]}
            />
          </Card>
          <section className="flex flex-col gap-2">
            <h2 className="eyebrow">Source spellings</h2>
            <AdminTable
              caption="Source aliases"
              head={<tr><th scope="col" className={th}>State</th><th scope="col" className={th}>District</th><th scope="col" className={th}>Market (raw)</th><th scope="col" className={th}>Evidence</th><th scope="col" className={th}>Added</th></tr>}
            >
              {data.aliases.map((alias) => (
                <tr key={`${alias.sourceStateName}:${alias.sourceDistrictName}:${alias.sourceMarketName}`}>
                  <td className={td}>{alias.sourceStateName}</td>
                  <td className={td}>{alias.sourceDistrictName}</td>
                  <td className={`${td} figure whitespace-pre`}>“{alias.sourceMarketNameRaw}”</td>
                  <td className={td}>{alias.evidenceNote}</td>
                  <td className={td}>{fmt.dateTime(alias.createdAt)}</td>
                </tr>
              ))}
            </AdminTable>
          </section>
          <CurationActions market={data} onChanged={reload} />
        </>
      )}
    </AdminPage>
  );
}

function CurationActions({ market, onChanged }: { market: AdminMarketResponse; onChanged: () => void }) {
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [done, setDone] = useState<string | null>(null);
  const [target, setTarget] = useState("");
  const [note, setNote] = useState("");
  const [lat, setLat] = useState(market.coordinates ? String(market.coordinates.lat) : "");
  const [lon, setLon] = useState(market.coordinates ? String(market.coordinates.lon) : "");
  const [source, setSource] = useState("");

  const run = async (label: string, action: () => Promise<unknown>) => {
    setBusy(label);
    setError(null);
    setDone(null);
    try {
      await action();
      setDone(`${label}: done.`);
      onChanged();
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(null);
    }
  };

  const coordinatesValid = COORDINATE.test(lat.trim()) && COORDINATE.test(lon.trim()) && Math.abs(Number(lat)) <= 90
    && Math.abs(Number(lon)) <= 180 && source.trim().length > 0 && source.trim().length <= 300;

  return (
    <div className="grid gap-4 desktop:grid-cols-2">
      {error ? <div className="desktop:col-span-2"><AdminError error={error} /></div> : null}
      {done ? <div className="desktop:col-span-2"><Notice tone="success" live>{done}</Notice></div> : null}
      <Card className="flex flex-col gap-3 px-4 py-4">
        <h2 className="text-heading font-semibold">Status</h2>
        <div className="flex flex-wrap gap-2">
          {market.reviewStatus === "PROVISIONAL" ? (
            <Button busy={busy === "Verify"} onClick={() => void run("Verify", () => adminApi.verifyMarket(market.id))}>Verify market</Button>
          ) : null}
          {market.reviewStatus !== "MERGED" && market.active ? (
            <Button variant="destructive" busy={busy === "Deactivate"} onClick={() => void run("Deactivate", () => adminApi.deactivateMarket(market.id))}>Deactivate</Button>
          ) : null}
          {market.reviewStatus !== "MERGED" && !market.active ? (
            <Button variant="secondary" busy={busy === "Reactivate"} onClick={() => void run("Reactivate", () => adminApi.reactivateMarket(market.id))}>Reactivate</Button>
          ) : null}
        </div>
        <p className="text-caption text-ink-2">A verified, active market is a comparison and alert candidate. Deactivation keeps its observations.</p>
      </Card>

      {market.reviewStatus !== "MERGED" ? (
        <Card className="flex flex-col gap-3 px-4 py-4">
          <h2 className="text-heading font-semibold">Merge into another market</h2>
          <Field label="Target market id">
            {({ inputId }) => <TextInput id={inputId} value={target} onChange={(event) => setTarget(event.target.value.trim())} className="figure" />}
          </Field>
          <Field label="Evidence note" help="Required, up to 500 characters.">
            {({ inputId, describedBy }) => <TextInput id={inputId} aria-describedby={describedBy} value={note} maxLength={500} onChange={(event) => setNote(event.target.value)} />}
          </Field>
          <Button
            variant="destructive"
            className="w-fit"
            disabled={!UUID.test(target) || note.trim().length === 0}
            busy={busy === "Merge"}
            onClick={() => void run("Merge", () => adminApi.mergeMarket(market.id, { targetMarketId: target, evidenceNote: note.trim() }))}
          >
            Merge
          </Button>
        </Card>
      ) : null}

      <Card className="flex flex-col gap-3 px-4 py-4">
        <h2 className="text-heading font-semibold">Coordinates</h2>
        <div className="grid gap-3 tablet:grid-cols-2">
          <Field label="Latitude">
            {({ inputId }) => <TextInput id={inputId} inputMode="decimal" value={lat} onChange={(event) => setLat(event.target.value)} className="figure" />}
          </Field>
          <Field label="Longitude">
            {({ inputId }) => <TextInput id={inputId} inputMode="decimal" value={lon} onChange={(event) => setLon(event.target.value)} className="figure" />}
          </Field>
        </div>
        <Field label="Coordinate source" help="An identified source, up to 300 characters (ADR-0014).">
          {({ inputId, describedBy }) => <TextInput id={inputId} aria-describedby={describedBy} value={source} maxLength={300} onChange={(event) => setSource(event.target.value)} />}
        </Field>
        <Button
          className="w-fit"
          disabled={!coordinatesValid}
          busy={busy === "Coordinates"}
          onClick={() => void run("Coordinates", () => adminApi.setMarketCoordinates(market.id, { lat: Number(lat), lon: Number(lon), coordinateSource: source.trim() }))}
        >
          Save coordinates
        </Button>
      </Card>
    </div>
  );
}
