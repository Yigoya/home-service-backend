# Technician Portfolio Endpoints

Base path: `/profile`

## GET /technician/{technicianId}/portfolio
Returns all portfolio items for a technician.

- **Headers:** `Accept: application/json`
- **Path params:** `technicianId` (number, required)

### 200 Response
Content-Type: `application/json`
```json
[
  {
    "id": 7,
    "description": "Kitchen remodel",
    "beforeImage": "uploads/before-123.jpg",
    "afterImage": "uploads/after-456.jpg"
  }
]
```

### 404 Response
Technician not found.
```json
{
  "message": "Technician not found"
}
```

## POST /technician/{technicianId}/portfolio
Adds a new portfolio item. Supports either `beforeImage`/`afterImage` or alternative keys `before`/`after`.

- **Headers:** `Content-Type: multipart/form-data`
- **Path params:** `technicianId` (number, required)
- **Form fields:**
  - `description` (string, optional)
  - `beforeImage` (file, optional) or `before` (file, optional)
  - `afterImage` (file, optional) or `after` (file, optional)

### 200 Response
Content-Type: `text/plain`
```
Portfolio item added successfully
```

### 404 Response
Technician not found.
```json
{
  "message": "Technician not found"
}
```

## DELETE /technician/{technicianId}/portfolio/{portfolioId}
Deletes a portfolio item owned by the technician.

- **Path params:**
  - `technicianId` (number, required)
  - `portfolioId` (number, required)

### 200 Response
Content-Type: `text/plain`
```
Portfolio item deleted successfully
```

### 404 Response
Portfolio item not found.
```json
{
  "message": "Portfolio item not found"
}
```

### 400 Response
Portfolio item exists but belongs to a different technician.
```json
{
  "message": "Portfolio item does not belong to this technician"
}
```

## Notes
- File paths in responses are the stored file names returned by the backend (`FileStorageService`).
- All endpoints live under `/profile`; prepend the server base URL (e.g., `http://localhost:8080/profile/...`).
- Responses above assume the global error handler returns `message` fields for exceptions.
