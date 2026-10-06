package org.michael.payment.service;

import org.michael.payment.dto.PaymentCallbackDTO;
import org.michael.payment.dto.PaymentCreateDTO;
import org.michael.payment.vo.PaymentCreateVO;

public interface PaymentService {

    PaymentCreateVO createPayment(Long userId, PaymentCreateDTO dto);

    Boolean handleCallback(PaymentCallbackDTO dto);
}
