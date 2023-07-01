package com.swapnilsankla.sqs.demo;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    @Bean
    public AmazonSQS sqs() {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard();
        builder.setRegion("ap-south-1");
        return builder.build();
    }
}
