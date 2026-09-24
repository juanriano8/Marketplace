package com.marketplace.api.modules.order.application.service;

import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import com.marketplace.api.modules.order.application.dto.CartResponse;
import com.marketplace.api.modules.order.application.mapper.OrderMapper;
import com.marketplace.api.modules.order.domain.model.Cart;
import com.marketplace.api.modules.order.domain.port.inbound.CartUseCase;
import com.marketplace.api.modules.order.domain.port.outbound.CartRepositoryPort;
import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService implements CartUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final StockQueryPort stockQueryPort;
    private final OrderMapper orderMapper;

    /**
     * Returns the buyer's active cart. Deliberately not {@code readOnly}: reading a cart that does
     * not exist yet creates an empty one, so the transaction must be able to flush.
     */
    @Override
    @Transactional
    public CartResponse getActiveCart(UUID buyerId) {
        Cart cart = cartRepositoryPort.findActiveByBuyerId(buyerId)
            .orElseGet(() -> cartRepositoryPort.save(new Cart(buyerId)));
        return orderMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID buyerId, UUID productId, int quantity) {
        Product product = requirePurchasableProduct(productId);

        // Fail fast with a clear message instead of accepting a cart the buyer can never check out.
        if (!stockQueryPort.hasSellableStock(productId, quantity)) {
            throw new DomainException("Insufficient stock available for product " + productId);
        }

        Cart cart = cartRepositoryPort.findActiveByBuyerId(buyerId)
            .orElseGet(() -> new Cart(buyerId));

        cart.addItem(
            product.getId(),
            product.getSellerId(),
            product.getName(),
            quantity,
            product.getPrice(),
            product.getCurrencyCode()
        );

        Cart saved = cartRepositoryPort.save(cart);
        log.info("Buyer {} added {} unit(s) of product {} to cart {}", buyerId, quantity, productId, saved.getId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(UUID buyerId, UUID productId, int quantity) {
        Cart cart = requireActiveCart(buyerId);

        if (quantity > 0 && !stockQueryPort.hasSellableStock(productId, quantity)) {
            throw new DomainException("Insufficient stock available for product " + productId);
        }

        cart.updateItemQuantity(productId, quantity);
        return orderMapper.toResponse(cartRepositoryPort.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID buyerId, UUID productId) {
        Cart cart = requireActiveCart(buyerId);
        cart.removeItem(productId);
        return orderMapper.toResponse(cartRepositoryPort.save(cart));
    }

    @Override
    @Transactional
    public CartResponse clearCart(UUID buyerId) {
        Cart cart = requireActiveCart(buyerId);
        cart.clear();
        return orderMapper.toResponse(cartRepositoryPort.save(cart));
    }

    private Cart requireActiveCart(UUID buyerId) {
        return cartRepositoryPort.findActiveByBuyerId(buyerId)
            .orElseThrow(() -> new ResourceNotFoundException("No active cart found for the authenticated buyer"));
    }

    private Product requirePurchasableProduct(UUID productId) {
        Product product = productRepositoryPort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new DomainException("Product is not available for purchase (status " + product.getStatus() + ")");
        }
        if (product.getSellerId() == null) {
            throw new DomainException("Product has no assigned seller and cannot be purchased");
        }
        return product;
    }
}
