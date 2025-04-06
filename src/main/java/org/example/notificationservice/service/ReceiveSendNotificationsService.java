package org.example.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.example.notificationservice.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.*;
import java.util.function.Consumer;


/**
 * @author Yohannes k Yimam
 */
@Service
public class ReceiveSendNotificationsService {
   private static final Logger logger = LoggerFactory.getLogger(ReceiveSendNotificationsService.class);

   private static final String SOURCE_EMAIL = "no-reply@localstack.cloud";

    private final SqsClient sqsClient;
    private final SesClient sesClient;
    private final String notificationQueueUrl;

    public ReceiveSendNotificationsService(SqsClient sqsClient, SesClient sesClient, String notificationQueueUrl) {
        this.sqsClient = sqsClient;
        this.sesClient = sesClient;
        this.notificationQueueUrl = notificationQueueUrl;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> processNotifications(){
        ReceiveMessageResponse receiveMessageResponse = sqsClient
                .receiveMessage(request -> request
                        .queueUrl(notificationQueueUrl)
                        .maxNumberOfMessages(10));

        if (!receiveMessageResponse.hasMessages()){
            return Collections.emptyList();
        }

        // transform notifications
        List<Message> messages = receiveMessageResponse.messages();
        List<Notification> notificationsToSend = new ArrayList<>(messages.size());
        List<String> notificationRecipients = new ArrayList<>(messages.size());

        for (Message message : messages) {
            String messageBody = message.body();

            try {
                HashMap snsEvent = objectMapper.readValue(messageBody, HashMap.class);
                logger.info("processing notification: {}", snsEvent);

                String notificationString = snsEvent.get("Message").toString();
                Notification notification = objectMapper.readValue(notificationString, Notification.class);
                notificationsToSend.add(notification);
                notificationRecipients.add(message.receiptHandle());
            } catch (Exception e) {
                logger.error("error processing message body {}", messageBody, e);
            }
        }

        // send notifications transactional

        List<String> sentMessages = new ArrayList<>();
        for (int i = 0; i < notificationRecipients.size(); i++) {
            Notification notification = notificationsToSend.get(i);
            String recipientHandler = notificationRecipients.get(i);

            try {
                String messageId = sendNotificationAsEmail(notification);
                sentMessages.add(messageId);
            } catch (Exception e) {
                logger.error("could not send notification as email {}", notification, e);
            }

            // delete
            sqsClient.deleteMessage(builder ->
                    builder.queueUrl(notificationQueueUrl).receiptHandle(recipientHandler));
        }

        return sentMessages;

    }

    public String sendNotificationAsEmail(Notification notification) {
        return sesClient.sendEmail(notificationToEmail(notification)).messageId();
    }

    private SendEmailRequest notificationToEmail(Notification notification) {

        return SendEmailRequest.builder()
                .applyMutation(email -> email.message(
                                    msg -> msg.body(
                                            body -> body.text(
                                                    text -> text.data(notification.getBody()))).subject(
                                                            subject -> subject.data(notification.getSubject()))).destination(
                                                                    destination -> destination.toAddresses(notification.getAddress()))
                        .source(SOURCE_EMAIL))
                .build();
    }

    public List<HashMap<String, String>> listMessages() {
        ReceiveMessageRequest receiveMessage =  ReceiveMessageRequest.builder()
                .queueUrl(notificationQueueUrl)
                .maxNumberOfMessages(10)
                .visibilityTimeout(0)
                .build();

        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessage);

        if (!receiveMessageResponse.hasMessages()) {
            return Collections.emptyList();
        }

        return receiveMessageResponse.messages().stream().map(Message::body).map(str ->{
            try {
                return (HashMap<String, String>) objectMapper.readValue(str, HashMap.class);
            } catch (Exception e) {
                logger.error("error processing message body {}", str, e);
                HashMap<String, String> map = new HashMap<>();
                map.put("body", str);
                return map;
            }
        }).toList();
    }

    public void purgeQueue() {
        sqsClient.purgeQueue(builder -> builder.queueUrl(notificationQueueUrl));
    }
}















