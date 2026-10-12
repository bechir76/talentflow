package com.talentflow.job.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for departments.
 */
public interface DepartmentRepository {

    List<Department> findAllOrderedByName();

    List<Department> findAllById(Collection<UUID> ids);

    boolean existsById(UUID id);
}
