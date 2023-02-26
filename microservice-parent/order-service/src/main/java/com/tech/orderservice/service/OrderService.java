package com.tech.orderservice.service;

import com.tech.orderservice.dto.InventoryResponse;
import com.tech.orderservice.dto.OrderLineItemsDto;
import com.tech.orderservice.dto.OrderRequest;
import com.tech.orderservice.model.Order;
import com.tech.orderservice.model.OrderLineItems;
import com.tech.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;

    private final WebClient webClient;
    public void placeOder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        final List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
            .map(this::mapToDto).toList();

        order.setOrderLineItemsList(orderLineItems);

        final List<String> skus = orderLineItems.stream().map(OrderLineItems::getSkuCode).toList();

        // Check if inventory avl.
        final InventoryResponse[] inventoryResponses = webClient.get()
            .uri("http://localhost:8082/api/inventory",
                uriBuilder -> uriBuilder.queryParam("skuCode", skus).build())
            .retrieve()
            .bodyToMono(InventoryResponse[].class)
            .block();

        final boolean allProductsInStock = Arrays.stream(inventoryResponses != null ? inventoryResponses : new InventoryResponse[0]).allMatch(InventoryResponse::isInStock);
        if (allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock, Please try again later.");
        }

    }

    private OrderLineItems mapToDto(final OrderLineItemsDto order) {
        final OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(order.getPrice());
        orderLineItems.setQuantity(order.getQuantity());
        orderLineItems.setSkuCode(order.getSkuCode());
        return orderLineItems;
    }
}
