package org.michael.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderPageVO {

    private List<OrderListItemVO> list;

    private Long total;
}
