package com.marketplace.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.api.modules.admin.application.service.AdminService;
import com.marketplace.api.modules.admin.infrastructure.adapter.inbound.rest.AdminSellerController;
import com.marketplace.api.modules.inventory.domain.port.inbound.InventoryUseCase;
import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import com.marketplace.api.modules.inventory.infrastructure.adapter.inbound.rest.AdminInventoryController;
import com.marketplace.api.modules.inventory.infrastructure.adapter.inbound.rest.InventoryController;
import com.marketplace.api.modules.inventory.infrastructure.adapter.inbound.rest.SellerInventoryController;
import com.marketplace.api.modules.order.domain.port.inbound.CartUseCase;
import com.marketplace.api.modules.order.domain.port.inbound.CheckoutUseCase;
import com.marketplace.api.modules.order.domain.port.inbound.OrderQueryUseCase;
import com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest.AdminOrderController;
import com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest.BuyerOrderController;
import com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest.CartController;
import com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest.SellerOrderController;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
import com.marketplace.api.modules.product.infrastructure.adapter.inbound.rest.AdminProductController;
import com.marketplace.api.modules.product.infrastructure.adapter.inbound.rest.ProductController;
import com.marketplace.api.modules.product.infrastructure.adapter.inbound.rest.SellerProductController;
import com.marketplace.api.modules.review.domain.port.inbound.ReviewUseCase;
import com.marketplace.api.modules.review.infrastructure.adapter.inbound.rest.AdminReviewController;
import com.marketplace.api.modules.review.infrastructure.adapter.inbound.rest.BuyerReviewController;
import com.marketplace.api.modules.review.infrastructure.adapter.inbound.rest.ProductReviewController;
import com.marketplace.api.shared.exception.GlobalExceptionHandler;
import com.marketplace.api.shared.security.CustomUserDetailsService;
import com.marketplace.api.shared.security.JwtAccessDeniedHandler;
import com.marketplace.api.shared.security.JwtAuthenticationEntryPoint;
import com.marketplace.api.shared.security.JwtAuthenticationFilter;
import com.marketplace.api.shared.security.JwtTokenProvider;
import com.marketplace.api.shared.security.UserRole;
import com.marketplace.api.shared.security.WithMockUserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RBAC contract for every endpoint in the marketplace.
 *
 * <p>The real {@link SecurityConfig} is imported, so URL rules and {@code @PreAuthorize}
 * expressions are exercised rather than re-implemented. Each case asserts the status the API must
 * return for anonymous, wrong-role and correct-role callers.</p>
 */
@WebMvcTest(controllers = {
    ProductController.class,
    SellerProductController.class,
    AdminProductController.class,
    InventoryController.class,
    SellerInventoryController.class,
    AdminInventoryController.class,
    CartController.class,
    BuyerOrderController.class,
    SellerOrderController.class,
    AdminOrderController.class,
    BuyerReviewController.class,
    ProductReviewController.class,
    AdminReviewController.class,
    AdminSellerController.class
})
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    JwtAuthenticationEntryPoint.class,
    JwtAccessDeniedHandler.class,
    GlobalExceptionHandler.class
})
@AutoConfigureMockMvc
class RbacSecurityTest {

    private static final String SELLER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String PRODUCT_ID = "22222222-2222-2222-2222-222222222222";
    private static final String SUB_ORDER_ID = "33333333-3333-3333-3333-333333333333";
    private static final String REVIEW_ID = "44444444-4444-4444-4444-444444444444";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductUseCase productUseCase;

    @MockBean
    private InventoryUseCase inventoryUseCase;

    @MockBean
    private StockQueryPort stockQueryPort;

    @MockBean
    private CartUseCase cartUseCase;

    @MockBean
    private CheckoutUseCase checkoutUseCase;

    @MockBean
    private OrderQueryUseCase orderQueryUseCase;

    @MockBean
    private ReviewUseCase reviewUseCase;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // ------------------------------------------------------------------
    // Public endpoints
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Public catalog endpoints are reachable anonymously")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/v1/products is public")
        void listProductsIsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/products/{slug} is public")
        void productDetailIsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/products/wireless-headphones-abc12345"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/inventory/check/{productId} is public")
        void inventoryCheckIsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/inventory/check/" + PRODUCT_ID))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/reviews/product/{productId} is public")
        void productReviewsArePublic() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/product/" + PRODUCT_ID))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/v1/products stays protected while GET stays public")
        void catalogWriteIsProtected() throws Exception {
            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------
    // Anonymous access to protected endpoints
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Protected endpoints reject anonymous callers with 401")
    class AnonymousIsRejected {

        @ParameterizedTest(name = "{0} requires authentication")
        @ValueSource(strings = {
            "/api/v1/cart",
            "/api/v1/buyer/orders",
            "/api/v1/seller/products",
            "/api/v1/seller/orders",
            "/api/v1/seller/inventory",
            "/api/v1/admin/products",
            "/api/v1/admin/orders",
            "/api/v1/admin/inventory/audit",
            "/api/v1/admin/reviews"
        })
        void getEndpointsRequireAuthentication(String path) throws Exception {
            mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/orders/checkout requires authentication")
        void checkoutRequiresAuthentication() throws Exception {
            mockMvc.perform(post("/api/v1/orders/checkout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/cart/items requires authentication")
        void addCartItemRequiresAuthentication() throws Exception {
            mockMvc.perform(post("/api/v1/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productId\":\"" + PRODUCT_ID + "\",\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/v1/admin/sellers/{id}/verify requires authentication")
        void verifySellerRequiresAuthentication() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/sellers/" + SELLER_ID + "/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"approved\":true}"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/v1/admin/reviews/{id} requires authentication")
        void deleteReviewRequiresAuthentication() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/reviews/" + REVIEW_ID))
                .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------
    // Horizontal / vertical privilege escalation
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Wrong role is rejected with 403")
    class WrongRoleIsForbidden {

        @Test
        @DisplayName("BUYER cannot list the seller catalog")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void buyerCannotAccessSellerProducts() throws Exception {
            mockMvc.perform(get("/api/v1/seller/products"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SELLER cannot moderate products as admin")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCannotModerateProducts() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/products/" + PRODUCT_ID + "/approval")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"approved\":true}"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SELLER cannot read the global inventory audit")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCannotReadInventoryAudit() throws Exception {
            mockMvc.perform(get("/api/v1/admin/inventory/audit"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SELLER cannot access the buyer cart")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCannotAccessCart() throws Exception {
            mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BUYER cannot ship orders")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void buyerCannotShipOrders() throws Exception {
            mockMvc.perform(patch("/api/v1/seller/orders/" + SUB_ORDER_ID + "/ship")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"trackingNumber\":\"TRK-1\",\"carrier\":\"DHL\"}"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("BUYER cannot verify sellers")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void buyerCannotVerifySellers() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/sellers/" + SELLER_ID + "/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"approved\":true}"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN cannot use buyer-only endpoints")
        @WithMockUserPrincipal(role = UserRole.ROLE_ADMIN)
        void adminCannotUseBuyerCart() throws Exception {
            mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN cannot publish products as a seller")
        @WithMockUserPrincipal(role = UserRole.ROLE_ADMIN)
        void adminCannotCreateProductAsSeller() throws Exception {
            mockMvc.perform(post("/api/v1/seller/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"X\",\"price\":10,\"stockQuantity\":1,\"category\":\"Y\"}"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SELLER cannot read the admin transaction list")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCannotReadAllOrders() throws Exception {
            mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isForbidden());
        }
    }

    // ------------------------------------------------------------------
    // Right role reaches the handler
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Correct role reaches the handler")
    class CorrectRoleIsAllowed {

        @Test
        @DisplayName("SELLER can list own catalog")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCanListOwnProducts() throws Exception {
            mockMvc.perform(get("/api/v1/seller/products"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SELLER can register a dispatch")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCanShip() throws Exception {
            mockMvc.perform(patch("/api/v1/seller/orders/" + SUB_ORDER_ID + "/ship")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"trackingNumber\":\"TRK-1\",\"carrier\":\"DHL\"}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SELLER can adjust own stock")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void sellerCanAdjustStock() throws Exception {
            mockMvc.perform(post("/api/v1/seller/inventory/adjust")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productId\":\"" + PRODUCT_ID + "\",\"quantityDelta\":-2,\"reason\":\"damaged\"}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can moderate products")
        @WithMockUserPrincipal(role = UserRole.ROLE_ADMIN)
        void adminCanModerateProducts() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/products/" + PRODUCT_ID + "/approval")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"approved\":true}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can verify a seller")
        @WithMockUserPrincipal(role = UserRole.ROLE_ADMIN)
        void adminCanVerifySeller() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/sellers/" + SELLER_ID + "/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"approved\":true}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can read the global transaction list")
        @WithMockUserPrincipal(role = UserRole.ROLE_ADMIN)
        void adminCanReadAllOrders() throws Exception {
            mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can delete a review")
        @WithMockUserPrincipal(role = UserRole.ROLE_ADMIN)
        void adminCanDeleteReview() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/reviews/" + REVIEW_ID))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("BUYER can read the cart")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void buyerCanReadCart() throws Exception {
            mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("BUYER can update a cart line")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void buyerCanUpdateCartLine() throws Exception {
            mockMvc.perform(put("/api/v1/cart/items/" + PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"quantity\":3}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("BUYER can create a review")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void buyerCanCreateReview() throws Exception {
            mockMvc.perform(post("/api/v1/buyer/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productId\":\"" + PRODUCT_ID + "\",\"rating\":5,\"comment\":\"Great\"}"))
                .andExpect(status().isCreated());
        }
    }

    // ------------------------------------------------------------------
    // Validation still applies behind the security layer
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Payload validation is enforced for authorised callers")
    class Validation {

        @Test
        @DisplayName("Blank tracking number is rejected")
        @WithMockUserPrincipal(role = UserRole.ROLE_SELLER)
        void blankTrackingNumberIsRejected() throws Exception {
            mockMvc.perform(patch("/api/v1/seller/orders/" + SUB_ORDER_ID + "/ship")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"trackingNumber\":\"\"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rating out of range is rejected")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void outOfRangeRatingIsRejected() throws Exception {
            mockMvc.perform(post("/api/v1/buyer/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productId\":\"" + PRODUCT_ID + "\",\"rating\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams.rating").exists());
        }

        @Test
        @DisplayName("Negative cart quantity is rejected")
        @WithMockUserPrincipal(role = UserRole.ROLE_BUYER)
        void negativeCartQuantityIsRejected() throws Exception {
            mockMvc.perform(post("/api/v1/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productId\":\"" + PRODUCT_ID + "\",\"quantity\":0}"))
                .andExpect(status().isBadRequest());
        }
    }
}
