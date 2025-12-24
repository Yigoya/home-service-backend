# Enhanced Business Promotion System Documentation

## Overview
This document outlines the comprehensive promotion system for businesses, including both public-facing endpoints for customers and management endpoints for business owners. The system now supports service-specific promotions, featured promotions, and enhanced promotion management.

## New Features Added
- **Service Integration**: Promotions can now be linked to specific business services
- **Featured Promotions**: Ability to mark promotions as featured for better visibility
- **Enhanced Promotion Details**: Added image URLs, terms and conditions, and service information
- **Professional Management**: Complete CRUD operations with service connections

## Key Features

### 1. Public Promotion DTO
- **Purpose**: Sanitized data transfer object for public consumption
- **Security**: Excludes sensitive business information
- **Includes**: 
  - Promotion details (title, description, dates, discount)
  - Business basic info (name, logo, category, ID)
  - Active status indicator

### 2. Active Promotion Filtering
- Only returns currently active promotions (between start and end dates)
- Automatic filtering ensures expired promotions are not displayed
- Real-time validation of promotion status

## Public Endpoints

### 1. Get All Active Promotions
```
GET /businesses/promotions/public
```
**Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 12) - Items per page

**Description:** Returns all currently active promotions across all businesses.

### 2. Get Featured Promotions
```
GET /businesses/promotions/public/featured
```
**Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 8) - Items per page

**Description:** Returns promotions marked as "FEATURED_LISTING" type for homepage display.

### 3. Get Promotion by ID
```
GET /businesses/promotions/public/{id}
```
**Description:** Returns a specific active promotion by ID. Returns 404 if promotion is expired or not found.

### 4. Get Promotions by Type
```
GET /businesses/promotions/public/type/{type}
```
**Parameters:**
- `type` (required) - Promotion type: FEATURED_LISTING, SPECIAL_OFFER, or DISCOUNT
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 12) - Items per page

**Description:** Filter promotions by specific type.

### 5. Get Promotions by Industry
```
GET /businesses/promotions/public/industry/{industry}
```
**Parameters:**
- `industry` (required) - Business industry name
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 12) - Items per page

**Description:** Returns active promotions from businesses in a specific industry.

### 6. Get Promotions by Business Type
```
GET /businesses/promotions/public/business-type/{businessType}
```
**Parameters:**
- `businessType` (required) - Business type (enum value)
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 12) - Items per page

**Description:** Returns active promotions from businesses of a specific type.

### 7. Search Promotions
```
GET /businesses/promotions/public/search
```
**Parameters:**
- `query` (required) - Search term
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 12) - Items per page

**Description:** Search promotions by title, description, business name, or industry.

### 8. Get Promotions by Business
```
GET /businesses/promotions/public/business/{businessId}
```
**Parameters:**
- `businessId` (required) - Business ID
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Items per page

**Description:** Returns all active promotions for a specific business.

## Management Endpoints (Business Owners)

### 1. Make Promotion Featured
```
PATCH /businesses/promotions/{id}/featured
```
**Description:** Mark a promotion as featured for better visibility on the platform.

### 2. Remove Featured Status
```
PATCH /businesses/promotions/{id}/unfeatured
```
**Description:** Remove featured status from a promotion.

### 3. Get Promotion Details (Management View)
```
GET /businesses/promotions/{id}/details
```
**Description:** Get complete promotion details including service connections and management information.

### 4. Get Business Services for Promotion
```
GET /businesses/{businessId}/services
```
**Description:** Get all services available for a business to link with promotions.

### 5. Create Promotion with Services (supports multiple images)
```
POST /businesses/promotions
Content-Type: multipart/form-data
```
**Form Fields:**
- `businessId` (number)
- `title` (string)
- `description` (string)
- `startDate` (ISO datetime)
- `endDate` (ISO datetime)
- `type` (enum)
- `discountPercentage` (number)
- `isFeatured` (boolean)
- `serviceIds` (array of numbers)
- `images` (array of files) — send multiple files using `images[]`
- `termsAndConditions` (string)

### 6. Update Promotion with Services
```
PUT /businesses/promotions/{id}
```
**Description:** Update promotion details including linked services.

## Response Format

### PublicPromotionDTO Structure
```json
{
  "id": 1,
  "title": "Summer Special Discount",
  "description": "Get 20% off on all services this summer!",
  "startDate": "2025-01-15T00:00:00",
  "endDate": "2025-03-15T23:59:59",
  "type": "SPECIAL_OFFER",
  "discountPercentage": 20.0,
  "businessName": "ABC Services",
  "businessLogo": "/uploads/logo.png",
  "businessId": 5,
  "businessCategory": "Home Services",
  "isActive": true
  "images": ["/uploads/promo1.jpg", "/uploads/promo2.jpg"]
}
```

### Paginated Response
```json
{
  "content": [/* Array of PublicPromotionDTO */],
  "pageable": {
    "sort": { "sorted": false, "unsorted": true },
    "pageNumber": 0,
    "pageSize": 12
  },
  "totalElements": 25,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 12
}
```

## Database Enhancements

### New Repository Methods
- `findActivePromotionsByType()` - Filter by promotion type
- `findActivePromotionsByCategory()` - Filter by business category
- `findActivePromotionsBySearch()` - Full-text search capability
- `findFeaturedPromotions()` - Get featured promotions only

## Usage Examples

### Frontend Integration
```javascript
// Get featured promotions for homepage
const featuredPromotions = await fetch('/businesses/promotions/public/featured?size=6');

// Search promotions
const searchResults = await fetch('/businesses/promotions/public/search?query=discount&page=0&size=10');

// Get promotions by category
const restaurantPromotions = await fetch('/businesses/promotions/public/category/Restaurant');
```

### Mobile App Integration
```javascript
// Get all active promotions with pagination
const promotions = await api.get('/businesses/promotions/public', {
  params: { page: 0, size: 20 }
});

// Get specific business promotions
const businessPromotions = await api.get(`/businesses/promotions/public/business/${businessId}`);
```

## Security Features

1. **Data Sanitization**: Only public-safe data is exposed
2. **Active Filtering**: Expired promotions are automatically filtered out
3. **No Authentication Required**: Public endpoints for easy integration
4. **Rate Limiting Ready**: Endpoints designed for high-traffic scenarios

## Performance Considerations

1. **Pagination**: All endpoints support pagination to handle large datasets
2. **Database Indexing**: Optimized queries with proper indexing on dates and categories
3. **Caching Ready**: Responses can be cached for better performance
4. **Efficient Filtering**: Database-level filtering reduces memory usage

## Error Handling

- **400 Bad Request**: Invalid parameters (e.g., invalid promotion type)
- **404 Not Found**: Promotion not found or expired
- **500 Internal Server Error**: Server-side issues

## Best Practices

1. **Use appropriate page sizes** (8-12 for featured, 10-20 for listings)
2. **Implement client-side caching** for frequently accessed data
3. **Handle pagination properly** in frontend applications
4. **Validate promotion status** before displaying to users
5. **Implement proper error handling** for better user experience

## Future Enhancements

1. **Location-based filtering** for nearby promotions
2. **User preference-based recommendations**
3. **Promotion analytics and tracking**
4. **Social sharing capabilities**
5. **Push notification integration**