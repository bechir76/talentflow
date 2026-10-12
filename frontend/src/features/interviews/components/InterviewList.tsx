import { useState } from "react";
import { useTranslation } from "react-i18next";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import { initialsOf } from "@/lib/format";
import { useApplicationInterviews, useCancelInterview } from "../api/interviews";
import FeedbackModal from "./FeedbackModal";
import Rating from "./Rating";

const formatDateTime = (iso: string) => new Date(iso).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });

/** Interviews of one application with every interviewer's scorecard. */
export default function InterviewList({ applicationId, canManage }: { applicationId: string; canManage: boolean }) {
  const { t } = useTranslation();
  const interviews = useApplicationInterviews(applicationId);
  const cancel = useCancelInterview(applicationId);
  const [feedbackFor, setFeedbackFor] = useState<string>();

  if (interviews.isPending) return <div className="table-empty"><strong>{t("common.loading")}</strong></div>;
  if (interviews.isError) return <div className="table-empty"><Icon name="info" size={22}/><strong>{t("common.loadError")}</strong></div>;
  if (interviews.data.length === 0) {
    return <div className="table-empty"><Icon name="calendar" size={22}/><strong>{t("candidate.feedback.emptyTitle")}</strong><span>{t("candidate.feedback.emptyText")}</span></div>;
  }

  return <div className="feedback-list">
    {interviews.data.map((interview) => <section key={interview.id} className="interview-block">
      <header>
        <span className="timeline-icon"><Icon name="calendar" size={15}/></span>
        <div><strong>{formatDateTime(interview.scheduledAt)} · {t("interviews.minutes", { count: interview.durationMinutes })}</strong>
          <span>{interview.interviewers.map((i) => i.name).join(", ")}</span></div>
        {interview.meetingLink && <a className="link-button" href={interview.meetingLink} target="_blank" rel="noreferrer noopener">{t("interviews.join")}<Icon name="arrow" size={13}/></a>}
        {interview.canGiveFeedback && <button className="primary-button small" onClick={() => setFeedbackFor(interview.id)}>{t("interviews.giveFeedback")}</button>}
        {canManage && interview.feedback.length === 0 && new Date(interview.scheduledAt) > new Date() && <button className="secondary-button" disabled={cancel.isPending} onClick={() => cancel.mutate(interview.id)}>{t("interviews.cancel")}</button>}
      </header>
      {interview.feedback.map((feedback) => <article key={feedback.id}>
        <Avatar initials={initialsOf(feedback.authorName ?? "?")} color="blue"/>
        <div>
          <div><strong>{feedback.authorName}</strong><span>{formatDateTime(feedback.submittedAt)}</span></div>
          <dl className="scores">
            <dt>{t("interviews.criteria.technical")}</dt><dd><Rating value={feedback.technical}/></dd>
            <dt>{t("interviews.criteria.communication")}</dt><dd><Rating value={feedback.communication}/></dd>
            <dt>{t("interviews.criteria.cultureFit")}</dt><dd><Rating value={feedback.cultureFit}/></dd>
          </dl>
          {feedback.comment && <p>{feedback.comment}</p>}
        </div>
        <b className={`recommendation-badge ${feedback.recommendation.toLowerCase()}`}>{t(`interviews.recommendations.${feedback.recommendation}`)}</b>
      </article>)}
      {interview.feedback.length === 0 && <p className="no-feedback">{t("interviews.noFeedbackYet")}</p>}
    </section>)}
    <FeedbackModal applicationId={applicationId} interviewId={feedbackFor} onClose={() => setFeedbackFor(undefined)}/>
  </div>;
}
