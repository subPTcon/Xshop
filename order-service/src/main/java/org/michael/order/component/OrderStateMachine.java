package org.michael.order.component;

import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStateMachine {

    public void checkCancel(Integer currentStatus) {
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(currentStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }

    public void checkConfirm(Integer currentStatus) {
        if (!OrderStatus.SHIPPED.getCode().equals(currentStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }
}
