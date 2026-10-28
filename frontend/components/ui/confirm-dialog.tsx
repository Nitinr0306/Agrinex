"use client";

import { useEffect, useRef, type ReactNode } from "react";
import { Button } from "./button";

/**
 * A confirmation for a destructive action, on the native modal <dialog>: it traps focus while open, closes on
 * Escape, and returns focus to the control that opened it (role file 02 §12, §16).
 */
export function ConfirmDialog({ open, title, confirmLabel, cancelLabel, busy, onConfirm, onCancel }: {
  open: boolean;
  title: ReactNode;
  confirmLabel: string;
  cancelLabel: string;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const returnFocus = useRef<HTMLElement | null>(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    if (open && !dialog.open) {
      returnFocus.current = document.activeElement as HTMLElement | null;
      dialog.showModal();
    } else if (!open && dialog.open) {
      dialog.close();
      returnFocus.current?.focus();
    }
  }, [open]);

  return (
    <dialog
      ref={dialogRef}
      onCancel={(event) => {
        event.preventDefault();
        onCancel();
      }}
      className="m-auto w-[min(28rem,calc(100vw-2rem))] rounded-card border border-rule-strong bg-raised p-0 text-ink backdrop:bg-ink/40"
      aria-labelledby="confirm-title"
    >
      <div className="flex flex-col gap-4 p-5">
        <p id="confirm-title" className="text-body font-semibold">{title}</p>
        <div className="flex flex-col-reverse gap-2 tablet:flex-row tablet:justify-end">
          <Button variant="secondary" onClick={onCancel}>{cancelLabel}</Button>
          <Button variant="destructive" busy={busy} onClick={onConfirm}>{confirmLabel}</Button>
        </div>
      </div>
    </dialog>
  );
}
