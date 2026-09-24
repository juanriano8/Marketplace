package com.marketplace.api.modules.order.application.mapper;

import com.marketplace.api.modules.order.application.dto.CartResponse;
import com.marketplace.api.modules.order.application.dto.OrderResponse;
import com.marketplace.api.modules.order.domain.model.Cart;
import com.marketplace.api.modules.order.domain.model.CartItem;
import com.marketplace.api.modules.order.domain.model.Order;
import com.marketplace.api.modules.order.domain.model.OrderItem;
import com.marketplace.api.modules.order.domain.model.SubOrder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hand-written mapper for the order module.
 *
 * <p>MapStruct would need {@code default} methods for the computed totals anyway, so the mapping is
 * expressed explicitly here; it also documents exactly which fields are exposed to clients.</p>
 */
@Component
public class OrderMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getItems().stream()
            .map(this::toItemResponse)
            .toList();

        String currencyCode = cart.getItems().stream()
            .findFirst()
            .map(CartItem::getCurrencyCode)
            .orElse("USD");

        return new CartResponse(
            cart.getId(),
            cart.getBuyerId(),
            cart.getStatus(),
            items,
            cart.getTotalItemCount(),
            cart.getSubtotal(),
            currencyCode,
            cart.getUpdatedAt()
        );
    }

    private CartResponse.CartItemResponse toItemResponse(CartItem item) {
        return new CartResponse.CartItemResponse(
            item.getId(),
            item.getProductId(),
            item.getSellerId(),
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.lineTotal(),
            item.getCurrencyCode()
        );
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getBuyerId(),
            order.getTotalAmount(),
            order.getCurrencyCode(),
            order.getStatus(),
            order.getPaymentReference(),
            order.getPaidAt(),
            order.getCreatedAt(),
            order.getSubOrders().stream().map(this::toResponse).toList()
        );
    }

    public OrderResponse.SubOrderResponse toResponse(SubOrder subOrder) {
        return new OrderResponse.SubOrderResponse(
            subOrder.getId(),
            subOrder.getSellerId(),
            subOrder.getSubtotal(),
            subOrder.getCurrencyCode(),
            subOrder.getStatus(),
            subOrder.getTrackingNumber(),
            subOrder.getCarrier(),
            subOrder.getShippedAt(),
            subOrder.getDeliveredAt(),
            subOrder.getItems().stream().map(this::toResponse).toList()
        );
    }

    public OrderResponse.OrderItemResponse toResponse(OrderItem item) {
        return new OrderResponse.OrderItemResponse(
            item.getId(),
            item.getProductId(),
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.lineTotal(),
            item.getCurrencyCode()
        );
    }
}
