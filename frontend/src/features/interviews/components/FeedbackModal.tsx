import { useState } from "react";
import { useTranslation } from "react-i18next";
import Modal from "@/components/common/Modal";
import { useSubmitFeedback, type Recommendation } from "../api/interviews";
import Rating from "./Rating";

const RECOMMENDATIONS: Recommendation[] = ["HIRE", "UNSURE", "NO_HIRE"];
type Criterion = "technical" | "communication" | "cultureFit";
const CRITERIA: Criterion[] = ["technical", "communication", "cultureFit"];

/** Interview scorecard: three ratings, a recommendation and an optional comment. */
export default function FeedbackModal({ applicationId, interviewId, onClose }: { applicationId: string; interviewId: string | undefined; onClose: () => void }) {
  const { t } = useTranslation();
  const submit = useSubmitFeedback(applicationId);
  const [scores, setScores] = useState<Record<Criterion, number>>({ technical: 0, communication: 0, cultureFit: 0 });
  const [recommendation, setRecommendation] = useState<Recommendation>();
  const [comment, setComment] = useState("");
  const complete = CRITERIA.every((c) => scores[c] > 0) && recommendation;

  const send = () => {
    if (!interviewId || !recommendation) return;
    submit.mutate({ interviewId, input: { ...scores, recommendation, comment: comment.trim() || undefined } }, {
      onSuccess: () => { setScores({ technical: 0, communication: 0, cultureFit: 0 }); setRecommendation(undefined); setComment(""); onClose(); },
    });
  };

  return <Modal open={Boolean(interviewId)} title={t("interviews.feedbackTitle")} onClose={onClose} footer={<>
    <button className="cancel-button" onClick={onClose}>{t("common.cancel")}</button>
    <button className="primary-button" disabled={!complete || submit.isPending} onClick={send}>{t("interviews.submitFeedback")}</button>
  </>}>
    <div className="scorecard">
      {CRITERIA.map((criterion) => <div className="scorecard-row" key={criterion}>
        <span>{t(`interviews.criteria.${criterion}`)}</span>
        <Rating value={scores[criterion]} label={t(`interviews.criteria.${criterion}`)} onChange={(value) => setScores({ ...scores, [criterion]: value })}/>
      </div>)}
    </div>
    <fieldset className="recommendation-picker"><legend>{t("interviews.recommendation")}</legend>
      {RECOMMENDATIONS.map((option) => <label key={option} className={`recommendation ${option.toLowerCase()} ${recommendation === option ? "selected" : ""}`}>
        <input type="radio" name="recommendation" checked={recommendation === option} onChange={() => setRecommendation(option)}/>{t(`interviews.recommendations.${option}`)}
      </label>)}
    </fieldset>
    <label className="feedback-comment">{t("interviews.comment")}<textarea value={comment} maxLength={4000} onChange={(e) => setComment(e.target.value)} placeholder={t("interviews.commentPlaceholder")}/></label>
    {submit.isError && <p className="field-error" role="alert">{submit.error.message}</p>}
  </Modal>;
}
