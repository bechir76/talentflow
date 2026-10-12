import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import type { Role } from "@/lib/auth/roles";

export type Me = {
  id: string;
  email: string | null;
  fullName: string | null;
  roles: Role[];
};

export function useMe() {
  return useQuery({
    queryKey: ["me"],
    queryFn: ({ signal }) => api.get<Me>("/me", signal),
  });
}
