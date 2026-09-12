package com.matchhire.jobservice.specification;

import com.matchhire.jobservice.model.JobType;
import com.matchhire.jobservice.model.Jobs;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecifications {

    public static Specification<Jobs> hasTitleContaining(String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Jobs> hasJobType(JobType jobType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("jobType"), jobType);
    }

    public static Specification<Jobs> hasRemote(Boolean remote) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("remote"), remote);
    }

    public static Specification<Jobs> hasMinSalaryGreaterThanOrEqual(int minSalary) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("minSalary"), minSalary);
    }

    public static Specification<Jobs> hasMaxSalaryLessThanOrEqual(int maxSalary) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("maxSalary"), maxSalary);
    }
}
