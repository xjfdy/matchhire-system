package com.matchhire.matchingservice.grpc;

import com.matchhire.jobservice.grpc.GetAllJobsRequest;
import com.matchhire.jobservice.grpc.JobGrpcServiceGrpc;
import com.matchhire.jobservice.grpc.JobResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobGrpcClient {

    @GrpcClient("job-service")
    private JobGrpcServiceGrpc.JobGrpcServiceBlockingStub jobGrpcStub;

    public List<JobResponse> getAllJobs() {
        GetAllJobsRequest request = GetAllJobsRequest.newBuilder().build();
        return jobGrpcStub.getAllJobs(request).getJobsList();
    }
}
