package com.bikangu.authentication.service;

import com.bikangu.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SmsService smsService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;

    public void sendOtp(String phoneNumber, AuthService.OtpPurpose purpose) {
        String otp = SecurityUtil.generateOtp(OTP_LENGTH);
        String key = buildKey(phoneNumber, purpose);
        String attemptsKey = key + ":attempts";

        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_TTL_MINUTES));
        redisTemplate.opsForValue().set(attemptsKey, "0", Duration.ofMinutes(OTP_TTL_MINUTES));

        smsService.sendOtp(phoneNumber, otp);
        log.info("OTP sent to {} for purpose {}", SecurityUtil.maskPhoneNumber(phoneNumber), purpose);
    }

    public boolean verifyOtp(String phoneNumber, String otp, AuthService.OtpPurpose purpose) {
        String key = buildKey(phoneNumber, purpose);
        String attemptsKey = key + ":attempts";

        String storedOtp = redisTemplate.opsForValue().get(key);
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);

        if (storedOtp == null) {
            log.warn("OTP expired or not found for {}", SecurityUtil.maskPhoneNumber(phoneNumber));
            return false;
        }

        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Max OTP attempts exceeded for {}", SecurityUtil.maskPhoneNumber(phoneNumber));
redisTemplate.delete(Set.of(key, attemptsKey));
            return false;
        }

        if (!storedOtp.equals(otp)) {
            redisTemplate.opsForValue().increment(attemptsKey);
            log.warn("Invalid OTP for {}", SecurityUtil.maskPhoneNumber(phoneNumber));
            return false;
        }

        redisTemplate.delete(Set.of(key, attemptsKey));
        log.info("OTP verified for {}", SecurityUtil.maskPhoneNumber(phoneNumber));
        return true;
    }

    public void invalidateOtp(String phoneNumber, AuthService.OtpPurpose purpose) {
        String key = buildKey(phoneNumber, purpose);
        String attemptsKey = key + ":attempts";
        redisTemplate.delete(Set.of(key, attemptsKey));
    }

    private String buildKey(String phoneNumber, AuthService.OtpPurpose purpose) {
        return "otp:" + purpose.name().toLowerCase() + ":" + phoneNumber;
    }
}