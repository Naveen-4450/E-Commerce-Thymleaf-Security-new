package com.example.models.dtoModels;

import lombok.Data;

import java.util.List;

@Data
public class OneOrderDto
{
    private int customerId;
    private Integer productId;
    private Integer quantity;

    private int custAddId;

    private String paymentMethod;

}

// private List<OrderItemsDto> orderItems;
// private PaymentDto payment;