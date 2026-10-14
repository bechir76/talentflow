import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api/client";

export type PrivacyPolicyFacts = { version: string; rejectedRetentionMonths: number; autoDeleteEnabled: boolean };

export function usePrivacyPolicy() {
  return useQuery({
    queryKey: ["public", "privacy-policy"],
    queryFn: ({ signal }) => api.get<PrivacyPolicyFacts>("/public/privacy-policy", signal),
    staleTime: 5 * 60_000,
  });
}
