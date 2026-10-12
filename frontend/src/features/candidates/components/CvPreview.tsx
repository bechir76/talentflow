import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import { api } from "@/lib/api/client";
import { saveBlob } from "@/lib/download";
import { useCvUrl } from "../api/applications";

function formatSize(bytes: number): string {
  return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

/** The candidate's CV in the browser's own PDF viewer (no extra library). */
export default function CvPreview({ applicationId, available, fileName }: { applicationId: string; available: boolean; fileName: string }) {
  const { t } = useTranslation();
  const cv = useCvUrl(applicationId, available);
  // A separate request so the download is audit-logged as such (viewing is logged by the preview request).
  const download = useMutation({ mutationFn: async () => saveBlob(await api.blob(`/applications/${applicationId}/cv?download=true`), fileName) });
  return <article className="cv-panel panel">
    <div className="cv-toolbar">
      <div><span className="pdf-icon">PDF</span><div><strong>{fileName}</strong><small>{cv.size ? formatSize(cv.size) : t("candidate.cv.title")}</small></div></div>
      <div>{cv.url && <button className="icon-button" disabled={download.isPending} onClick={() => download.mutate()} aria-label={t("candidate.cv.download")} title={t("candidate.cv.download")}><Icon name="download"/></button>}</div>
    </div>
    <div className="pdf-canvas">
      {cv.url && <iframe className="cv-frame" src={cv.url} title={t("candidate.cv.title")}/>}
      {!available && <div className="table-empty"><Icon name="file" size={24}/><strong>{t("candidate.cv.unavailable")}</strong></div>}
      {available && cv.isLoading && <div className="table-empty"><strong>{t("common.loading")}</strong></div>}
      {cv.isError && <div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong></div>}
    </div>
  </article>;
}
