package com.webliix.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpLength", 6);
        ReflectionTestUtils.setField(otpService, "expirationSeconds", 300L);
        ReflectionTestUtils.setField(otpService, "resendCooldownSeconds", 60L);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 5);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateAndStoreOtp_ShouldReturnSixDigitNumericOtp() {
        String email = "test@webliix.com";
        when(redisTemplate.getExpire(anyString())).thenReturn(null);

        String otp = otpService.generateAndStoreOtp(OtpService.PREFIX_EMAIL_VERIFICATION, email);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));

        verify(valueOperations).set(eq(OtpService.PREFIX_EMAIL_VERIFICATION + email), anyString(), eq(Duration.ofSeconds(300)));
        verify(valueOperations).set(eq(OtpService.PREFIX_RESEND_COOLDOWN + email), eq("1"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void checkResendCooldown_ShouldThrowException_WhenCooldownActive() {
        String email = "cooldown@webliix.com";
        when(redisTemplate.getExpire(OtpService.PREFIX_RESEND_COOLDOWN + email)).thenReturn(45L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                otpService.checkResendCooldown(email)
        );

        assertTrue(ex.getMessage().contains("Please wait 45 seconds"));
    }

    @Test
    void verifyOtp_ShouldReturnTrue_WhenHashMatchesAndSingleUse() {
        String email = "verify@webliix.com";
        String rawOtp = "123456";

        // Generate expected SHA-256 hash of "123456"
        String expectedHash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

        when(valueOperations.get(OtpService.PREFIX_ATTEMPTS + email)).thenReturn(null);
        when(valueOperations.get(OtpService.PREFIX_EMAIL_VERIFICATION + email)).thenReturn(expectedHash);

        boolean result = otpService.verifyOtp(OtpService.PREFIX_EMAIL_VERIFICATION, email, rawOtp);

        assertTrue(result);
        verify(redisTemplate).delete(OtpService.PREFIX_EMAIL_VERIFICATION + email);
        verify(redisTemplate).delete(OtpService.PREFIX_ATTEMPTS + email);
    }

    @Test
    void verifyOtp_ShouldIncrementAttempts_WhenCodeIsIncorrect() {
        String email = "wrong@webliix.com";
        String wrongOtp = "654321";

        String expectedHash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

        when(valueOperations.get(OtpService.PREFIX_ATTEMPTS + email)).thenReturn(1);
        when(valueOperations.get(OtpService.PREFIX_EMAIL_VERIFICATION + email)).thenReturn(expectedHash);
        when(valueOperations.increment(OtpService.PREFIX_ATTEMPTS + email)).thenReturn(2L);

        boolean result = otpService.verifyOtp(OtpService.PREFIX_EMAIL_VERIFICATION, email, wrongOtp);

        assertFalse(result);
        verify(valueOperations).increment(OtpService.PREFIX_ATTEMPTS + email);
    }
}
