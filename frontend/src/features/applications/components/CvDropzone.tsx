import { useRef, useState, type DragEvent } from "react";
import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";

type CvDropzoneProps = {
  file: File | undefined;
  onChange: (file: File | undefined) => void;
  invalid?: boolean;
};

function formatSize(bytes: number): string {
  return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

/** Drop area for the CV. Clicking it or pressing Enter opens the file picker. */
export default function CvDropzone({ file, onChange, invalid }: CvDropzoneProps) {
  const { t } = useTranslation();
  const input = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);

  const onDrop = (event: DragEvent) => {
    event.preventDefault();
    setDragging(false);
    onChange(event.dataTransfer.files[0]);
  };

  if (file) {
    return <div className="cv-selected">
      <span className="pdf-icon">PDF</span>
      <div><strong>{file.name}</strong><small>{formatSize(file.size)}</small></div>
      <button type="button" className="secondary-button" onClick={() => onChange(undefined)}>{t("apply.cv.replace")}</button>
    </div>;
  }

  return <div
    role="button"
    tabIndex={0}
    className={`dropzone ${dragging ? "dragging" : ""} ${invalid ? "invalid" : ""}`}
    onClick={() => input.current?.click()}
    onKeyDown={(event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); input.current?.click(); } }}
    onDragOver={(event) => { event.preventDefault(); setDragging(true); }}
    onDragLeave={() => setDragging(false)}
    onDrop={onDrop}
  >
    <span className="sparkle-icon"><Icon name="download" size={18}/></span>
    <strong>{t("apply.cv.drop")}</strong>
    <small>{t("apply.cv.hint")}</small>
    <input ref={input} type="file" accept="application/pdf,.pdf" hidden onChange={(event) => onChange(event.target.files?.[0])}/>
  </div>;
}
