import { currentLanguage } from "@/lib/i18n";

const API_BASE = "/api/v1";

/** RFC 9457 Problem Details body returned by the backend for every error. */
export type ProblemDetail = {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: { field: string; message: string }[];
};

export class ApiError extends Error {
  readonly status: number;
  readonly problem?: ProblemDetail;

  constructor(status: number, problem?: ProblemDetail) {
    super(problem?.detail ?? problem?.title ?? `Request failed with status ${status}`);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
  }
}

type TokenProvider = () => Promise<string | undefined>;

let tokenProvider: TokenProvider = async () => undefined;

/** Called once by the auth layer so every request carries a fresh access token. */
export function setTokenProvider(provider: TokenProvider) {
  tokenProvider = provider;
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  signal?: AbortSignal;
};

async function authHeaders(accept: string): Promise<Headers> {
  const headers = new Headers({ Accept: accept, "Accept-Language": currentLanguage() });
  const token = await tokenProvider();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  return headers;
}

async function toApiError(response: Response): Promise<ApiError> {
  const problem = (await response.json().catch(() => undefined)) as ProblemDetail | undefined;
  return new ApiError(response.status, problem);
}

async function request<T>(path: string, { method = "GET", body, signal }: RequestOptions = {}): Promise<T> {
  const headers = await authHeaders("application/json");

  let payload: BodyInit | undefined;
  if (body instanceof FormData) {
    payload = body;
  } else if (body !== undefined) {
    headers.set("Content-Type", "application/json");
    payload = JSON.stringify(body);
  }

  const response = await fetch(API_BASE + path, { method, headers, body: payload, signal });
  if (!response.ok) throw await toApiError(response);
  if (response.status === 204 || response.status === 202) return undefined as T;
  return (await response.json()) as T;
}

export const api = {
  get: <T>(path: string, signal?: AbortSignal) => request<T>(path, { signal }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: "POST", body }),
  put: <T>(path: string, body?: unknown) => request<T>(path, { method: "PUT", body }),
  patch: <T>(path: string, body?: unknown) => request<T>(path, { method: "PATCH", body }),
  delete: <T = void>(path: string) => request<T>(path, { method: "DELETE" }),
  /** Downloads a binary resource such as a CV. */
  blob: async (path: string, signal?: AbortSignal): Promise<Blob> => {
    const response = await fetch(API_BASE + path, { headers: await authHeaders("*/*"), signal });
    if (!response.ok) throw await toApiError(response);
    return response.blob();
  },
};
