import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import Icon from "./Icon";

export type RowMenuItem = { label: string; onSelect: () => void; danger?: boolean };

/** The "⋯" button at the end of a table row, opening a small list of actions. */
export default function RowMenu({ items }: { items: RowMenuItem[] }) {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const close = (event: MouseEvent | KeyboardEvent) => {
      if (event instanceof KeyboardEvent ? event.key === "Escape" : !ref.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", close);
    document.addEventListener("keydown", close);
    return () => {
      document.removeEventListener("mousedown", close);
      document.removeEventListener("keydown", close);
    };
  }, [open]);

  return <div className="row-menu" ref={ref}>
    <button className="more-button" aria-label={t("common.actions")} aria-haspopup="menu" aria-expanded={open} onClick={() => setOpen(!open)}><Icon name="more"/></button>
    {open && <div className="row-menu-list" role="menu">
      {items.map((item) => <button key={item.label} role="menuitem" className={item.danger ? "danger" : ""} onClick={() => { setOpen(false); item.onSelect(); }}>{item.label}</button>)}
    </div>}
  </div>;
}
