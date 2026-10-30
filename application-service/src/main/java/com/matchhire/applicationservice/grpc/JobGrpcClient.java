package com.matchhire.applicationservice.grpc;

import com.matchhire.jobservice.grpc.JobGrpcServiceGrpc;
import com.matchhire.jobservice.grpc.JobRequest;
import com.matchhire.jobservice.grpc.JobResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class JobGrpcClient {

    @GrpcClient("job-service")
    private JobGrpcServiceGrpc.JobGrpcServiceBlockingStub jobGrpcStub;

    public JobResponse getJobById(String jobId) {
        JobRequest request = JobRequest.newBuilder()
                .setId(jobId)
                .build();
        return jobGrpcStub.getJobById(request);
    }
}
