# New and Top Products Endpoints

## Overview
Two new paginated endpoints have been added to the Marketplace API for fetching new and top products.

## Endpoints

### 1. Get New Products
**Endpoint:** `GET /marketplace/products/new`

**Description:** Returns recently added products that are active and in stock, sorted by creation date (newest first).

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Number of items per page

**Example Request:**
```
GET /marketplace/products/new?page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "name": "Product Name",
      "description": "Product description",
      "price": 99.99,
      "currency": "USD",
      "stockQuantity": 100,
      "minOrderQuantity": 1,
      "images": ["image1.jpg", "image2.jpg"],
      "category": "Electronics",
      "sku": "SKU123",
      "active": true,
      "businessId": 456,
      "specifications": "Product specs",
      "serviceIds": [1, 2, 3]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

### 2. Get Top Products
**Endpoint:** `GET /marketplace/products/top`

**Description:** Returns top/popular products that are active and in stock. Currently sorted by product ID (can be enhanced with actual analytics like views, orders, revenue).

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Number of items per page

**Example Request:**
```
GET /marketplace/products/top?page=0&size=20
```

**Response:** Same structure as New Products endpoint

## Implementation Details

### Repository Layer
Added two new query methods in `ProductRepository`:
- `findNewProducts(Pageable)` - Orders by `createdAt DESC`
- `findTopProducts(Pageable)` - Currently orders by `id DESC` (placeholder for analytics)

### Service Layer
Added two methods in `ProductService`:
- `getNewProducts(int page, int size)`
- `getTopProducts(int page, int size)`

### Controller Layer
Added two endpoints in `MarketplaceController`:
- `GET /marketplace/products/new`
- `GET /marketplace/products/top`

## Future Enhancements

The `findTopProducts` query currently uses a simple ordering mechanism. To make it truly reflect "top" products, consider:

1. **Add analytics tracking:**
   - Product views count
   - Order count
   - Total revenue
   - Customer ratings

2. **Update the query to:**
```java
@Query("SELECT p FROM Product p WHERE p.isActive = true AND p.inStock = true " +
       "ORDER BY p.viewCount DESC, p.orderCount DESC, p.totalRevenue DESC")
Page<Product> findTopProducts(Pageable pageable);
```

3. **Add fields to Product entity:**
```java
private Long viewCount = 0L;
private Long orderCount = 0L;
private Double totalRevenue = 0.0;
private Double averageRating = 0.0;
```

## Testing

Test the endpoints using curl or Postman:

```bash
# Get new products
curl -X GET "http://localhost:8080/marketplace/products/new?page=0&size=10"

# Get top products
curl -X GET "http://localhost:8080/marketplace/products/top?page=0&size=10"
```
