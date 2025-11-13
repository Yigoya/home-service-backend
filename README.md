# home-service-bachend
# home-service-bachend

## Marketplace: Get products by business with filters

Endpoint: `GET /marketplace/businesses/{businessId}/products`

Query params (all optional unless noted):
- search: keyword contained in name/description
- category: exact category string
- minPrice, maxPrice: numeric price bounds
- inStock: true|false
- active: true|false
- serviceId: filter by linked service id
- page, size: pagination (defaults 0, 10)
- sortBy: field to sort by (e.g., createdAt, price, name) default `createdAt`
- sortDir: `asc` or `desc` (default `desc`)

Example:
`GET /marketplace/businesses/42/products?search=chair&category=furniture&minPrice=50&maxPrice=500&inStock=true&active=true&serviceId=7&page=0&size=12&sortBy=price&sortDir=asc`

## Marketplace: Get businesses by owner

Endpoint: `GET /marketplace/businesses/by-owner/{ownerId}`

Query params:
- page, size: pagination (defaults 0, 10)

Example:
`GET /marketplace/businesses/by-owner/123?page=0&size=10`

Response template (Page<BusinessDTO>):
```
{
	"content": [
		{
			"id": 99,
			"name": "Acme Services",
			"email": "contact@acme.com",
			"phoneNumber": "+251900000000",
			"businessType": "SMALL_BUSINESS",
			"description": "...",
			"logo": "/uploads/logo.png",
			"website": "https://acme.com",
			"foundedYear": 2018,
			"employeeCount": 12,
			"verified": true,
			"industry": "Home Services",
			"taxId": "TX-123",
			"certifications": "ISO9001",
			"minOrderQuantity": 1,
			"tradeTerms": "NET30",
			"images": ["/uploads/1.png", "/uploads/2.png"],
			"featured": false,
			"owner": { "id": 123, "username": "ownername" }
		}
	],
	"pageable": { "pageNumber": 0, "pageSize": 10 },
	"totalElements": 1,
	"totalPages": 1,
	"last": true,
	"first": true,
	"size": 10,
	"number": 0,
	"sort": { "sorted": false, "unsorted": true, "empty": true },
	"numberOfElements": 1,
	"empty": false
}
```
