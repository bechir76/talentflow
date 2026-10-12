package com.talentflow.job.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.talentflow.job.domain.Department;
import com.talentflow.job.domain.DepartmentRepository;

@Repository
class JpaDepartmentRepository implements DepartmentRepository {

    private final SpringDataDepartmentRepository jpa;

    JpaDepartmentRepository(SpringDataDepartmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Department> findAllOrderedByName() {
        return jpa.findAll(Sort.by("name"));
    }

    @Override
    public List<Department> findAllById(Collection<UUID> ids) {
        return jpa.findAllById(ids);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }
}
