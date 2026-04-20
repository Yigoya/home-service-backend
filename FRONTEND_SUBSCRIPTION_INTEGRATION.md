# Frontend Subscription Integration Guide (Chapa + Subscription APIs)

This guide explains the full frontend flow for subscription purchase and activation.

## 1) Overview

Frontend flow:

1. Load plans (`/subscriptions/plans`)
2. User selects plan
3. Create checkout (`/payment/chapa/checkout`)
4. Redirect user to `checkoutUrl` (Chapa hosted page)
5. After payment, frontend lands on your return page
6. Frontend calls verify (`/payment/chapa/verify/{txRef}`)
7. Frontend refreshes subscription status (`/subscriptions/{type}/{subscriberId}`)

> Important: `/payment/chapa/callback` is for Chapa server callback to backend. Frontend should **not** call it in production.

---

## 2) Auth

Most endpoints require your normal authenticated session/token.

- Send your app auth header (typically `Authorization: Bearer <your_jwt>`).
- Use same auth pattern you already use for protected endpoints.

---

## 3) Key IDs and meanings

For Chapa checkout request:

- `subscriberType`: one of `BUSINESS`, `TECHNICIAN`, `CUSTOMER`
- `subscriberId`: **subscriber entity id** (not generic user id)
  - if `BUSINESS` => `business.id`
  - if `TECHNICIAN` => `technician.id`
  - if `CUSTOMER` => `customer.id`

Backend uses `subscriberId` + `subscriberType` to fetch the linked user and auto-read:
- email
- first name
- last name

So frontend should NOT send email/name/userId.

---

## 4) API endpoints used by frontend

## 4.1 Get plans

`GET /subscriptions/plans?planType={planType}&language={language}`

Example:

`/subscriptions/plans?planType=MARKETPLACE&language=ENGLISH`

Typical `planType` values used:
- Business: `MARKETPLACE`, `YELLOW_PAGES`, `JOBS`, `BUSINESS`
- Technician: `HOME_PROFESSIONAL`, `TECHNICIAN`
- Customer: `TENDER`, `CUSTOMER_TENDER`

---

## 4.2 Create Chapa checkout

`POST /payment/chapa/checkout`

Request body:

```json
{
  "planId": 1,
  "subscriberType": "BUSINESS",
  "subscriberId": 12
}
```

Response shape:

```json
{
  "checkoutUrl": "https://checkout.chapa.co/...",
  "txRef": "SUB1740632123123",
  "status": "PENDING"
}
```

If plan is free, backend may return:

```json
{
  "checkoutUrl": null,
  "txRef": "SUB1740632123123",
  "status": "FREE_ACTIVATED"
}
```

Frontend behavior:
- if `status === "PENDING"` and `checkoutUrl` exists => redirect to `checkoutUrl`
- if `status === "FREE_ACTIVATED"` => show success immediately and refresh subscription data

---

## 4.3 Verify payment

`GET /payment/chapa/verify/{txRef}`

Example:

`GET /payment/chapa/verify/SUB1740632123123`

Response shape:

```json
{
  "checkoutUrl": "https://checkout.chapa.co/...",
  "txRef": "SUB1740632123123",
  "status": "SUCCESS"
}
```

Other possible status values:
- `PENDING`
- `FAILED`
- `SUCCESS`

---

## 4.4 Read current subscription status

Use one based on subscriber type:

- Business: `GET /subscriptions/business/{subscriberId}`
- Technician: `GET /subscriptions/technician/{subscriberId}`
- Customer: `GET /subscriptions/customer/{subscriberId}`

Use this after verify to refresh UI state.

---

## 5) Recommended frontend implementation

## 5.1 Checkout action

1. User picks plan
2. Call `/payment/chapa/checkout`
3. Save `txRef` in local state/session (needed after return)
4. Redirect to `checkoutUrl`

## 5.2 Return page flow (`/chapa/return`)

When user returns from Chapa:

1. Read `txRef` from your saved state (or URL if you include it in your own state handling)
2. Poll verify endpoint until final state:
   - every 2-3 sec
   - timeout after ~60 sec
3. If `SUCCESS`:
   - call subscription status endpoint
   - update UI (active plan badge, limits, features)
4. If `FAILED`:
   - show failure message and retry option
5. If still `PENDING` after timeout:
   - show “Payment processing, refresh later”

---

## 6) UI state model (suggested)

- `IDLE`
- `CREATING_CHECKOUT`
- `REDIRECTING_TO_CHAPA`
- `VERIFYING`
- `SUCCESS`
- `FAILED`
- `PROCESSING`

---

## 7) Error handling

Common backend errors:

- `400` invalid `planId` or wrong `planType`/`subscriberType` combination
- `404` subscriber not found
- `500` external payment/provider issue

Frontend handling:

- Show backend message if available
- Keep a retry button for `checkout` and `verify`
- Log `txRef` for support/debug

---

## 8) Security notes

- Do not expose secret keys in frontend
- Do not trust frontend-only success
- Always trust backend `verify` + subscription status endpoint as source of truth

---

## 9) Minimal frontend pseudo-code

```ts
async function startSubscription(planId: number, subscriberType: 'BUSINESS'|'TECHNICIAN'|'CUSTOMER', subscriberId: number) {
  const checkout = await api.post('/payment/chapa/checkout', {
    planId,
    subscriberType,
    subscriberId
  });

  if (checkout.status === 'FREE_ACTIVATED') {
    return { done: true };
  }

  sessionStorage.setItem('lastTxRef', checkout.txRef);
  window.location.href = checkout.checkoutUrl;
}

async function handleChapaReturn(subscriberType: string, subscriberId: number) {
  const txRef = sessionStorage.getItem('lastTxRef');
  if (!txRef) throw new Error('Missing txRef');

  for (let i = 0; i < 20; i++) {
    const verify = await api.get(`/payment/chapa/verify/${txRef}`);
    if (verify.status === 'SUCCESS') {
      await refreshSubscription(subscriberType, subscriberId);
      return 'SUCCESS';
    }
    if (verify.status === 'FAILED') return 'FAILED';
    await wait(3000);
  }

  return 'PENDING';
}
```

---

## 10) Postman/testing reference

Use collection file:

- `postman-telebirr-subscription-collection.json`

(contains updated Chapa checkout/verify flow).
