"use client";

import { Fragment, type ReactNode } from "react";
import { useI18n } from "@/lib/i18n/i18n-provider";
import type { MessageKey } from "@/lib/i18n/messages";

/** "≈" that screen readers announce as "approximately" in the page language (role file 02 §12). */
export function Approx() {
  const { t } = useI18n();
  return (
    <>
      <span aria-hidden="true">≈</span>
      <span className="sr-only">{t("approx.sr")} </span>
    </>
  );
}

/** Plain text in which every "≈" is made accessible. */
export function AccessibleText({ text }: { text: string }) {
  const parts = text.split("≈");
  return (
    <>
      {parts.map((part, index) => (
        <Fragment key={index}>
          {index > 0 ? <Approx /> : null}
          {part}
        </Fragment>
      ))}
    </>
  );
}

/**
 * A translated message whose {placeholders} are React nodes (for example a figure in the ledger font or a market
 * name in bold). Word order stays the translation's own.
 */
export function Message({ id, values }: { id: MessageKey; values?: Record<string, ReactNode> }) {
  const { raw } = useI18n();
  return <>{renderTemplate(raw(id), values ?? {})}</>;
}

export function renderTemplate(template: string, values: Record<string, ReactNode>): ReactNode[] {
  const nodes: ReactNode[] = [];
  const pattern = /\{(\w+)\}/g;
  let last = 0;
  let match: RegExpExecArray | null;
  while ((match = pattern.exec(template)) !== null) {
    if (match.index > last) nodes.push(<AccessibleText key={`t${last}`} text={template.slice(last, match.index)} />);
    const name = match[1];
    nodes.push(<Fragment key={`v${match.index}`}>{name in values ? values[name] : match[0]}</Fragment>);
    last = match.index + match[0].length;
  }
  if (last < template.length) nodes.push(<AccessibleText key={`t${last}`} text={template.slice(last)} />);
  return nodes;
}
