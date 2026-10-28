import Link from "next/link";
import type { ButtonHTMLAttributes, ComponentProps, ReactNode } from "react";

export type ButtonVariant = "primary" | "secondary" | "tertiary" | "destructive" | "hypothetical";

const VARIANTS: Record<ButtonVariant, string> = {
  primary: "bg-action text-inverse border border-action hover:bg-action-hover",
  secondary: "bg-raised text-ink border border-rule-strong hover:bg-sunken",
  tertiary: "bg-transparent text-net border border-transparent underline underline-offset-4 hover:bg-sunken",
  destructive: "bg-raised text-error border border-error hover:bg-error-bg",
  hypothetical: "bg-hyp-surface text-hyp border-2 border-dashed border-hyp hover:bg-raised",
};

/** 48 px minimum touch target (role file 02 §12); 4 px corners, no shadow (§1.1). */
export function buttonClasses(variant: ButtonVariant = "primary", block = false): string {
  return [
    "inline-flex min-h-12 items-center justify-center gap-2 rounded-control px-5 py-2 text-body font-semibold",
    "transition-colors disabled:opacity-60 aria-disabled:opacity-60",
    VARIANTS[variant],
    block ? "w-full" : "",
  ].join(" ");
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  block?: boolean;
  /** Busy at once on submit (APPROVED PDD-13); the button cannot be pressed twice. */
  busy?: boolean;
  children: ReactNode;
}

export function Button({ variant = "primary", block, busy = false, disabled, className = "", children, type = "button", ...props }: ButtonProps) {
  return (
    <button
      type={type}
      className={`${buttonClasses(variant, block)} ${className}`}
      disabled={disabled || busy}
      aria-busy={busy || undefined}
      {...props}
    >
      {children}
      {busy ? <BusyDots /> : null}
    </button>
  );
}

type ButtonLinkProps = ComponentProps<typeof Link> & { variant?: ButtonVariant; block?: boolean };

export function ButtonLink({ variant = "primary", block, className = "", ...props }: ButtonLinkProps) {
  return <Link className={`${buttonClasses(variant, block)} ${className}`} {...props} />;
}

function BusyDots() {
  return (
    <span className="inline-flex gap-1" aria-hidden="true">
      {[0, 1, 2].map((dot) => (
        <span
          key={dot}
          className="h-1.5 w-1.5 animate-pulse rounded-full bg-current motion-reduce:animate-none"
          style={{ animationDelay: `${dot * 150}ms` }}
        />
      ))}
    </span>
  );
}
