package org.example.notificationservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * @author Yohannes k Yimam
 */

@Configuration
public class AwsConfiguration {

    @Value("${aws.endpoint}")
    private static String AWS_ENDPOINT;
    private static final Region DEFUALT_REGION = Region.US_EAST_1;

    @Bean
    public SqsClient sqsClient() {
     return SqsClient.builder()
             .region(DEFUALT_REGION)
             .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
             .applyMutation(sqsClientBuilder -> {
                 sqsClientBuilder.endpointOverride(URI.create(AWS_ENDPOINT));
             })
             .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(DEFUALT_REGION)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .applyMutation(sqsClientBuilder -> {
                    sqsClientBuilder.endpointOverride(URI.create(AWS_ENDPOINT));
                })
                .build();
    }

    @Bean
    @Autowired
    public String notificationQueueUrl(SqsClient sqsClient) {
        return sqsClient.getQueueUrl(builder -> builder.queueName("email-notifications")).queueUrl();
    }

}
