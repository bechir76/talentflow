import { useMutation, useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import { saveBlob } from "@/lib/download";

export type ConsentRecord = { type: string; policyVersion: string; givenAt: string; withdrawnAt: string | null };

export function useConsents() {
  return useQuery({
    queryKey: ["my-consents"],
    queryFn: ({ signal }) => api.get<ConsentRecord[]>("/me/consents", signal),
  });
}

/** Downloads the GDPR Art. 15 export as a JSON file. */
export function useDataExport() {
  return useMutation({
    mutationFn: async () => saveBlob(await api.blob("/me/data-export"), "talentflow-data-export.json"),
  });
}

export function useDeleteMyData() {
  return useMutation({ mutationFn: () => api.delete("/me") });
}
