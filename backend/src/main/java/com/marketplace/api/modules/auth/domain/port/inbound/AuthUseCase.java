package com.marketplace.api.modules.auth.domain.port.inbound;

import com.marketplace.api.modules.auth.application.dto.AuthResponse;
import com.marketplace.api.modules.auth.application.dto.LoginRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterBuyerRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterSellerRequest;

public interface AuthUseCase {
    AuthResponse registerBuyer(RegisterBuyerRequest request);
    AuthResponse registerSeller(RegisterSellerRequest request);
    AuthResponse login(LoginRequest request);
}
