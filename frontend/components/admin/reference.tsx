"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, Notice, Skeleton } from "@/components/ui/feedback";
import { Field, Select, TextInput } from "@/components/ui/form";
import { adminApi } from "@/lib/api/endpoints";
import type { AdminAuditAction, AdminCommodityResponse } from "@/lib/api/types";
import { useQuery } from "@/lib/hooks/use-query";
import { referenceData } from "@/lib/reference/reference-data";
import { AdminError, AdminPage, AdminTable, Pager, td, th, useAdminFormat } from "./admin-ui";

const nonEmpty = (value: string, max: number) => value.trim().length > 0 && value.trim().length <= max;

/** A04 commodities: the known list with unit evidence and localized names (ADR-0006, ADR-0008, ADR-0015). */
export function Commodities() {
  const [unitVerified, setUnitVerified] = useState<"" | "true" | "false">("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [version, setVersion] = useState(0);
  const [editing, setEditing] = useState<AdminCommodityResponse | null>(null);
  const query = search.trim();
  const list = useQuery(`admin-commodities:${unitVerified}:${query}:${page}:${version}`, (signal) =>
    adminApi.listAdminCommodities({ page, size: 20, unitVerified: unitVerified === "" ? undefined : unitVerified === "true", search: query || undefined }, signal));
  const reload = () => setVersion((value) => value + 1);

  return (
    <AdminPage title="Commodities">
      <div className="grid gap-3 tablet:grid-cols-2 desktop:max-w-2xl">
        <Field label="Unit verified">
          {({ inputId }) => (
            <Select id={inputId} value={unitVerified} onChange={(event) => { setUnitVerified(event.target.value as "" | "true" | "false"); setPage(0); }}>
              <option value="">All</option>
              <option value="true">Verified unit</option>
              <option value="false">No verified unit</option>
            </Select>
          )}
        </Field>
        <Field label="Search by name">
          {({ inputId }) => <TextInput id={inputId} type="search" value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} />}
        </Field>
      </div>
      {list.status === "error" ? <AdminError error={list.error} onRetry={list.retry} /> : null}
      {!list.data ? <Skeleton className="h-48" /> : (
        <AdminTable
          caption="Commodities"
          head={<tr><th scope="col" className={th}>Name</th><th scope="col" className={th}>Marathi · Hindi</th><th scope="col" className={th}>Category</th><th scope="col" className={th}>Unit</th><th scope="col" className={th}>Unit source</th><th scope="col" className={th}>Edit</th></tr>}
        >
          {list.data.items.length === 0 ? <tr><td className={td} colSpan={6}>No commodities match.</td></tr> : list.data.items.map((item) => (
            <tr key={item.id}>
              <td className={`${td} font-semibold`}>{item.name}</td>
              <td className={td}><span lang="mr">{item.nameMr ?? "—"}</span> · <span lang="hi">{item.nameHi ?? "—"}</span></td>
              <td className={td}>{item.category ?? "—"}</td>
              <td className={td}>{item.priceUnit ?? <strong className="text-stale">Not verified</strong>}</td>
              <td className={`${td} text-caption`}>{item.priceUnitSource ?? "—"}</td>
              <td className={td}><Button variant="tertiary" onClick={() => setEditing(item)}>Edit</Button></td>
            </tr>
          ))}
        </AdminTable>
      )}
      {list.data ? <Pager page={page} totalPages={list.data.totalPages} onPage={setPage} /> : null}
      {editing ? <CommodityEditor key={editing.id} commodity={editing} onSaved={() => { setEditing(null); reload(); }} /> : null}
      <AddCommodity onAdded={reload} />
    </AdminPage>
  );
}

function CommodityEditor({ commodity, onSaved }: { commodity: AdminCommodityResponse; onSaved: () => void }) {
  const [unit, setUnit] = useState(commodity.priceUnit ?? "Rs./Quintal");
  const [unitSource, setUnitSource] = useState(commodity.priceUnitSource ?? "");
  const [nameMr, setNameMr] = useState(commodity.nameMr ?? "");
  const [nameHi, setNameHi] = useState(commodity.nameHi ?? "");
  const [provenance, setProvenance] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);

  const save = async (action: () => Promise<unknown>) => {
    setBusy(true);
    setError(null);
    try {
      await action();
      onSaved();
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card className="flex flex-col gap-4 px-4 py-4">
      <h2 className="text-heading font-semibold">Edit {commodity.name}</h2>
      {error ? <AdminError error={error} /> : null}
      <div className="grid gap-3 tablet:grid-cols-2">
        <Field label="Verified price unit">
          {({ inputId }) => <TextInput id={inputId} value={unit} maxLength={50} onChange={(event) => setUnit(event.target.value)} />}
        </Field>
        <Field label="Unit evidence source" help="Required, up to 300 characters (ADR-0008 item 1).">
          {({ inputId, describedBy }) => <TextInput id={inputId} aria-describedby={describedBy} value={unitSource} maxLength={300} onChange={(event) => setUnitSource(event.target.value)} />}
        </Field>
      </div>
      <Button className="w-fit" busy={busy} disabled={!nonEmpty(unit, 50) || !nonEmpty(unitSource, 300)}
        onClick={() => void save(() => adminApi.verifyCommodityPriceUnit(commodity.id, { priceUnit: unit.trim(), priceUnitSource: unitSource.trim() }))}>
        Save verified unit
      </Button>
      <div className="grid gap-3 tablet:grid-cols-3">
        <Field label="Marathi name" optionalLabel="(empty = official name)">
          {({ inputId }) => <TextInput id={inputId} lang="mr" value={nameMr} maxLength={200} onChange={(event) => setNameMr(event.target.value)} />}
        </Field>
        <Field label="Hindi name" optionalLabel="(empty = official name)">
          {({ inputId }) => <TextInput id={inputId} lang="hi" value={nameHi} maxLength={200} onChange={(event) => setNameHi(event.target.value)} />}
        </Field>
        <Field label="Provenance note" help="Required: where the translation comes from (ADR-0015).">
          {({ inputId, describedBy }) => <TextInput id={inputId} aria-describedby={describedBy} value={provenance} maxLength={500} onChange={(event) => setProvenance(event.target.value)} />}
        </Field>
      </div>
      <Button className="w-fit" busy={busy} disabled={!nonEmpty(provenance, 500)}
        onClick={() => void save(() => adminApi.updateCommodityNames(commodity.id, { nameMr: nameMr.trim() || null, nameHi: nameHi.trim() || null, provenanceNote: provenance.trim() }))}>
        Save names
      </Button>
    </Card>
  );
}

function AddCommodity({ onAdded }: { onAdded: () => void }) {
  const [form, setForm] = useState({ name: "", category: "", priceUnit: "Rs./Quintal", priceUnitSource: "", sourceReference: "" });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [done, setDone] = useState<string | null>(null);
  const valid = nonEmpty(form.name, 200) && form.category.trim().length <= 100 && nonEmpty(form.priceUnit, 50)
    && nonEmpty(form.priceUnitSource, 300) && nonEmpty(form.sourceReference, 500);
  const set = (key: keyof typeof form) => (event: React.ChangeEvent<HTMLInputElement>) => setForm((current) => ({ ...current, [key]: event.target.value }));

  const add = async () => {
    setBusy(true);
    setError(null);
    try {
      const created = await adminApi.addCommodity({
        name: form.name.trim(),
        category: form.category.trim() || null,
        priceUnit: form.priceUnit.trim(),
        priceUnitSource: form.priceUnitSource.trim(),
        sourceReference: form.sourceReference.trim(),
      });
      setDone(`Added ${created.name}.`);
      setForm({ name: "", category: "", priceUnit: "Rs./Quintal", priceUnitSource: "", sourceReference: "" });
      onAdded();
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card className="flex flex-col gap-3 px-4 py-4">
      <h2 className="text-heading font-semibold">Add a commodity from the official master</h2>
      <div className="grid gap-3 tablet:grid-cols-2">
        <Field label="Name (as published)">{({ inputId }) => <TextInput id={inputId} value={form.name} maxLength={200} onChange={set("name")} />}</Field>
        <Field label="Category" optionalLabel="(optional)">{({ inputId }) => <TextInput id={inputId} value={form.category} maxLength={100} onChange={set("category")} />}</Field>
        <Field label="Verified price unit">{({ inputId }) => <TextInput id={inputId} value={form.priceUnit} maxLength={50} onChange={set("priceUnit")} />}</Field>
        <Field label="Unit evidence source">{({ inputId }) => <TextInput id={inputId} value={form.priceUnitSource} maxLength={300} onChange={set("priceUnitSource")} />}</Field>
        <Field label="Official source reference">{({ inputId }) => <TextInput id={inputId} value={form.sourceReference} maxLength={500} onChange={set("sourceReference")} />}</Field>
      </div>
      {error ? <AdminError error={error} /> : null}
      {done ? <Notice tone="success" live>{done}</Notice> : null}
      <Button className="w-fit" busy={busy} disabled={!valid} onClick={() => void add()}>Add commodity</Button>
    </Card>
  );
}

/** A04 district aliases: a former or variant district spelling mapped to a district, with evidence (ADR-0014). */
export function DistrictAliases() {
  const fmt = useAdminFormat();
  const [page, setPage] = useState(0);
  const [version, setVersion] = useState(0);
  const aliases = useQuery(`aliases:${page}:${version}`, (signal) => adminApi.listDistrictAliases({ page, size: 20 }, signal));
  const states = useQuery("states", () => referenceData.states());
  const [stateId, setStateId] = useState("");
  const districts = useQuery(stateId ? `districts:${stateId}` : null, () => referenceData.districts(stateId));
  const [form, setForm] = useState({ sourceStateName: "", sourceDistrictName: "", districtId: "", evidenceNote: "" });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const valid = nonEmpty(form.sourceStateName, 100) && nonEmpty(form.sourceDistrictName, 150) && form.districtId !== "" && nonEmpty(form.evidenceNote, 500);

  const add = async () => {
    setBusy(true);
    setError(null);
    try {
      await adminApi.addDistrictAlias({
        sourceStateName: form.sourceStateName.trim(),
        sourceDistrictName: form.sourceDistrictName.trim(),
        districtId: form.districtId,
        evidenceNote: form.evidenceNote.trim(),
      });
      setForm({ sourceStateName: "", sourceDistrictName: "", districtId: "", evidenceNote: "" });
      setVersion((value) => value + 1);
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <AdminPage title="District aliases">
      {aliases.status === "error" ? <AdminError error={aliases.error} onRetry={aliases.retry} /> : null}
      {!aliases.data ? <Skeleton className="h-32" /> : (
        <AdminTable
          caption="District aliases"
          head={<tr><th scope="col" className={th}>Source state</th><th scope="col" className={th}>Source district</th><th scope="col" className={th}>District</th><th scope="col" className={th}>Evidence</th><th scope="col" className={th}>Added</th></tr>}
        >
          {aliases.data.items.length === 0 ? <tr><td className={td} colSpan={5}>No aliases yet.</td></tr> : aliases.data.items.map((alias) => (
            <tr key={alias.id}>
              <td className={td}>{alias.sourceStateName}</td>
              <td className={td}>{alias.sourceDistrictName}</td>
              <td className={`${td} font-semibold`}>{alias.district.name}</td>
              <td className={td}>{alias.evidenceNote}</td>
              <td className={td}>{fmt.dateTime(alias.createdAt)}</td>
            </tr>
          ))}
        </AdminTable>
      )}
      {aliases.data ? <Pager page={page} totalPages={aliases.data.totalPages} onPage={setPage} /> : null}

      <Card className="flex flex-col gap-3 px-4 py-4">
        <h2 className="text-heading font-semibold">Add an alias</h2>
        <div className="grid gap-3 tablet:grid-cols-2">
          <Field label="Source state name (as the feed spells it)">
            {({ inputId }) => <TextInput id={inputId} value={form.sourceStateName} maxLength={100} onChange={(event) => setForm((current) => ({ ...current, sourceStateName: event.target.value }))} />}
          </Field>
          <Field label="Source district name (as the feed spells it)">
            {({ inputId }) => <TextInput id={inputId} value={form.sourceDistrictName} maxLength={150} onChange={(event) => setForm((current) => ({ ...current, sourceDistrictName: event.target.value }))} />}
          </Field>
          <Field label="State">
            {({ inputId }) => (
              <Select id={inputId} value={stateId} onChange={(event) => { setStateId(event.target.value); setForm((current) => ({ ...current, districtId: "" })); }}>
                <option value="">Choose</option>
                {(states.data ?? []).map((state) => <option key={state.id} value={state.id}>{state.name}</option>)}
              </Select>
            )}
          </Field>
          <Field label="District">
            {({ inputId }) => (
              <Select id={inputId} value={form.districtId} disabled={districts.status !== "success"} onChange={(event) => setForm((current) => ({ ...current, districtId: event.target.value }))}>
                <option value="">Choose</option>
                {(districts.status === "success" ? districts.data : []).map((district) => <option key={district.id} value={district.id}>{district.name}</option>)}
              </Select>
            )}
          </Field>
          <Field label="Evidence note" help="Required, up to 500 characters.">
            {({ inputId, describedBy }) => <TextInput id={inputId} aria-describedby={describedBy} value={form.evidenceNote} maxLength={500} onChange={(event) => setForm((current) => ({ ...current, evidenceNote: event.target.value }))} />}
          </Field>
        </div>
        {states.status === "success" && states.data.length === 0 ? <Notice>No states are loaded yet, so no district can be chosen.</Notice> : null}
        {error ? <AdminError error={error} /> : null}
        <Button className="w-fit" busy={busy} disabled={!valid} onClick={() => void add()}>Add alias</Button>
      </Card>
    </AdminPage>
  );
}

const ACTIONS: AdminAuditAction[] = [
  "INGESTION_TRIGGERED", "INGESTION_PAUSED", "INGESTION_RESUMED", "TRANSPORT_ASSUMPTIONS_UPDATED",
  "RECOMMENDATION_WEIGHTS_UPDATED", "MARKET_VERIFIED", "MARKET_MERGED", "MARKET_DEACTIVATED", "MARKET_REACTIVATED",
  "MARKET_COORDINATES_SET", "COMMODITY_ADDED", "COMMODITY_UNIT_VERIFIED", "COMMODITY_NAMES_UPDATED",
  "DISTRICT_ALIAS_ADDED", "ADMIN_BOOTSTRAPPED", "ACCOUNT_ERASED", "ACCOUNT_DEACTIVATED", "ACCOUNT_REACTIVATED",
];

/** A05 admin audit log, newest first, with the masked before/after details the API returns (ADR-0020 item 6). */
export function AuditLog() {
  const fmt = useAdminFormat();
  const [action, setAction] = useState<AdminAuditAction | "">("");
  const [page, setPage] = useState(0);
  const events = useQuery(`audit:${action}:${page}`, (signal) => adminApi.listAdminAuditEvents({ page, size: 20, action: action || undefined }, signal));

  return (
    <AdminPage title="Audit log">
      <Field label="Action">
        {({ inputId }) => (
          <Select id={inputId} value={action} onChange={(event) => { setAction(event.target.value as AdminAuditAction | ""); setPage(0); }} className="max-w-md">
            <option value="">All</option>
            {ACTIONS.map((value) => <option key={value} value={value}>{value}</option>)}
          </Select>
        )}
      </Field>
      {events.status === "error" ? <AdminError error={events.error} onRetry={events.retry} /> : null}
      {!events.data ? <Skeleton className="h-48" /> : (
        <AdminTable
          caption="Audit events"
          head={<tr><th scope="col" className={th}>When</th><th scope="col" className={th}>Action</th><th scope="col" className={th}>Actor</th><th scope="col" className={th}>Target</th><th scope="col" className={th}>Details</th></tr>}
        >
          {events.data.items.length === 0 ? <tr><td className={td} colSpan={5}>No audit events.</td></tr> : events.data.items.map((event) => (
            <tr key={event.id}>
              <td className={td}>{fmt.dateTime(event.createdAt)}</td>
              <td className={`${td} figure`}>{event.action}</td>
              <td className={`${td} text-caption`}><pre className="whitespace-pre-wrap break-all">{JSON.stringify(event.actor)}</pre></td>
              <td className={td}>{event.targetType}{event.targetId ? <span className="figure block text-caption">{event.targetId}</span> : null}</td>
              <td className={`${td} text-caption`}>
                <details>
                  <summary className="font-semibold text-net">Show</summary>
                  <pre className="mt-1 max-h-48 overflow-auto whitespace-pre-wrap break-all bg-sunken p-2">{JSON.stringify(event.details, null, 2)}</pre>
                  {event.correlationId ? <p className="figure mt-1">Reference: {event.correlationId}</p> : null}
                </details>
              </td>
            </tr>
          ))}
        </AdminTable>
      )}
      {events.data ? <Pager page={page} totalPages={events.data.totalPages} onPage={setPage} /> : null}
    </AdminPage>
  );
}
