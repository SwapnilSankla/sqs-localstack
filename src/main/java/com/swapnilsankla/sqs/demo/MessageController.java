package com.swapnilsankla.sqs.demo;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("messages")
public class MessageController {

    @Autowired
    private AmazonSQS sqs;

    @Value("${sqs.url}")
    private String sqsQueueUrl;

    @GetMapping
    public List<String> get() {
        return sqs.receiveMessage(sqsQueueUrl).getMessages().stream().map(Message::getBody).collect(Collectors.toList());
    }

    @PostMapping
    public void send() {
        sqs.sendMessage(sqsQueueUrl, "Hello world!");
    }
}