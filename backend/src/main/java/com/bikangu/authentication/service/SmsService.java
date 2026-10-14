package com.bikangu.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${app.sms.provider:mock}")
    private String provider;

    @Value("${app.sms.api-key:}")
    private String apiKey;

    @Value("${app.sms.sender-id:Bikangu}")
    private String senderId;

    public void sendOtp(String phoneNumber, String otp) {
        String message = String.format("Your Bikangu verification code is: %s. Valid for 10 minutes. Do not share.", otp);
        sendSms(phoneNumber, message);
    }

    public void sendSms(String phoneNumber, String message) {
        try {
            switch (provider) {
                case "twilio" -> sendViaTwilio(phoneNumber, message);
                case "africastalking" -> sendViaAfricaTalking(phoneNumber, message);
                case "mock" -> log.info("MOCK SMS to {}: {}", phoneNumber, message);
                default -> log.warn("Unknown SMS provider: {}", provider);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    private void sendViaTwilio(String phoneNumber, String message) {
        // Implement Twilio integration
        log.info("Would send via Twilio to {}: {}", phoneNumber, message);
    }

    private void sendViaAfricaTalking(String phoneNumber, String message) {
        // Implement Africa's Talking integration
        log.info("Would send via Africa's Talking to {}: {}", phoneNumber, message);
    }
}