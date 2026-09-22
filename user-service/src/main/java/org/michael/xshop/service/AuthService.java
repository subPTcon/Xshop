package org.michael.xshop.service;

import org.michael.xshop.dto.LoginRequest;
import org.michael.xshop.dto.LoginResponse;
import org.michael.xshop.dto.RegisterRequest;

public interface AuthService {

    Long register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
