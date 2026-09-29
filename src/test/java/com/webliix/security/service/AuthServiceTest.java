package com.webliix.security.service;

import com.webliix.audit.service.AuditService;
import com.webliix.notifications.service.EmailService;
import com.webliix.security.dto.*;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import com.webliix.security.jwt.JwtService;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuditService auditService;

    @Mock
    private OtpService otpService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        Role employeeRole = Role.builder().id(2L).name("EMPLOYEE").build();
        sampleUser = User.builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .email("john@webliix.com")
                .password("encoded_pass")
                .enabled(true)
                .emailVerified(false)
                .roles(Set.of(employeeRole))
                .build();
    }

    @Test
    void forgotPassword_ShouldReturnGenericMessage_AndTriggerOtpIfUserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@webliix.com");
        when(userRepository.findByEmail("john@webliix.com")).thenReturn(Optional.of(sampleUser));
        when(otpService.generateAndStoreOtp(anyString(), anyString())).thenReturn("123456");

        String result = authService.forgotPassword(request);

        assertNotNull(result);
        assertTrue(result.contains("If an account with that email exists"));
        verify(rateLimiterService).checkRateLimit(eq("forgot-password:john@webliix.com"), eq(3), eq(3600L), eq("forgot password"));
        verify(otpService).generateAndStoreOtp(OtpService.PREFIX_PASSWORD_RESET, "john@webliix.com");
        verify(emailService).sendForgotPasswordOtpEmail(eq("john@webliix.com"), eq("John"), eq("123456"), eq(5));
    }

    @Test
    void verifyResetOtp_ShouldReturnResetToken_WhenOtpIsValid() {
        VerifyResetOtpRequest request = new VerifyResetOtpRequest("john@webliix.com", "123456");
        when(otpService.verifyOtp(OtpService.PREFIX_PASSWORD_RESET, "john@webliix.com", "123456")).thenReturn(true);
        when(otpService.createResetToken("john@webliix.com")).thenReturn("reset_token_xyz");

        VerifyResetOtpResponse response = authService.verifyResetOtp(request);

        assertNotNull(response);
        assertEquals("reset_token_xyz", response.getResetToken());
        assertEquals("john@webliix.com", response.getEmail());
    }

    @Test
    void resetPassword_ShouldUpdatePassword_WhenResetTokenIsValid() {
        ResetPasswordRequest request = new ResetPasswordRequest("john@webliix.com", "reset_token_xyz", "new_secure_password");
        when(otpService.verifyResetToken("john@webliix.com", "reset_token_xyz")).thenReturn(true);
        when(userRepository.findByEmail("john@webliix.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("new_secure_password")).thenReturn("encoded_new_pass");

        authService.resetPassword(request);

        verify(passwordEncoder).encode("new_secure_password");
        verify(userRepository).save(sampleUser);
        verify(otpService).clearResetToken("john@webliix.com");
        verify(emailService).sendPasswordResetSuccessEmail("john@webliix.com", "John");
    }

    @Test
    void verifyEmail_ShouldMarkEmailVerified_WhenOtpIsValid() {
        VerifyEmailRequest request = new VerifyEmailRequest("john@webliix.com", "654321");
        when(userRepository.findByEmail("john@webliix.com")).thenReturn(Optional.of(sampleUser));
        when(otpService.verifyOtp(OtpService.PREFIX_EMAIL_VERIFICATION, "john@webliix.com", "654321")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("john@webliix.com")).thenReturn(mock(org.springframework.security.core.userdetails.UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("jwt_token_abc");

        AuthResponse response = authService.verifyEmail(request);

        assertNotNull(response);
        assertEquals("jwt_token_abc", response.getAccessToken());
        assertTrue(sampleUser.getEmailVerified());
        verify(userRepository).save(sampleUser);
    }
}
