// Placeholder data until the pipeline endpoints exist.
export type PipelineCandidate = {
  id: string;
  name: string;
  initials: string;
  role: string;
  score: number;
  days: number;
  tags: string[];
};

export type PipelineColumn = { title: string; tone: string; people: PipelineCandidate[] };

export const pipeline: PipelineColumn[] = [
  { title: "Applied", tone: "gray", people: [
    { id: "david-klein", name: "David Klein", initials: "DK", role: "Java Developer at Zalando", score: 88, days: 2, tags: ["Java", "Spring Boot"] },
    { id: "sophia-mueller", name: "Sophia Müller", initials: "SM", role: "Backend Engineer at N26", score: 84, days: 3, tags: ["Java", "Docker"] },
    { id: "lucas-bernard", name: "Lucas Bernard", initials: "LB", role: "Software Engineer at Qonto", score: 79, days: 1, tags: ["Kotlin", "Java"] },
  ]},
  { title: "Screening", tone: "indigo", people: [
    { id: "amira-haddad", name: "Amira Haddad", initials: "AH", role: "Java Engineer at Celonis", score: 92, days: 4, tags: ["Java", "Spring Boot", "Docker"] },
    { id: "felix-wagner", name: "Felix Wagner", initials: "FW", role: "Senior Developer at SAP", score: 86, days: 5, tags: ["Java", "AWS"] },
  ]},
  { title: "Interview", tone: "amber", people: [
    { id: "lena-schmidt", name: "Lena Schmidt", initials: "LS", role: "Backend Lead at Delivery Hero", score: 94, days: 2, tags: ["Java", "Kubernetes"] },
    { id: "matteo-rossi", name: "Matteo Rossi", initials: "MR", role: "Platform Engineer at SumUp", score: 90, days: 3, tags: ["Spring Boot", "Docker"] },
  ]},
  { title: "Offer", tone: "purple", people: [
    { id: "jonas-weber", name: "Jonas Weber", initials: "JW", role: "Cloud Engineer at Flix", score: 89, days: 1, tags: ["Java", "AWS"] },
  ]},
  { title: "Hired", tone: "green", people: [
    { id: "elisa-fischer", name: "Elisa Fischer", initials: "EF", role: "Senior Java Engineer", score: 96, days: 0, tags: ["Java", "Spring Boot"] },
  ]},
  { title: "Rejected", tone: "red", people: [
    { id: "paul-meier", name: "Paul Meier", initials: "PM", role: "Software Developer", score: 61, days: 2, tags: ["Java"] },
  ]},
];
