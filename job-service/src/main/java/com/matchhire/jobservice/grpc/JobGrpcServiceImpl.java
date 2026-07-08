package com.matchhire.jobservice.grpc;

import com.matchhire.jobservice.model.Jobs;
import com.matchhire.jobservice.repository.JobRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@GrpcService
public class JobGrpcServiceImpl extends JobGrpcServiceGrpc.JobGrpcServiceImplBase {
    private final JobRepository jobRepository;

    public JobGrpcServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void getJobById(JobRequest request, StreamObserver<JobResponse> responseObserver) {
        try {
            UUID id = UUID.fromString(request.getId());
            Jobs job = jobRepository.findById(id)
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("Job not found with ID: " + id)
                            .asRuntimeException());

            responseObserver.onNext(JobGrpcMapper.toGrpcResponse(job));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid UUID format")
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getAllJobs(GetAllJobsRequest request, StreamObserver<GetAllJobsResponse> responseObserver) {
        List<JobResponse> jobResponses = jobRepository.findAll()
                .stream()
                .map(JobGrpcMapper::toGrpcResponse)
                .toList();

        GetAllJobsResponse response = GetAllJobsResponse.newBuilder()
                .addAllJobs(jobResponses)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
