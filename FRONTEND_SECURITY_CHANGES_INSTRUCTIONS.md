# Frontend Instructions After Backend Security Changes

Date: 2026-04-06

This document lists what the frontend must update to stay compatible with the latest backend security hardening.

---

## 1) Subscription APIs (`/subscriptions/**`)

### What changed
- Backend now enforces strict ownership + role checks.
- Request body for create/update subscription now only allows:
  - `planId`
- Any extra JSON fields are rejected.

### Frontend action
- Send only:

```json
{ "planId": 123 }
```

- Do **not** send fields like `price`, `status`, `verified`, `role`, etc.
- If user tries to update another user/business/technician subscription, expect `403`.

---

## 2) Booking status update (`PUT /booking/update-status`)

### What changed
- Backend now verifies booking ownership/assignment from token context.
- Role-based status transitions are enforced.

### Allowed status actions
- **Technician** can set only:
  - `ACCEPTED`, `DENIED`, `TECHNICIAN_STARTED`, `CONFIRMED`, `COMPLETED`
- **Customer** can set only:
  - `CUSTOMER_STARTED`, `CANCELED`, `PENDING`

### Frontend action
- Only show status buttons valid for current user role.
- Keep request format:

```json
{ "bookingId": 1001, "status": "ACCEPTED" }
```

- If invalid status or foreign booking is used, expect `403`.

---

## 3) Booking list by technician (`GET /booking/technician/{id}`)

### What changed
- Backend now blocks access if `{id}` is not the authenticated technician.

### Frontend action
- Always use the logged-in technician id from auth/session state.
- Do not allow manual override of technician id in UI routing/state.

---

## 4) Profile image upload (`POST /profile/uploadProfileImage/{userId}`)

### What changed
- Endpoint now expects `multipart/form-data` with `file` field.
- Image-only validation is enforced.
- Unsafe/scriptable formats (e.g., SVG) are blocked.

### Frontend action
- Use `FormData` and append under key `file`.
- Use accepted file types:
  - `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- Do not upload SVG.

Example (JS):

```js
const formData = new FormData();
formData.append("file", selectedFile);

await api.post(`/profile/uploadProfileImage/${userId}`, formData, {
  headers: { "Content-Type": "multipart/form-data" }
});
```

---

## 5) Public upload access (`/uploads/{filename}`)

### What changed
- `/uploads/**` is no longer public permit-all.
- Static serving is restricted to safe image extensions only.
- Sensitive document types are blocked from direct static access.

### Frontend action
- Do not assume any uploaded file URL is publicly readable.
- For IDs/licenses/contracts, use protected backend APIs (if available) instead of direct `/uploads/...` links.
- Keep auth token attached when app requests protected resources.

---

## 6) Technician listing (`GET /technicians`)

### What changed
- Endpoint now returns a sanitized public DTO (not full entity).
- Sensitive fields are removed (no password/internal user fields, no document paths list from entity internals).

### Frontend action
- If you previously relied on raw entity fields, update mapping to public fields only.
- Safe fields include: `id`, `name`, `businessName`, `yearsExperience`, `serviceArea`, `bio`, `availability`, `rating`, `completedJobs`, `verified`, location summary, `profileImage`, `services`.

---

## 7) Marketplace product validation (`PUT /marketplace/products/{id}`, `POST /marketplace/products`)

### What changed
- Server now enforces numeric/business constraints.
- Invalid payloads return validation errors.

### Required/validated behavior
- `price` must be `> 0`
- `stockQuantity >= 0`
- `minOrderQuantity >= 0`
- `minOrderQuantity <= stockQuantity` (if both provided)
- `currency` must be 3 uppercase letters (e.g., `ETB`, `USD`)

### Frontend action
- Add matching client-side checks before submit.
- Block negative values in UI and form validation.

---

## 8) Phone number validation (Marketplace Business payload)

### What changed
- Business request phone fields now validated server-side.
- Pattern required: `^\+?[0-9]{10,13}$`

### Frontend action
- Validate and normalize phone number before submit.
- Show immediate form message for invalid values.

---

## 9) Expected error handling updates (recommended)

Frontend should gracefully handle:
- `400` validation errors (bad payload/field constraints)
- `401` missing/expired token
- `403` authorization/ownership violation
- `404` target resource not found

Show user-friendly messages like:
- "You don’t have permission for this action."
- "Please check your input values."

---

## Quick QA Checklist

- [ ] Subscription requests send only `{ planId }`
- [ ] Booking status UI is role-aware
- [ ] Profile upload uses `multipart/form-data` + `file`
- [ ] SVG uploads are blocked in UI
- [ ] `/uploads/...` is not assumed public for documents
- [ ] Product forms block negative price/amounts
- [ ] Currency uses 3-letter uppercase code
- [ ] Phone format validated before submit
- [ ] 400/401/403 responses are handled with proper UX
