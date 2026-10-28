"use client";

import { useId } from "react";

/**
 * One What-If assumption (role file 02 S09, AssumptionControl): a range slider with the reported value as a tick.
 * `value` null means "not moved": the slider shows the reported value but nothing is overridden (PDD-10); a
 * reported value outside the range has no tick and the slider starts at its minimum until moved.
 */
export function AssumptionSlider({ label, min, max, step, value, original, format, originalNote, onChange, onCommit }: {
  label: string;
  min: number;
  max: number;
  step: number;
  value: number | null;
  original: number | null;
  format: (value: number) => string;
  originalNote: string;
  onChange: (value: number) => void;
  onCommit?: (value: number) => void;
}) {
  const id = useId();
  const shown = value ?? original ?? min;
  const moved = value !== null;
  const percent = (point: number) => ((point - min) / (max - min)) * 100;
  const commit = (event: { currentTarget: HTMLInputElement }) => onCommit?.(Number(event.currentTarget.value));

  return (
    <div className={`flex flex-col gap-2 rounded-card p-3 ${moved ? "hyp-frame" : "border border-transparent"}`}>
      <div className="flex flex-wrap items-baseline justify-between gap-x-3 gap-y-1">
        <label htmlFor={id} className="text-label font-semibold text-ink">{label}</label>
        <span className="text-right">
          <span className={`figure text-heading font-semibold ${moved ? "text-hyp" : "text-ink"}`}>
            {value !== null || original !== null ? format(shown) : "—"}
          </span>{" "}
          <span className="text-caption text-ink-2">{originalNote}</span>
        </span>
      </div>
      <div className="relative py-2">
        {original !== null ? (
          <span
            className="pointer-events-none absolute top-0 h-7 w-0.5 -translate-x-1/2 bg-ink"
            style={{ left: `${percent(original)}%` }}
            aria-hidden="true"
          />
        ) : null}
        <input
          id={id}
          type="range"
          min={min}
          max={max}
          step={step}
          value={shown}
          aria-valuetext={format(shown)}
          onChange={(event) => onChange(Number(event.target.value))}
          onPointerUp={commit}
          onKeyUp={commit}
          className="h-3 w-full cursor-pointer accent-[var(--color-hyp)]"
        />
      </div>
      <div className="figure flex justify-between text-caption text-ink-2" aria-hidden="true">
        <span>{format(min)}</span>
        <span>{format(max)}</span>
      </div>
    </div>
  );
}
