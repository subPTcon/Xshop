package org.michael.xshop.service;

import org.michael.xshop.dto.RegisterRequest;

public interface AuthService {

    Long register(RegisterRequest request);
}
