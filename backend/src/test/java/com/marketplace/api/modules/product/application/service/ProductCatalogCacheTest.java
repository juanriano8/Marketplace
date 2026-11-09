package com.marketplace.api.modules.product.application.service;

import com.marketplace.api.config.CacheConfig;
import com.marketplace.api.modules.inventory.domain.port.inbound.InventoryUseCase;
import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.application.dto.UpdateProductRequest;
import com.marketplace.api.modules.product.application.mapper.ProductMapper;
import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.cache.CacheNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica que el catalogo publico se sirve desde Caffeine y que una escritura de producto
 * invalida tanto el listado como el detalle.
 */
@SpringJUnitConfig(ProductCatalogCacheTest.TestConfig.class)
class ProductCatalogCacheTest {

    @Configuration
    @Import(CacheConfig.class)
    static class TestConfig {

        @Bean
        ProductRepositoryPort productRepositoryPort() {
            return mock(ProductRepositoryPort.class);
        }

        @Bean
        ProductMapper productMapper() {
            return mock(ProductMapper.class);
        }

        @Bean
        InventoryUseCase inventoryUseCase() {
            return mock(InventoryUseCase.class);
        }

        @Bean
        StockQueryPort stockQueryPort() {
            return mock(StockQueryPort.class);
        }

        @Bean
        UserRepositoryPort userRepositoryPort() {
            return mock(UserRepositoryPort.class);
        }

        @Bean
        ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }

        @Bean
        ProductService productService(ProductRepositoryPort productRepositoryPort,
                                      ProductMapper productMapper,
                                      InventoryUseCase inventoryUseCase,
                                      StockQueryPort stockQueryPort,
                                      UserRepositoryPort userRepositoryPort,
                                      ApplicationEventPublisher applicationEventPublisher) {
            return new ProductService(
                productRepositoryPort, productMapper, inventoryUseCase, stockQueryPort,
                userRepositoryPort, applicationEventPublisher
            );
        }
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepositoryPort productRepositoryPort;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockQueryPort stockQueryPort;

    @Autowired
    private CacheManager cacheManager;

    private UUID productId;
    private UUID sellerId;
    private Product product;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        reset(productRepositoryPort, productMapper, stockQueryPort);
        cacheManager.getCache(CacheNames.PRODUCT_CATALOG).clear();
        cacheManager.getCache(CacheNames.PRODUCT_DETAIL).clear();

        productId = UUID.randomUUID();
        sellerId = UUID.randomUUID();
        product = new Product(
            "Cafe de origen", "Grano tostado", new BigDecimal("12.50"), "USD", 8, sellerId, "Comida"
        );
        product.setId(productId);
        product.approve();

        response = new ProductResponse(
            productId,
            product.getName(),
            product.getDescription(),
            product.getSlug(),
            product.getPrice(),
            product.getCurrencyCode(),
            product.getStockQuantity(),
            null,
            product.getStatus(),
            sellerId,
            product.getCategory(),
            null,
            null
        );

        when(productMapper.toResponse(any(Product.class))).thenReturn(response);
        when(stockQueryPort.findAvailability(any())).thenReturn(Optional.empty());
        when(productRepositoryPort.findAllActive(any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(List.of(product), invocation.getArgument(0), 1));
        when(productRepositoryPort.findBySlug(product.getSlug())).thenReturn(Optional.of(product));
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(product));
        when(productRepositoryPort.save(product)).thenReturn(product);
    }

    @Test
    void listadoActivoSeReutilizaHastaQueSeEditaUnProducto() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<ProductResponse> first = productService.listActiveProducts(pageable);
        Page<ProductResponse> second = productService.listActiveProducts(pageable);

        assertThat(second).isSameAs(first);
        verify(productRepositoryPort, times(1)).findAllActive(pageable);

        productService.updateProduct(
            productId,
            new UpdateProductRequest("Cafe nuevo", null, null, null, null, null),
            sellerId
        );

        productService.listActiveProducts(pageable);
        verify(productRepositoryPort, times(2)).findAllActive(pageable);
    }

    @Test
    void detallePorSlugSeReutilizaYSeInvalidaAlEditar() {
        ProductResponse first = productService.getProductBySlug(product.getSlug());
        ProductResponse second = productService.getProductBySlug(product.getSlug());

        assertThat(second).isSameAs(first);
        verify(productRepositoryPort, times(1)).findBySlug(product.getSlug());

        productService.updateProduct(
            productId,
            new UpdateProductRequest("Cafe nuevo", null, null, null, null, null),
            sellerId
        );

        productService.getProductBySlug(product.getSlug());
        verify(productRepositoryPort, times(2)).findBySlug(product.getSlug());
    }
}
