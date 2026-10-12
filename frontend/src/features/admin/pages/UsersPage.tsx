import { useState } from "react";
import { useTranslation } from "react-i18next";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import Pagination from "@/components/common/Pagination";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import { primaryRole, ROLES, type Role } from "@/lib/auth/roles";
import { initialsOf } from "@/lib/format";
import { useDebouncedValue } from "@/lib/useDebouncedValue";
import { useAdminUsers, useChangeUserRole, useSetUserEnabled } from "../api/admin";

export default function UsersPage() {
  const { t } = useTranslation();
  const me = useCurrentUser();
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const search = useDebouncedValue(query.trim());
  const users = useAdminUsers(search, page);
  const changeRole = useChangeUserRole();
  const setEnabled = useSetUserEnabled();
  const error = changeRole.error ?? setEnabled.error;

  return <div className="page jobs-page">
    <div className="page-heading"><div><span className="eyebrow">{t("admin.eyebrow")}</span><h1>{t("admin.users.title")}</h1><p>{t("admin.users.subtitle")}</p></div></div>
    {error && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{error.message}</div>}
    <section className="jobs-table-panel panel">
      <div className="table-toolbar">
        <div className="table-search"><Icon name="search"/><input value={query} onChange={(e) => { setQuery(e.target.value); setPage(0); }} placeholder={t("admin.users.search")} aria-label={t("admin.users.search")}/></div>
        {users.data && <span>{t("admin.users.count", { count: users.data.totalElements })}</span>}
      </div>
      <div className="jobs-table-wrap">
        <table className="jobs-table">
          <thead><tr><th>{t("admin.users.user")}</th><th>{t("admin.users.role")}</th><th>{t("admin.users.status")}</th><th><span className="sr-only">{t("common.actions")}</span></th></tr></thead>
          <tbody>{users.data?.content.map((user) => {
            const isMe = user.id === me.id;
            const role = primaryRole(user.roles);
            return <tr key={user.id}>
              <td><span className="job-link"><Avatar initials={initialsOf(user.fullName || user.email)} color="violet" small/><span><strong>{user.fullName || user.email}</strong><small>{user.email}{isMe && ` · ${t("admin.users.you")}`}</small></span></span></td>
              <td><select className="inline-select" value={role ?? ""} disabled={isMe || changeRole.isPending} onChange={(e) => changeRole.mutate({ id: user.id, role: e.target.value as Role })} aria-label={t("admin.users.role")}>
                {!role && <option value="">{t("admin.users.noRole")}</option>}
                {ROLES.map((r) => <option key={r} value={r}>{t(`roles.${r}`)}</option>)}
              </select></td>
              <td><span className={`job-status ${user.enabled ? "published" : "closed"}`}><i></i>{t(user.enabled ? "admin.users.active" : "admin.users.disabled")}</span></td>
              <td>{!isMe && <button className="secondary-button" disabled={setEnabled.isPending} onClick={() => setEnabled.mutate({ id: user.id, enabled: !user.enabled })}>{t(user.enabled ? "admin.users.disable" : "admin.users.enable")}</button>}</td>
            </tr>;
          })}</tbody>
        </table>
        {users.isPending && <div className="table-empty"><strong>{t("common.loading")}</strong></div>}
        {users.isError && <div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong><span>{users.error.message}</span></div>}
      </div>
      {users.data && users.data.totalElements > 0 && <Pagination {...users.data} onPageChange={setPage}/>}
    </section>
  </div>;
}
