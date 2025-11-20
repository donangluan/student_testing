package org.example.student_testing.test.controller;

import org.example.student_testing.test.dto.ProctorAlertDTO;
import org.example.student_testing.test.dto.WarningMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ProctorController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/report-violation")
    public void handleViolation(@Payload ProctorAlertDTO alert) {
        System.out.println("⚠ ALERT: " + alert.getStudentUsername() + " - " + alert.getViolationType());


        String destination = "/topic/test/" + alert.getTestId();

        messagingTemplate.convertAndSend(destination, alert);
    }

    @MessageMapping("/send-warning")
    public void sendWarning(WarningMessageDTO warning) {
        messagingTemplate.convertAndSend("/topic/test/" + warning.getTestId(),warning);

        System.out.println("[ALERT_GV] Đã gửi cảnh báo cho học sinh đến test: "+warning.getTestId()
                            +". Nội dung"+ warning.getMessage());
    }

}
