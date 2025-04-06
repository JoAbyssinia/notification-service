package org.example.notificationservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * @author Yohannes k Yimam
 */

@Configuration
public class AwsConfiguration {

    private static final String LOCALSTACK_ENDPOINT =
            System.getenv().getOrDefault("AWS_ENDPOINT", "http://localhost:4566");
    private static final String ACCESS_KEY_ID =
            System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "000000000000");
    private static final String ACCESS_KEY_SECRET =
            System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "000000000000");
    private static final Region DEFAULT_REGION = Region.US_EAST_1;

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(DEFAULT_REGION)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY_ID, ACCESS_KEY_SECRET)))
                .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(DEFAULT_REGION)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY_ID, ACCESS_KEY_SECRET)))
                .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                .build();
    }

    @Bean
    public String notificationQueueUrl(SqsClient sqsClient) {
        return sqsClient.getQueueUrl(builder -> builder.queueName("email-notification-queue")).queueUrl();
    }
}
