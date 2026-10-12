import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm, type FieldPath } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useParams } from "react-router";
import Icon from "@/components/common/Icon";
import { usePublicJob } from "@/features/careers/api/publicJobs";
import { WorkModeBadge } from "@/features/jobs/components/JobBadges";
import { ApiError } from "@/lib/api/client";
import { useAuth } from "@/lib/auth/AuthContext";
import { useApply } from "../api/applications";
import { applyFormSchema, MAX_COVER_LETTER, type ApplyFormValues } from "../applyFormSchema";
import CvDropzone from "../components/CvDropzone";

/** Application form for a published job (/careers/:id/apply). Candidates must be signed in. */
export default function ApplyPage() {
  const { t } = useTranslation();
  const { id = "" } = useParams();
  const location = useLocation();
  const { state, login, register } = useAuth();
  const job = usePublicJob(id);

  if (job.isPending) return <div className="table-empty"><strong>{t("common.loading")}</strong></div>;
  if (job.isError) {
    return <div className="table-empty career-missing"><Icon name="search" size={24}/><strong>{t("careers.notFound.title")}</strong><Link className="secondary-button" to="/careers">{t("careers.backToJobs")}</Link></div>;
  }

  const header = <>
    <div className="breadcrumbs"><Link to="/careers">{t("careers.title")}</Link><Icon name="chevron" size={13}/><Link to={`/careers/${id}`}>{job.data.title}</Link><Icon name="chevron" size={13}/><strong>{t("apply.breadcrumb")}</strong></div>
    <header className="career-detail-head">
      <div><span className="eyebrow">{t("apply.eyebrow")}</span><h1>{job.data.title}</h1><p><Icon name="map" size={14}/>{job.data.location}<i></i><WorkModeBadge mode={job.data.workMode}/></p></div>
    </header>
  </>;

  if (state.status !== "authenticated" || state.user.primaryRole !== "CANDIDATE") {
    const isStaff = state.status === "authenticated";
    return <div className="career-detail">{header}
      <section className="panel apply-gate">
        <span className="sparkle-icon"><Icon name={isStaff ? "lock" : "users"} size={18}/></span>
        <h2>{t(isStaff ? "apply.staffTitle" : "apply.signInTitle")}</h2>
        <p>{t(isStaff ? "apply.staffText" : "apply.signInText")}</p>
        {!isStaff && <div className="apply-gate-actions">
          <button className="primary-button" onClick={() => login(location.pathname)}>{t("auth.signIn")}<Icon name="arrow"/></button>
          <button className="secondary-button" onClick={() => register(location.pathname)}>{t("auth.createAccount")}</button>
        </div>}
      </section>
    </div>;
  }

  return <div className="career-detail">{header}<ApplyForm jobId={id} fullName={state.user.fullName} email={state.user.email}/></div>;
}

function ApplyForm({ jobId, fullName, email }: { jobId: string; fullName: string; email?: string }) {
  const { t } = useTranslation();
  const apply = useApply(jobId);
  const [serverError, setServerError] = useState<string>();
  const { register, control, handleSubmit, watch, setError, formState: { errors, isSubmitting } } = useForm<ApplyFormValues>({
    resolver: zodResolver(applyFormSchema),
    defaultValues: { phone: "", location: "", coverLetter: "", consent: false },
  });

  const onSubmit = handleSubmit(async (values) => {
    setServerError(undefined);
    try {
      await apply.mutateAsync(values);
    } catch (error) {
      if (error instanceof ApiError && error.problem?.errors?.length) {
        error.problem.errors.forEach((e) => setError(e.field as FieldPath<ApplyFormValues>, { message: e.message }));
      } else {
        setServerError(error instanceof Error ? error.message : String(error));
      }
    }
  });

  const fieldError = (name: FieldPath<ApplyFormValues>) => {
    const message = errors[name]?.message;
    return message ? <small className="field-error" role="alert">{t(`validation.${message}` as never, { defaultValue: message })}</small> : null;
  };

  if (apply.isSuccess) {
    return <section className="panel apply-success" aria-live="polite">
      <span className="success-dot"><Icon name="check" size={22}/></span>
      <h2>{t("apply.success.title")}</h2>
      <p>{t("apply.success.text")}</p>
      <div className="apply-gate-actions">
        <Link className="primary-button" to="/portal/applications">{t("apply.success.viewApplications")}<Icon name="arrow"/></Link>
        <Link className="secondary-button" to="/careers">{t("apply.success.moreJobs")}</Link>
      </div>
    </section>;
  }

  return <form className="job-form apply-form" noValidate onSubmit={onSubmit}>
    {serverError && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{serverError}</div>}
    <section className="form-card panel">
      <div className="form-card-head"><span>1</span><div><h2>{t("apply.contactTitle")}</h2><p>{t("apply.contactText")}</p></div></div>
      <div className="form-card-body form-grid">
        <label>{t("apply.fullName")}<input value={fullName} readOnly disabled/></label>
        <label>{t("apply.email")}<input value={email ?? ""} readOnly disabled/></label>
        <label>{t("apply.phone")}<input type="tel" autoComplete="tel" className={errors.phone ? "invalid" : ""} {...register("phone")}/>{fieldError("phone")}</label>
        <label>{t("apply.location")}<input autoComplete="address-level2" className={errors.location ? "invalid" : ""} {...register("location")} placeholder={t("jobs.form.locationPlaceholder")}/>{fieldError("location")}</label>
        <small className="full-field apply-account-note">{t("apply.accountNote")}</small>
      </div>
    </section>
    <section className="form-card panel">
      <div className="form-card-head"><span>2</span><div><h2>{t("apply.documentsTitle")}</h2><p>{t("apply.documentsText")}</p></div></div>
      <div className="form-card-body">
        <label>{t("apply.cv.label")}<span>*</span></label>
        <Controller control={control} name="cv" render={({ field }) => <CvDropzone file={field.value} onChange={field.onChange} invalid={Boolean(errors.cv)}/>}/>
        {fieldError("cv")}
        <label className="apply-cover" htmlFor="cover-letter">{t("apply.coverLetter")}</label>
        <div className="rich-editor"><textarea id="cover-letter" className="editor-content editor-textarea apply-cover-text" maxLength={MAX_COVER_LETTER} {...register("coverLetter")} placeholder={t("apply.coverLetterPlaceholder")}/></div>
        {fieldError("coverLetter")}
        <div className="editor-foot"><span><Icon name="info" size={13}/>{t("apply.coverLetterHint")}</span><b>{t("jobs.form.characters", { count: watch("coverLetter")?.length ?? 0 })}</b></div>
      </div>
    </section>
    <section className="form-card panel">
      <div className="form-card-head"><span>3</span><div><h2>{t("apply.privacyTitle")}</h2><p>{t("apply.privacyText")}</p></div></div>
      <div className="form-card-body">
        <label className={`consent-box ${errors.consent ? "invalid" : ""}`}>
          <input type="checkbox" {...register("consent")}/>
          <span><strong>{t("apply.consentLabel")}<span>*</span></strong>{t("apply.consentText")}</span>
        </label>
        {fieldError("consent")}
        <p className="career-privacy"><Icon name="shield" size={14}/>{t("apply.aiNotice")}</p>
      </div>
    </section>
    <div className="job-form-actions">
      <Link className="cancel-button" to={`/careers/${jobId}`}>{t("common.cancel")}</Link><span></span>
      <button type="submit" disabled={isSubmitting} className="primary-button"><Icon name="check"/>{t(isSubmitting ? "apply.submitting" : "apply.submit")}</button>
    </div>
  </form>;
}
