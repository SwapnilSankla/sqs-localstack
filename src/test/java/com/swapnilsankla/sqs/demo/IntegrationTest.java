package com.swapnilsankla.sqs.demo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.net.MalformedURLException;
import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Import(TestAppConfiguration.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String architecture = System.getenv("ARCHITECTURE");

    private final static DockerImageName localstackImage = architecture != null && architecture.equals("arm64") ?
            DockerImageName.parse("localstack/localstack:latest-arm64") :
            DockerImageName.parse("localstack/localstack:latest");

    @Container
    private final static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withServices(LocalStackContainer.Service.SQS)
            .withExposedPorts(4566)
            .withStartupTimeout(Duration.ofSeconds(180))
            .withEnv("AWS_ACCESS_KEY_ID", "key")
            .withEnv("AWS_SECRET_ACCESS_KEY", "secret")
            .withReuse(true);

    @BeforeAll
    public static void setup() throws MalformedURLException {
        localstack.start();
        String sqsQueueUrl = localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toURL().toString();
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("key", "secret")));
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsQueueUrl, "ap-south-1"));
        AmazonSQS sqsClient = builder.build();
        if (sqsClient != null && sqsClient.listQueues().getQueueUrls().isEmpty()) {
            String queueUrl = sqsClient.createQueue("test").getQueueUrl();
            System.setProperty("QueueUrl", queueUrl);
        }
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("sqs.url", () -> System.getProperty("QueueUrl"));
    }

    @Test
    void sendAndReceiveMessagesOnAmazonSqs() throws Exception {
        mockMvc.perform(post("/messages"));

        String response = mockMvc.perform(get("/messages")).andReturn().getResponse().getContentAsString();
        String[] messages = objectMapper.readValue(response, String[].class);

        Assertions.assertEquals("Hello world!", messages[0]);
    }
}
