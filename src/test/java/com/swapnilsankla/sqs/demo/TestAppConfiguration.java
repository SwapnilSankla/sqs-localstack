package com.swapnilsankla.sqs.demo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestAppConfiguration {
    @Value("${sqs.url}")
    private String sqsQueueUrl;

    private AmazonSQS sqsClient;

    @Bean
    @Primary
    AmazonSQS sqsClientWithLocalstack() {
        if (sqsClient == null) {
            AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("key", "secret")));
            builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsQueueUrl, "ap-south-1"));
            sqsClient = builder.build();
        }
        return sqsClient;
    }
}
