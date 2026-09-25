/** Tipos que reflejan los DTO del backend. */

export type UserRole = 'ROLE_ADMIN' | 'ROLE_SELLER' | 'ROLE_BUYER';

export type AuthResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  role: UserRole;
  sellerApproved: boolean | null;
};

export type ProductStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED' | 'INACTIVE';

export type Product = {
  id: string;
  name: string;
  description: string | null;
  slug: string;
  price: number;
  currencyCode: string;
  /** Stock vendible real (lo calcula el módulo de inventario). */
  stockQuantity: number;
  imageUrl: string | null;
  status: ProductStatus;
  sellerId: string;
  category: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateProductBody = {
  name: string;
  description?: string;
  price: number;
  currencyCode?: string;
  stockQuantity: number;
  category: string;
  imageUrl?: string;
};

export type UpdateProductBody = Partial<Omit<CreateProductBody, 'stockQuantity'>>;

export type StockAvailability = {
  productId: string;
  sellerId: string;
  availableQuantity: number;
  reservedQuantity: number;
  sellableQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
  available: boolean;
  lastAdjustedAt: string;
};

export type SellerStock = {
  stockItemId: string;
  productId: string;
  sellerId: string;
  availableQuantity: number;
  reservedQuantity: number;
  sellableQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
  lastAdjustedAt: string;
};

export type StockMovement = {
  movementId: string;
  productId: string;
  sellerId: string;
  movementType: 'INITIAL' | 'ADJUSTMENT' | 'RESERVATION' | 'SALE' | 'CANCELLATION' | 'ADMIN_CORRECTION';
  quantityDelta: number;
  quantityBefore: number;
  quantityAfter: number;
  reason: string | null;
  performedBy: string | null;
  reference: string | null;
  occurredAt: string;
};

export type CartItem = {
  itemId: string;
  productId: string;
  sellerId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  currencyCode: string;
};

export type Cart = {
  cartId: string;
  buyerId: string;
  status: 'ACTIVE' | 'CHECKED_OUT' | 'ABANDONED';
  items: CartItem[];
  totalItemCount: number;
  subtotal: number;
  currencyCode: string;
  updatedAt: string;
};

export type OrderStatus = 'PENDING_PAYMENT' | 'PAID' | 'PAYMENT_FAILED' | 'CANCELLED' | 'COMPLETED';
export type SubOrderStatus = 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export type OrderItem = {
  orderItemId: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  currencyCode: string;
};

export type SubOrder = {
  subOrderId: string;
  sellerId: string;
  subtotal: number;
  currencyCode: string;
  status: SubOrderStatus;
  trackingNumber: string | null;
  carrier: string | null;
  shippedAt: string | null;
  deliveredAt: string | null;
  items: OrderItem[];
};

export type Order = {
  orderId: string;
  orderNumber: string;
  buyerId: string;
  totalAmount: number;
  currencyCode: string;
  status: OrderStatus;
  paymentReference: string | null;
  paidAt: string | null;
  createdAt: string;
  subOrders: SubOrder[];
};

export type CheckoutResponse = {
  order: Order;
  paymentCaptured: boolean;
  paymentReference: string | null;
  chargedAmount: number;
  redirectUrl: string | null;
  paymentMessage: string;
};

export type Review = {
  reviewId: string;
  productId: string;
  sellerId: string;
  buyerId: string;
  rating: number;
  title: string | null;
  comment: string | null;
  verifiedPurchase: boolean;
  visible: boolean;
  createdAt: string;
};

export type ProductReviews = {
  productId: string;
  averageRating: number | null;
  totalReviews: number;
  reviews: {
    content: Review[];
    totalElements: number;
    totalPages: number;
    number: number;
    last: boolean;
    first: boolean;
  };
};

export type UserResponse = {
  id: string;
  email: string;
  role: UserRole;
  enabled: boolean;
  sellerApproved: boolean | null;
  createdAt: string;
};
