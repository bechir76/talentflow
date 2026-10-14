import { useMemo, useState } from "react";
import { DndContext, DragOverlay, KeyboardSensor, PointerSensor, useSensor, useSensors, type DragEndEvent, type DragStartEvent } from "@dnd-kit/core";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router";
import Icon from "@/components/common/Icon";
import { useJob, useJobs } from "@/features/jobs/api/jobs";
import { JobStatusBadge } from "@/features/jobs/components/JobBadges";
import type { ApplicationStage } from "@/features/applications/api/applications";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import { formatSalaryRange } from "@/lib/format";
import { BOARD_STAGES, canMove, useMoveStage, usePipelineBoard, type PipelineCard } from "../api/pipeline";
import CandidateCard from "../components/CandidateCard";
import KanbanColumn from "../components/KanbanColumn";

type SortOrder = "score" | "newest";

/** A small drag distance keeps a plain click on a card working as "open". */
const DRAG_ACTIVATION_DISTANCE = 6;

function sortCards(cards: PipelineCard[], order: SortOrder): PipelineCard[] {
  return [...cards].sort((a, b) => order === "score"
    ? (b.aiScore ?? -1) - (a.aiScore ?? -1)
    : b.appliedAt.localeCompare(a.appliedAt));
}

export default function PipelinePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const user = useCurrentUser();
  const canManage = user.roles.includes("RECRUITER") || user.roles.includes("ADMIN");
  const [params, setParams] = useSearchParams();
  const openJobs = useJobs({ status: "PUBLISHED", page: 0 });
  const jobId = params.get("jobId") ?? openJobs.data?.content[0]?.id;
  const job = useJob(jobId);
  const board = usePipelineBoard(jobId);
  const moveStage = useMoveStage(jobId);
  const [search, setSearch] = useState("");
  const [order, setOrder] = useState<SortOrder>("score");
  const [dragged, setDragged] = useState<PipelineCard>();
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: DRAG_ACTIVATION_DISTANCE } }),
    useSensor(KeyboardSensor),
  );

  const columns = useMemo(() => {
    const query = search.trim().toLowerCase();
    const cards = (board.data?.content ?? []).filter((card) => !query || (card.candidateName ?? "").toLowerCase().includes(query));
    return BOARD_STAGES.map((stage) => ({ stage, cards: sortCards(cards.filter((card) => card.stage === stage), order) }));
  }, [board.data, search, order]);

  const onDragStart = (event: DragStartEvent) => {
    setDragged(board.data?.content.find((card) => card.id === event.active.id));
    moveStage.reset();
  };

  const onDragEnd = (event: DragEndEvent) => {
    const card = dragged;
    setDragged(undefined);
    const target = event.over?.id as ApplicationStage | undefined;
    if (card && target && canMove(card.stage, target)) {
      moveStage.mutate({ applicationId: card.id, stage: target });
    }
  };

  const salary = job.data ? formatSalaryRange(job.data.salaryMin, job.data.salaryMax, job.data.currency) : undefined;
  const total = board.data?.totalElements ?? 0;

  return <div className="page pipeline-page">
    <div className="breadcrumbs"><span>{t("jobs.title")}</span><Icon name="chevron" size={13}/>{job.data && <><span>{job.data.departmentName}</span><Icon name="chevron" size={13}/><strong>{job.data.title}</strong></>}</div>
    <div className="job-heading">
      <div>
        <div className="job-title-line"><h1>{job.data?.title ?? t("pipeline.title")}</h1>{job.data && <JobStatusBadge status={job.data.status}/>}</div>
        {job.data && <p>{job.data.location} · {t(`jobs.workMode.${job.data.workMode}`)}{salary && <span>{salary}</span>}</p>}
      </div>
      <div className="job-actions">
        <select className="job-select" value={jobId ?? ""} onChange={(e) => setParams({ jobId: e.target.value })} aria-label={t("pipeline.selectJob")}>
          {jobId && !openJobs.data?.content.some((j) => j.id === jobId) && job.data && <option value={jobId}>{job.data.title}</option>}
          {openJobs.data?.content.map((j) => <option key={j.id} value={j.id}>{j.title}</option>)}
        </select>
        {canManage && jobId && <button className="secondary-button" onClick={() => navigate(`/jobs/${jobId}/edit`)}><Icon name="file"/>{t("jobs.actions.edit")}</button>}
      </div>
    </div>
    <div className="pipeline-toolbar">
      <div className="pipeline-search"><Icon name="search"/><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder={t("pipeline.searchPlaceholder")} aria-label={t("pipeline.searchPlaceholder")}/></div>
      <button className={`filter-button ${order === "score" ? "active" : ""}`} onClick={() => setOrder("score")}><Icon name="sparkles"/>{t("pipeline.sortScore")}</button>
      <button className={`filter-button ${order === "newest" ? "active" : ""}`} onClick={() => setOrder("newest")}><Icon name="calendar"/>{t("pipeline.sortNewest")}</button>
      <span className="toolbar-spacer"></span><span className="candidate-total">{t("pipeline.total", { count: total })}</span>
    </div>
    <p className="pipeline-hint"><Icon name="info" size={13}/>{t("candidate.ai.notice")}</p>
    {!canManage && <p className="pipeline-hint"><Icon name="lock" size={13}/>{t("pipeline.readOnly")}</p>}
    {moveStage.isError && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{moveStage.error.message}</div>}
    {openJobs.data?.totalElements === 0 && !params.get("jobId") && <div className="panel"><div className="table-empty"><Icon name="briefcase" size={24}/><strong>{t("pipeline.noJobsTitle")}</strong><span>{t("pipeline.noJobsText")}</span></div></div>}
    {board.isError && <div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong></div>}
    {jobId && <DndContext sensors={sensors} onDragStart={onDragStart} onDragEnd={onDragEnd} onDragCancel={() => setDragged(undefined)}>
      <div className="kanban">
        {columns.map(({ stage, cards }) => <KanbanColumn key={stage} stage={stage} count={cards.length} canDrop={!dragged || canMove(dragged.stage, stage)}>
          {cards.map((card) => <CandidateCard key={card.id} card={card} draggable={canManage && card.stage !== "HIRED" && card.stage !== "REJECTED"} onOpen={() => navigate(`/applications/${card.id}`)}/>)}
        </KanbanColumn>)}
      </div>
      <DragOverlay>{dragged && <CandidateCard card={dragged} draggable={false} overlay onOpen={() => undefined}/>}</DragOverlay>
    </DndContext>}
  </div>;
}
