package com.webliix.security.service;

public interface OtpService {

    /**
     * Key prefixes for OTP scopes
     */
    String PREFIX_EMAIL_VERIFICATION = "webliix:otp:email-verification:";
    String PREFIX_PASSWORD_RESET = "webliix:otp:password-reset:";
    String PREFIX_RESEND_COOLDOWN = "webliix:otp:resend:";
    String PREFIX_ATTEMPTS = "webliix:otp:attempts:";
    String PREFIX_RESET_TOKEN = "webliix:otp:reset-token:";

    /**
     * Check if user is in resend cooldown. Throws exception if active.
     */
    void checkResendCooldown(String identifier);

    /**
     * Generate 6-digit OTP, store in Redis with TTL and set resend cooldown.
     */
    String generateAndStoreOtp(String keyPrefix, String identifier);

    /**
     * Verify OTP against Redis. Returns true if valid, false if invalid or expired.
     * Deletes OTP on success. Increments attempts counter on failure.
     */
    boolean verifyOtp(String keyPrefix, String identifier, String candidateOtp);

    /**
     * Store temporary single-use password reset token after OTP verification.
     */
    String createResetToken(String identifier);

    /**
     * Verify single-use reset token.
     */
    boolean verifyResetToken(String identifier, String token);

    /**
     * Clear reset token after successful password reset.
     */
    void clearResetToken(String identifier);
}
