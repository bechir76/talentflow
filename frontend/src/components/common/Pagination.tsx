import { useTranslation } from "react-i18next";

type PaginationProps = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  onPageChange: (page: number) => void;
};

const WINDOW = 5;

/** Footer for paginated tables. `page` is zero-based, like the API. */
export default function Pagination({ page, size, totalElements, totalPages, onPageChange }: PaginationProps) {
  const { t } = useTranslation();
  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);
  const first = Math.max(0, Math.min(page - Math.floor(WINDOW / 2), totalPages - WINDOW));
  const pages = Array.from({ length: Math.min(WINDOW, totalPages) }, (_, i) => first + i);
  return <div className="table-footer">
    <span>{t("common.pagination.showing", { from, to, total: totalElements })}</span>
    <div>
      <button disabled={page === 0} onClick={() => onPageChange(page - 1)}>{t("common.pagination.previous")}</button>
      {pages.map((p) => <button key={p} className={p === page ? "active" : ""} aria-current={p === page ? "page" : undefined} onClick={() => onPageChange(p)}>{p + 1}</button>)}
      <button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>{t("common.pagination.next")}</button>
    </div>
  </div>;
}
