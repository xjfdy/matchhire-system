package com.matchhire.jobservice.grpc;

import com.matchhire.jobservice.model.Jobs;

import java.util.List;

public class JobGrpcMapper {

    public static JobResponse toGrpcResponse(Jobs job) {
        return JobResponse.newBuilder()
                .setId(job.getId().toString())
                .setEmployerId(job.getEmployerId().toString())
                .setTitle(job.getTitle())
                .addAllRequiredSkills(job.getRequiredSkills() != null ? job.getRequiredSkills() : List.of())
                .build();
    }
}