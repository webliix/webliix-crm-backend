package com.webliix.portal.service;

import com.webliix.portal.dto.PortalAuthRequest;
import com.webliix.portal.dto.PortalAuthResponse;

public interface PortalAuthService {
    PortalAuthResponse login(PortalAuthRequest request);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
