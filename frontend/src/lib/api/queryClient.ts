import { QueryClient } from "@tanstack/react-query";
import { ApiError } from "./client";

const MAX_RETRIES = 2;

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // Client errors (4xx) will not succeed on retry; only retry network and server errors.
      retry: (failureCount, error) =>
        !(error instanceof ApiError && error.status < 500) && failureCount < MAX_RETRIES,
    },
  },
});
