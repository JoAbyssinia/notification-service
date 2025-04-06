package org.example.notificationservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.notificationservice.service.ReceiveSendNotificationsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * @author Yohannes k Yimam
 */
@RestController
public class NotificationController {


    ReceiveSendNotificationsService receiveSendNotificationsService;
    NotificationController(ReceiveSendNotificationsService receiveSendNotificationsService) {
        this.receiveSendNotificationsService = receiveSendNotificationsService;
    }

    @GetMapping("/process")
    public ResponseEntity<List<String>> processNotifications() {
        return new ResponseEntity<>(receiveSendNotificationsService.processNotifications(), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<HashMap<String, String>>> listMessages() {
        return new ResponseEntity<>(receiveSendNotificationsService.listMessages(), HttpStatus.OK);
    }

    @GetMapping("/purge")
    public ResponseEntity<Void> purgeQueue() {
        receiveSendNotificationsService.purgeQueue();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

}
