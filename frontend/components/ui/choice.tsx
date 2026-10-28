"use client";

import { useRef, type KeyboardEvent, type ReactNode } from "react";
import { CheckIcon } from "@/components/icons";

export interface ChoiceOption<T extends string | number> {
  value: T;
  label: ReactNode;
  /** Set when the label is in another language than the page (fallback names, ADR-0015 / PDD-05). */
  lang?: string;
}

interface ChipGroupProps<T extends string | number> {
  label: string;
  options: ChoiceOption<T>[];
  value: T | null;
  onChange: (value: T) => void;
  /** Visually hidden label when a visible heading already names the group. */
  hideLabel?: boolean;
  variant?: "chips" | "rows";
}

/**
 * A single choice shown as chips or full-width rows ("tap, don't type", role file 02 §3). It is a radio group:
 * arrow keys move between options and the selected one is marked by a check and a filled style, not colour alone.
 */
export function ChipGroup<T extends string | number>({ label, options, value, onChange, hideLabel, variant = "chips" }: ChipGroupProps<T>) {
  const refs = useRef<(HTMLButtonElement | null)[]>([]);
  const selectedIndex = options.findIndex((option) => option.value === value);

  const onKeyDown = (event: KeyboardEvent, index: number) => {
    const delta = event.key === "ArrowRight" || event.key === "ArrowDown" ? 1
      : event.key === "ArrowLeft" || event.key === "ArrowUp" ? -1 : 0;
    if (delta === 0) return;
    event.preventDefault();
    const next = (index + delta + options.length) % options.length;
    onChange(options[next].value);
    refs.current[next]?.focus();
  };

  return (
    <div role="radiogroup" aria-label={label} className={variant === "rows" ? "flex flex-col gap-2" : "flex flex-wrap gap-2"}>
      {hideLabel ? null : <span className="sr-only">{label}</span>}
      {options.map((option, index) => {
        const selected = option.value === value;
        const focusable = selected || (selectedIndex === -1 && index === 0);
        return (
          <button
            key={String(option.value)}
            ref={(element) => {
              refs.current[index] = element;
            }}
            type="button"
            role="radio"
            aria-checked={selected}
            tabIndex={focusable ? 0 : -1}
            lang={option.lang}
            onClick={() => onChange(option.value)}
            onKeyDown={(event) => onKeyDown(event, index)}
            className={[
              "inline-flex min-h-12 items-center gap-2 rounded-control border px-4 text-body transition-colors",
              variant === "rows" ? "w-full justify-between text-left" : "",
              selected
                ? "border-action bg-action font-semibold text-inverse"
                : "border-rule-strong bg-raised text-ink hover:bg-sunken",
            ].join(" ")}
          >
            <span className="inline-flex items-center gap-2">
              {selected && variant === "chips" ? <CheckIcon size={18} /> : null}
              {option.label}
            </span>
            {variant === "rows" ? (
              <span
                className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 ${selected ? "border-inverse" : "border-rule-strong"}`}
                aria-hidden="true"
              >
                {selected ? <CheckIcon size={14} /> : null}
              </span>
            ) : null}
          </button>
        );
      })}
    </div>
  );
}

interface SegmentedProps<T extends string | number> {
  label: string;
  options: ChoiceOption<T>[];
  value: T;
  onChange: (value: T) => void;
}

/** Two or three mutually exclusive settings in one bar (kg | quintal, 7 | 30 days). */
export function Segmented<T extends string | number>({ label, options, value, onChange }: SegmentedProps<T>) {
  return (
    <div role="radiogroup" aria-label={label} className="inline-flex rounded-control border border-rule-strong bg-sunken p-1">
      {options.map((option) => {
        const selected = option.value === value;
        return (
          <button
            key={String(option.value)}
            type="button"
            role="radio"
            aria-checked={selected}
            onClick={() => onChange(option.value)}
            onKeyDown={(event) => {
              if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown"].includes(event.key)) {
                event.preventDefault();
                const index = options.findIndex((candidate) => candidate.value === value);
                const step = event.key === "ArrowRight" || event.key === "ArrowDown" ? 1 : -1;
                onChange(options[(index + step + options.length) % options.length].value);
              }
            }}
            tabIndex={selected ? 0 : -1}
            className={`min-h-11 min-w-16 rounded-control px-4 text-label font-semibold ${selected ? "bg-action text-inverse" : "text-ink hover:bg-raised"}`}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}

interface SwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label: string;
  onText: string;
  offText: string;
  disabled?: boolean;
}

/** An on/off switch whose state is also written as a word (role file 02 §12: never colour alone). */
export function Switch({ checked, onChange, label, onText, offText, disabled }: SwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      disabled={disabled}
      onClick={() => onChange(!checked)}
      className="inline-flex min-h-12 items-center gap-2 rounded-control px-1 text-label font-semibold text-ink"
    >
      <span
        className={`relative h-7 w-12 rounded-full border-2 transition-colors ${checked ? "border-action bg-action" : "border-rule-strong bg-sunken"}`}
        aria-hidden="true"
      >
        <span
          className={`absolute top-0.5 h-5 w-5 rounded-full bg-raised shadow-none transition-transform ${checked ? "translate-x-5" : "translate-x-0.5"} border border-rule-strong`}
        />
      </span>
      <span>{checked ? onText : offText}</span>
    </button>
  );
}
