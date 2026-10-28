"use client";

import { useId, useState, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from "react";
import { EyeIcon, EyeOffIcon } from "@/components/icons";

const CONTROL =
  "block min-h-12 w-full rounded-control border border-rule-strong bg-raised px-3 py-2 text-body text-ink "
  + "placeholder:text-ink-2 aria-[invalid=true]:border-error aria-[invalid=true]:border-2 disabled:bg-sunken";

interface FieldProps {
  label: ReactNode;
  /** Shown under the label; never instead of it (role file 02 §12). */
  help?: ReactNode;
  error?: string | null;
  optionalLabel?: string;
  children: (ids: { inputId: string; describedBy: string | undefined; invalid: boolean }) => ReactNode;
}

/** A visible label, an optional help line and an announced error next to the field (role file 02 §12). */
export function Field({ label, help, error, optionalLabel, children }: FieldProps) {
  const inputId = useId();
  const helpId = `${inputId}-help`;
  const errorId = `${inputId}-error`;
  const describedBy = [help ? helpId : null, error ? errorId : null].filter(Boolean).join(" ") || undefined;
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={inputId} className="text-label font-semibold text-ink">
        {label}
        {optionalLabel ? <span className="font-normal text-ink-2"> {optionalLabel}</span> : null}
      </label>
      {children({ inputId, describedBy, invalid: Boolean(error) })}
      {help ? <p id={helpId} className="text-caption text-ink-2">{help}</p> : null}
      <p id={errorId} className="text-label font-medium text-error" aria-live="polite">
        {error ?? ""}
      </p>
    </div>
  );
}

type TextInputProps = InputHTMLAttributes<HTMLInputElement> & { invalid?: boolean };

export function TextInput({ invalid, className = "", ...props }: TextInputProps) {
  return <input className={`${CONTROL} ${className}`} aria-invalid={invalid || undefined} {...props} />;
}

type SelectProps = SelectHTMLAttributes<HTMLSelectElement> & { invalid?: boolean };

export function Select({ invalid, className = "", children, ...props }: SelectProps) {
  return (
    <select className={`${CONTROL} ${className}`} aria-invalid={invalid || undefined} {...props}>
      {children}
    </select>
  );
}

interface PasswordInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "type"> {
  invalid?: boolean;
  showLabel: string;
  hideLabel: string;
}

/** Password with a show/hide control (role file 02 §8 S10, S15). */
export function PasswordInput({ invalid, showLabel, hideLabel, className = "", ...props }: PasswordInputProps) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="relative">
      <input
        type={visible ? "text" : "password"}
        className={`${CONTROL} pr-14 ${className}`}
        aria-invalid={invalid || undefined}
        {...props}
      />
      <button
        type="button"
        className="absolute inset-y-0 right-0 flex min-w-12 items-center justify-center text-ink-2 hover:text-ink"
        onClick={() => setVisible((value) => !value)}
        aria-label={visible ? hideLabel : showLabel}
        aria-pressed={visible}
      >
        {visible ? <EyeOffIcon /> : <EyeIcon />}
      </button>
    </div>
  );
}

interface PhoneInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "type" | "value" | "onChange"> {
  value: string;
  onChange: (digits: string) => void;
  invalid?: boolean;
}

/** Ten-digit Indian mobile number after a fixed "+91" (role file 02 §8 S10; ADR-0016 E.164). */
export function PhoneInput({ value, onChange, invalid, className = "", ...props }: PhoneInputProps) {
  return (
    <div className={`flex min-h-12 items-stretch rounded-control border bg-raised ${invalid ? "border-2 border-error" : "border-rule-strong"}`}>
      <span className="flex items-center border-r border-rule px-3 text-body text-ink-2 figure" aria-hidden="true">+91</span>
      <input
        type="tel"
        inputMode="numeric"
        autoComplete="tel-national"
        maxLength={10}
        className={`min-w-0 flex-1 rounded-control bg-transparent px-3 text-body text-ink figure focus-visible:outline-none ${className}`}
        value={value}
        onChange={(event) => onChange(event.target.value.replace(/\D/g, "").slice(0, 10))}
        aria-invalid={invalid || undefined}
        {...props}
      />
    </div>
  );
}
