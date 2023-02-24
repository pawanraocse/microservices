package com.tech.orderservice.service;

import com.tech.orderservice.dto.OrderLineItemsDto;
import com.tech.orderservice.dto.OrderRequest;
import com.tech.orderservice.model.Order;
import com.tech.orderservice.model.OrderLineItems;
import com.tech.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    public void placeOder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        final List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
            .map(this::mapToDto).toList();

        order.setOrderLineItemsList(orderLineItems);
        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(final OrderLineItemsDto order) {
        final OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(order.getPrice());
        orderLineItems.setQuantity(order.getQuantity());
        orderLineItems.setSkuCode(order.getSkuCode());
        return orderLineItems;
    }
}
