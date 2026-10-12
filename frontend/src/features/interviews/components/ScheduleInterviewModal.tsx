import { useState } from "react";
import { useTranslation } from "react-i18next";
import Modal from "@/components/common/Modal";
import { useInterviewers, useScheduleInterview } from "../api/interviews";

const DURATIONS = [30, 45, 60, 90, 120];

function tomorrow(): string {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return date.toLocaleDateString("sv-SE");
}

export default function ScheduleInterviewModal({ applicationId, open, onClose }: { applicationId: string; open: boolean; onClose: () => void }) {
  const { t } = useTranslation();
  const interviewers = useInterviewers(open);
  const schedule = useScheduleInterview(applicationId);
  const [date, setDate] = useState(tomorrow);
  const [time, setTime] = useState("10:00");
  const [duration, setDuration] = useState(60);
  const [selected, setSelected] = useState<string[]>([]);
  const [link, setLink] = useState("");

  const start = new Date(`${date}T${time}`);
  const valid = !Number.isNaN(start.getTime()) && start > new Date() && selected.length > 0;
  const toggle = (id: string) => setSelected((current) => current.includes(id) ? current.filter((x) => x !== id) : [...current, id]);
  const submit = () => schedule.mutate(
    { scheduledAt: start.toISOString(), durationMinutes: duration, interviewerIds: selected, meetingLink: link.trim() || undefined },
    { onSuccess: () => { setSelected([]); setLink(""); onClose(); } },
  );

  return <Modal open={open} title={t("interviews.scheduleTitle")} onClose={onClose} wide footer={<>
    <button className="cancel-button" onClick={onClose}>{t("common.cancel")}</button>
    <button className="primary-button" disabled={!valid || schedule.isPending} onClick={submit}>{t("interviews.schedule")}</button>
  </>}>
    <div className="form-card-body form-grid">
      <label>{t("interviews.date")}<input type="date" value={date} min={new Date().toLocaleDateString("sv-SE")} onChange={(e) => setDate(e.target.value)}/></label>
      <label>{t("interviews.time")}<input type="time" value={time} step={900} onChange={(e) => setTime(e.target.value)}/></label>
      <label>{t("interviews.duration")}<select value={duration} onChange={(e) => setDuration(Number(e.target.value))}>
        {DURATIONS.map((d) => <option key={d} value={d}>{t("interviews.minutes", { count: d })}</option>)}
      </select></label>
      <label>{t("interviews.meetingLink")}<input type="url" value={link} placeholder="https://meet.example.org/..." onChange={(e) => setLink(e.target.value)}/></label>
      <fieldset className="full-field interviewer-picker"><legend>{t("interviews.interviewers")}<span>*</span></legend>
        {interviewers.isPending && <small>{t("common.loading")}</small>}
        {interviewers.data?.map((user) => <label key={user.id} className="checkbox-label"><input type="checkbox" checked={selected.includes(user.id)} onChange={() => toggle(user.id)}/>{user.fullName || user.email}<small>{user.roles.map((r) => t(`roles.${r}`)).join(", ")}</small></label>)}
        {interviewers.isError && <small className="field-error">{interviewers.error.message}</small>}
      </fieldset>
      <small className="full-field">{t("interviews.inviteNote")}</small>
    </div>
    {schedule.isError && <p className="field-error" role="alert">{schedule.error.message}</p>}
  </Modal>;
}
