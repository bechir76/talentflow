package com.talentflow.job.application;

import com.talentflow.job.domain.Job;

/**
 * A job together with the name of its department, ready to be rendered.
 */
public record JobView(Job job, String departmentName) {
}
