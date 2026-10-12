package com.talentflow.job.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.job.domain.Department;

interface SpringDataDepartmentRepository extends JpaRepository<Department, UUID> {
}
