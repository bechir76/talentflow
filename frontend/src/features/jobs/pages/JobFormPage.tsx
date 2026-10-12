import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm, type FieldPath } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router";
import Icon, { type IconName } from "@/components/common/Icon";
import { ApiError } from "@/lib/api/client";
import { useChangeJobStatus, useCreateJob, useDepartments, useJob, useUpdateJob, type Job, type JobInput, type JobStatus, type WorkMode } from "../api/jobs";
import { JobStatusBadge } from "../components/JobBadges";
import SkillsInput from "../components/SkillsInput";
import { DESCRIPTION_MAX, jobFormSchema, type JobFormValues } from "../jobFormSchema";

const WORK_MODES: { mode: WorkMode; icon: IconName }[] = [
  { mode: "REMOTE", icon: "globe" },
  { mode: "HYBRID", icon: "users" },
  { mode: "ONSITE", icon: "briefcase" },
];

const EMPTY: JobFormValues = {
  title: "", departmentId: "", location: "", workMode: "HYBRID",
  salaryMin: undefined, salaryMax: undefined, requiredSkills: [], description: "",
};

function toValues(job: Job): JobFormValues {
  return {
    title: job.title, departmentId: job.departmentId, location: job.location, workMode: job.workMode,
    salaryMin: job.salaryMin ?? undefined, salaryMax: job.salaryMax ?? undefined,
    requiredSkills: job.requiredSkills, description: job.description,
  };
}

const toNumber = (value: unknown) => (value === "" || value == null ? undefined : Number(value));

/** Create a new job (/jobs/new) or edit an existing one (/jobs/:id/edit). */
export default function JobFormPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const existing = useJob(id);
  const departments = useDepartments();
  const createJob = useCreateJob();
  const updateJob = useUpdateJob(id ?? "");
  const changeStatus = useChangeJobStatus();
  const [serverError, setServerError] = useState<string>();

  const { register, control, handleSubmit, reset, watch, setError, formState: { errors, isSubmitting } } =
    useForm<JobFormValues>({ resolver: zodResolver(jobFormSchema), defaultValues: EMPTY });

  useEffect(() => {
    if (existing.data) reset(toValues(existing.data));
  }, [existing.data, reset]);

  const descriptionLength = watch("description")?.length ?? 0;
  const status = existing.data?.status;

  const save = (targetStatus: JobStatus | undefined) => handleSubmit(async (values) => {
    setServerError(undefined);
    const input: JobInput = { ...values, status: targetStatus };
    try {
      if (isEdit && id) {
        await updateJob.mutateAsync(input);
        if (targetStatus && targetStatus !== status) await changeStatus.mutateAsync({ id, status: targetStatus });
      } else {
        await createJob.mutateAsync(input);
      }
      navigate("/jobs");
    } catch (error) {
      if (error instanceof ApiError && error.problem?.errors?.length) {
        error.problem.errors.forEach((e) => setError(e.field as FieldPath<JobFormValues>, { message: e.message }));
      } else {
        setServerError(error instanceof Error ? error.message : String(error));
      }
    }
  });

  const fieldError = (name: FieldPath<JobFormValues>) => {
    const message = errors[name as keyof JobFormValues]?.message;
    if (!message) return null;
    const key = `validation.${message}`;
    return <small className="field-error" role="alert">{t(key as never, { defaultValue: message, min: 50 })}</small>;
  };

  if (isEdit && existing.isError) {
    return <div className="page create-job-page"><div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong><span>{existing.error.message}</span></div></div>;
  }

  return <div className="page create-job-page">
    <div className="breadcrumbs"><button onClick={() => navigate("/jobs")}>{t("jobs.title")}</button><Icon name="chevron" size={13}/><strong>{t(isEdit ? "jobs.form.editBreadcrumb" : "jobs.form.createBreadcrumb")}</strong></div>
    <div className="create-job-heading">
      <div><div className="job-title-line"><h1>{t(isEdit ? "jobs.form.editTitle" : "jobs.form.createTitle")}</h1>{status && <JobStatusBadge status={status}/>}</div><p>{t("jobs.form.subtitle")}</p></div>
      <span><Icon name="shield" size={15}/>{t("jobs.form.gdprBadge")}</span>
    </div>
    <form className="job-form" noValidate onSubmit={save(isEdit ? undefined : "PUBLISHED")}>
      {serverError && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{serverError}</div>}
      <section className="form-card panel">
        <div className="form-card-head"><span>1</span><div><h2>{t("jobs.form.detailsTitle")}</h2><p>{t("jobs.form.detailsText")}</p></div></div>
        <div className="form-card-body form-grid">
          <label className="full-field">{t("jobs.form.title")}<span>*</span><input className={errors.title ? "invalid" : ""} {...register("title")} placeholder={t("jobs.form.titlePlaceholder")}/>{fieldError("title")}</label>
          <label>{t("jobs.form.department")}<span>*</span>
            <select className={errors.departmentId ? "invalid" : ""} {...register("departmentId")}>
              <option value="">{t("jobs.form.selectDepartment")}</option>
              {departments.data?.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>{fieldError("departmentId")}
          </label>
          <label>{t("jobs.form.location")}<span>*</span><div className="input-with-icon"><Icon name="map" size={15}/><input className={errors.location ? "invalid" : ""} {...register("location")} placeholder={t("jobs.form.locationPlaceholder")}/></div>{fieldError("location")}</label>
          <fieldset className="full-field work-mode-field"><legend>{t("jobs.form.workMode")}<span>*</span></legend><div>
            {WORK_MODES.map(({ mode, icon }) => <label key={mode}><input type="radio" value={mode} {...register("workMode")}/><span><Icon name={icon}/><strong>{t(`jobs.workMode.${mode}`)}</strong><small>{t(`jobs.form.workModeHint.${mode}`)}</small></span></label>)}
          </div></fieldset>
        </div>
      </section>
      <section className="form-card panel">
        <div className="form-card-head"><span>2</span><div><h2>{t("jobs.form.compensationTitle")}</h2><p>{t("jobs.form.compensationText")}</p></div></div>
        <div className="form-card-body form-grid">
          <div className="full-field salary-group two-fields"><label>{t("jobs.form.salaryRange")}</label><div>
            <label><span>{t("jobs.form.salaryMin")}</span><div className="currency-input"><b>€</b><input type="number" min={0} step={1000} className={errors.salaryMin ? "invalid" : ""} {...register("salaryMin", { setValueAs: toNumber })}/></div></label>
            <i>{t("jobs.form.to")}</i>
            <label><span>{t("jobs.form.salaryMax")}</span><div className="currency-input"><b>€</b><input type="number" min={0} step={1000} className={errors.salaryMax ? "invalid" : ""} {...register("salaryMax", { setValueAs: toNumber })}/></div></label>
          </div>{fieldError("salaryMin")}{fieldError("salaryMax")}<small>{t("jobs.form.salaryHint")}</small></div>
          <label className="full-field" htmlFor="job-skills">{t("jobs.form.skills")}<span>*</span>
            <Controller control={control} name="requiredSkills" render={({ field }) => <SkillsInput id="job-skills" value={field.value} onChange={field.onChange} onBlur={field.onBlur} invalid={Boolean(errors.requiredSkills)}/>}/>
            {fieldError("requiredSkills")}<small>{t("jobs.form.skillsHint")}</small>
          </label>
        </div>
      </section>
      <section className="form-card panel">
        <div className="form-card-head"><span>3</span><div><h2>{t("jobs.form.descriptionTitle")}</h2><p>{t("jobs.form.descriptionText")}</p></div></div>
        <div className="form-card-body">
          <label htmlFor="job-description">{t("jobs.form.description")}<span>*</span></label>
          <div className={`rich-editor ${errors.description ? "invalid" : ""}`}>
            <textarea id="job-description" className="editor-content editor-textarea" maxLength={DESCRIPTION_MAX} {...register("description")} placeholder={t("jobs.form.descriptionPlaceholder")}/>
          </div>
          {fieldError("description")}
          <div className="editor-foot"><span><Icon name="info" size={13}/>{t("jobs.form.inclusiveHint")}</span><b>{t("jobs.form.characters", { count: descriptionLength })}</b></div>
        </div>
      </section>
      <div className="job-form-actions">
        <button type="button" className="cancel-button" onClick={() => navigate("/jobs")}>{t("common.cancel")}</button><span></span>
        {(!isEdit || status === "DRAFT") && <button type="button" disabled={isSubmitting} className="secondary-button draft-button" onClick={save(isEdit ? undefined : "DRAFT")}><Icon name="file"/>{t("jobs.form.saveDraft")}</button>}
        {isEdit && status !== "DRAFT"
          ? <button type="submit" disabled={isSubmitting} className="primary-button"><Icon name="check"/>{t("jobs.form.saveChanges")}</button>
          : <button type="button" disabled={isSubmitting} className="primary-button" onClick={save("PUBLISHED")}><Icon name="check"/>{t(isSubmitting ? "jobs.form.publishing" : "jobs.form.publish")}</button>}
      </div>
    </form>
  </div>;
}
