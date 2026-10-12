import { useEffect, useRef, type ReactNode } from "react";
import { useTranslation } from "react-i18next";
import Icon from "./Icon";

type ModalProps = {
  open: boolean;
  title: string;
  onClose: () => void;
  children: ReactNode;
  footer?: ReactNode;
  wide?: boolean;
};

/**
 * Dialog built on the native {@code <dialog>} element: focus trapping, Escape to close and the backdrop come
 * from the browser.
 */
export default function Modal({ open, title, onClose, children, footer, wide = false }: ModalProps) {
  const { t } = useTranslation();
  const dialog = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const element = dialog.current;
    if (!element) return;
    if (open && !element.open) element.showModal();
    if (!open && element.open) element.close();
  }, [open]);

  return <dialog ref={dialog} className={`modal ${wide ? "wide" : ""}`} onClose={onClose} onCancel={onClose} aria-labelledby="modal-title">
    <div className="modal-head"><h2 id="modal-title">{title}</h2><button type="button" className="icon-button" onClick={onClose} aria-label={t("common.close")}><Icon name="plus" size={18}/></button></div>
    <div className="modal-body">{children}</div>
    {footer && <div className="modal-foot">{footer}</div>}
  </dialog>;
}
