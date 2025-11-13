# Saved Business Feature

This feature allows customers to save and manage their favorite businesses.

## Implementation Overview

### Models
- **SavedBusiness**: Entity representing the relationship between a customer and a saved business
- **Customer**: Extended to support saved businesses relationship
- **Business**: Existing business entity

### DTOs
- **SavedBusinessSummaryDto**: Simplified DTO for list responses with essential business data only
- **SavedBusinessDto**: Complete DTO with full business details (for detailed views)

### Repositories
- **SavedBusinessRepository**: Handles CRUD operations for saved businesses
- **CustomerRepository**: Manages customer data access

### Services
- **SavedContentService**: Extended with saved business functionality
  - `saveBusiness(businessId, customerId)`: Save a business for a customer
  - `unsaveBusiness(businessId, customerId)`: Remove a saved business
  - `getSavedBusinesses(customerId, pageable)`: Get paginated list of saved businesses
  - `isBusinessSaved(businessId, customerId)`: Check if a business is saved

### Controllers
- **CustomerController**: Dedicated customer endpoints
- **BusinessController**: Extended with convenience save/unsave endpoints
- **SavedContentController**: Extended with saved business endpoints

## API Endpoints

### Customer Controller
- `POST /customers/{customerId}/saved-businesses/{businessId}` - Save a business
- `DELETE /customers/{customerId}/saved-businesses/{businessId}` - Unsave a business
- `GET /customers/{customerId}/saved-businesses` - Get saved businesses (paginated)
- `GET /customers/{customerId}/saved-businesses/{businessId}/is-saved` - Check if business is saved

### Business Controller (Convenience endpoints)
- `POST /businesses/{businessId}/save?customerId={customerId}` - Save a business
- `DELETE /businesses/{businessId}/unsave?customerId={customerId}` - Unsave a business
- `GET /businesses/{businessId}/is-saved?customerId={customerId}` - Check if business is saved

### Saved Content Controller
- `POST /saved/{customerId}/businesses/{businessId}` - Save a business
- `DELETE /saved/{customerId}/businesses/{businessId}` - Unsave a business
- `GET /saved/{customerId}/businesses` - Get saved businesses (paginated)
- `GET /saved/{customerId}/businesses/{businessId}/is-saved` - Check if business is saved

## Database Schema

```sql
CREATE TABLE saved_businesses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id),
    FOREIGN KEY (business_id) REFERENCES businesses(id),
    UNIQUE KEY unique_customer_business (customer_id, business_id)
);
```

## Response Structure

### Saved Business List Response (Simplified for Frontend)
The list endpoints return a simplified `SavedBusinessSummaryDto` with only essential data:

```json
{
  "content": [
    {
      "id": 1,
      "businessId": 5,
      "businessName": "Coffee Shop",
      "businessLogo": "/uploads/logo.png",
      "businessEmail": "info@coffeeshop.com",
      "businessPhoneNumber": "+1234567890",
      "businessIndustry": "Food & Beverage",
      "businessDescription": "Best coffee in town",
      "verified": true,
      "featured": false,
      "savedAt": "2025-01-20T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

## Usage Examples

### Save a business
```bash
curl -X POST "http://localhost:8080/customers/1/saved-businesses/5"
```

### Get saved businesses (simplified response)
```bash
curl "http://localhost:8080/customers/1/saved-businesses?page=0&size=10"
```

### Check if business is saved
```bash
curl "http://localhost:8080/customers/1/saved-businesses/5/is-saved"
```

### Unsave a business
```bash
curl -X DELETE "http://localhost:8080/customers/1/saved-businesses/5"
```

## Features
- Prevents duplicate saves (unique constraint)
- Paginated results for saved businesses
- Automatic timestamp tracking
- Cross-origin support for frontend integration
- Follows existing codebase patterns and conventions