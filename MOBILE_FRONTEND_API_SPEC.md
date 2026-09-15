# Webliix Mobile Frontend API Specification

## Overview
This document describes the mobile API contract for Webliix frontend development. It covers:
- Authentication
- Mobile dashboard APIs
- Employee mobile APIs
- Customer mobile APIs
- File download
- Notification and device management
- Pagination / filtering / sorting
- Common response structure and error handling

This spec is based on the current backend code and the mobile module skeleton added in the repository.

---

## 1. Base API Settings

### Base URL
- `http://localhost:8082`

### API prefix
- `/api/v1`

### Auth
- `Authorization: Bearer <accessToken>`

### Config values
The backend currently exposes the following keys in `application.properties`:
- `jwt.secret`
- `jwt.expiration`
- `minio.endpoint`
- `minio.access-key`
- `minio.secret-key`
- `minio.bucket`

### Recommended frontend environment variables
- `VITE_API_BASE_URL` or equivalent
- `VITE_AUTH_TOKEN_KEY`
- `VITE_REFRESH_TOKEN_KEY`
- `VITE_MINIO_ENDPOINT`
- `VITE_FCM_SENDER_ID`

---

## 2. Common Response Envelope
Most endpoints use `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Invoice fetched",
  "data": { ... }
}
```

For list endpoints, `data` usually contains Spring `Page<T>`:
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## 3. Authentication APIs

### `POST /api/v1/auth/login`
Request:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJI..."
}
```

### `POST /api/v1/auth/register`
Request:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "p@ssw0rd",
  "phone": "+911234567890"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJI..."
}
```

> Note: the current backend response contains only `accessToken`. For mobile, a refresh token endpoint and device-bound refresh token support should be added.

---

## 4. Mobile Dashboard

### `GET /api/v1/mobile/dashboard`
Response:
```json
{
  "summary": {},
  "recentActivities": [],
  "notifications": []
}
```

### Mobile dashboard payload pattern
This response should be mobile-optimized and compact. Suggested shape:
```json
{
  "summary": {
    "openLeads": 4,
    "pendingInvoices": 3,
    "todayAttendance": "present"
  },
  "recentActivities": [
    {
      "type": "project_updated",
      "title": "Project A updated",
      "timestamp": "2026-06-12T12:00:00Z"
    }
  ],
  "notifications": [
    {
      "id": 123,
      "title": "Invoice Paid",
      "body": "Invoice #INV-456 has been paid.",
      "read": false,
      "createdAt": "2026-06-12T12:12:34Z"
    }
  ]
}
```

---

## 5. Employee Mobile APIs
These endpoints are intended for employee app workflows.

### `GET /api/v1/mobile/attendance`
- Returns current employee attendance or list of records.
- If not implemented separately, the frontend can also use `/api/v1/attendance`.

### `POST /api/v1/mobile/check-in`
Request body can be minimal or empty, depending on backend implementation.
Response:
```json
{
  "status": "ok"
}
```

### `POST /api/v1/mobile/check-out`
Response:
```json
{
  "status": "ok"
}
```

### `GET /api/v1/mobile/leave`
- Returns leave requests relevant to the employee.
- Can reuse `/api/v1/leaves` if mobile-specific endpoint is not available.

### `GET /api/v1/mobile/payroll`
- Returns payroll records relevant to the employee.
- Can reuse `/api/v1/payrolls/employee/{employeeId}`.

### Existing related endpoints
- `POST /api/v1/attendance` (record attendance)
- `GET /api/v1/attendance?page=0&size=20`
- `GET /api/v1/employees/{employeeId}/attendance?page=0&size=20`
- `POST /api/v1/leaves`
- `GET /api/v1/leaves?page=0&size=20`
- `GET /api/v1/leaves/{id}`
- `GET /api/v1/payrolls/employee/{employeeId}?page=0&size=20`
- `GET /api/v1/payrolls/dashboard`

---

## 6. Customer Mobile APIs
These should provide compact mobile data for customer-facing workflows.

### Required mobile endpoints
- `GET /api/v1/mobile/projects`
- `GET /api/v1/mobile/invoices`
- `GET /api/v1/mobile/tickets`
- `GET /api/v1/mobile/files`

### Current backend endpoints to reuse
- `GET /api/v1/projects?page=0&size=20`
- `GET /api/v1/projects/{id}`
- `GET /api/v1/projects/search?keyword=...&page=0&size=20`
- `GET /api/v1/invoices?page=0&size=20`
- `GET /api/v1/invoices/{id}`
- `GET /api/v1/invoices/search?keyword=...&page=0&size=20`
- `GET /api/v1/tickets`
- `GET /api/v1/tickets/{id}`
- `GET /api/v1/tickets/dashboard`

### Mobile invoice list example
Response data shape:
```json
{
  "success": true,
  "message": "Invoices fetched",
  "data": {
    "content": [
      {
        "id": 192,
        "invoiceNumber": "INV-192",
        "status": "Paid",
        "total": 12345.67,
        "dueDate": "2026-06-20"
      }
    ],
    "totalElements": 40,
    "totalPages": 2,
    "size": 20,
    "number": 0
  }
}
```

---

## 7. File / Document APIs
The backend currently supports generic storage endpoints:
- `POST /api/v1/storage/upload`
- `GET /api/v1/storage/{id}`
- `DELETE /api/v1/storage/{id}`
- `GET /api/v1/storage/metadata/{id}`

### Recommended mobile extension
Add mobile download URL support:
- `GET /api/v1/mobile/files/{id}/download-url`

Response example:
```json
{
  "success": true,
  "message": "Download URL generated",
  "data": {
    "url": "https://minio.example.com/webliix/file.pdf?X-Amz-Signature=...",
    "expiresAt": "2026-06-12T13:00:00Z"
  }
}
```

This avoids streaming heavy files through the backend.

---

## 8. Notification / Device Management

### Mobile device registration
The frontend should register the device after login with:
- `tenantId`
- `userId`
- `deviceId`
- `deviceType`
- `osVersion`
- `appVersion`
- `fcmToken`

Suggested endpoint:
- `POST /api/v1/mobile/devices`

Example payload:
```json
{
  "tenantId": 1,
  "userId": 10,
  "deviceId": "device-uuid-1234",
  "deviceType": "android",
  "osVersion": "14.5",
  "appVersion": "1.0.0",
  "fcmToken": "fcm_token_here"
}
```

### Notification preferences
Suggested endpoint:
- `GET /api/v1/mobile/notification-preferences?userId=10`
- `PUT /api/v1/mobile/notification-preferences`

Request payload:
```json
{
  "tenantId": 1,
  "userId": 10,
  "emailEnabled": true,
  "pushEnabled": true,
  "smsEnabled": false,
  "inAppEnabled": true
}
```

### Push notifications
Backend placeholder exists as `PushNotificationService`.
Mobile apps should support event types like:
- New Lead Assigned
- Project Updated
- Invoice Paid
- Ticket Assigned
- Leave Approved
- Payroll Generated

---

## 9. Offline Sync

### Suggested contract
- `POST /api/v1/mobile/sync/events`
- `GET /api/v1/mobile/sync/events?since=2026-06-12T00:00:00Z`

Event payload:
```json
{
  "entityType": "invoice",
  "entityId": "192",
  "operation": "UPDATE",
  "payload": "{ ... }",
  "timestamp": "2026-06-12T12:34:56Z",
  "userId": 10,
  "deviceId": "device-uuid-1234"
}
```

This allows the mobile app to queue local changes and sync them after reconnect.

---

## 10. Pagination / Filtering / Sorting
All list endpoints should support:
- `page=0`
- `size=20`
- `sort=createdAt,desc`

Examples:
- `GET /api/v1/projects?page=0&size=20&sort=createdAt,desc`
- `GET /api/v1/invoices/search?keyword=customer&page=0&size=20&sort=dueDate,asc`

When mobile-specific endpoints are introduced, keep the same query contract.

---

## 11. Rate Limiting
The backend should protect these categories:
- Login APIs
- Portal / customer APIs
- Public APIs
- AI / heavy APIs

Recommended patterns:
- Redis-based throttling
- Bucket4j rate limiter
- Return HTTP 429 when the rate limit is exceeded

Frontend should handle 429 by showing a friendly retry message.

---

## 12. Monitoring / Observability
Suggested telemetry for mobile API traffic:
- Request count
- Response time
- Error count
- Endpoint usage

Recommended stack:
- Prometheus
- Grafana

Frontend should also track:
- API latency
- failed auth refresh
- sync failure rates

---

## 13. Screen-by-Screen Contract

### Login / Auth screen
- `POST /api/v1/auth/login`
- response: `accessToken`
- store token securely
- register device after login

### Home / Dashboard screen
- `GET /api/v1/mobile/dashboard`
- render summary, notifications, recent activities

### Employee attendance screen
- `GET /api/v1/mobile/attendance`
- `POST /api/v1/mobile/check-in`
- `POST /api/v1/mobile/check-out`

### Employee leave screen
- `GET /api/v1/mobile/leave`
- optionally create leave via `/api/v1/leaves`

### Employee payroll screen
- `GET /api/v1/mobile/payroll`
- optionally view details via `/api/v1/payrolls/{id}`

### Customer projects screen
- `GET /api/v1/mobile/projects`
- view detail via `/api/v1/projects/{id}`
- search via `/api/v1/projects/search?keyword=...`

### Customer invoices screen
- `GET /api/v1/mobile/invoices`
- view detail via `/api/v1/invoices/{id}`
- search via `/api/v1/invoices/search?keyword=...`

### Customer tickets screen
- `GET /api/v1/mobile/tickets`
- view detail via `/api/v1/tickets/{id}`
- create via `POST /api/v1/tickets`

### Files screen
- `GET /api/v1/mobile/files`
- `GET /api/v1/mobile/files/{id}/download-url`
- open file using signed URL

### Notification settings screen
- `GET /api/v1/mobile/notification-preferences`
- `PUT /api/v1/mobile/notification-preferences`

---

## 14. Backend gaps to fill before frontend implementation
The backend currently has these gaps for mobile-first support:
- No refresh token endpoint or response payload
- No mobile-specific `/api/v1/mobile/...` customer endpoints
- No mobile device registration endpoint
- No notification preference endpoints
- No signed download URL endpoint
- No offline sync event endpoint
- No Swagger/OpenAPI documentation

---

## 15. Next steps for frontend development
1. Add the missing mobile endpoints in backend.
2. Create OpenAPI/Swagger docs for all mobile APIs.
3. Build the frontend network layer with:
   - auth token handling
   - refresh token logic
   - pagination helper
   - signed file download helper
   - retry / offline queue helper
4. Implement mobile UI screens according to the contract above.

---

## 16. Suggested feature flags
- `mobile.auth.refresh.enabled`
- `mobile.notifications.enabled`
- `mobile.sync.enabled`
- `mobile.files.signed-url.enabled`

---

## 17. Example mobile API JSON inventory
### Auth
`POST /api/v1/auth/login`
`POST /api/v1/auth/register`

### Dashboard
`GET /api/v1/mobile/dashboard`

### Attendance
`GET /api/v1/mobile/attendance`
`POST /api/v1/mobile/check-in`
`POST /api/v1/mobile/check-out`

### Leave
`GET /api/v1/mobile/leave`

### Payroll
`GET /api/v1/mobile/payroll`

### Projects
`GET /api/v1/mobile/projects`

### Invoices
`GET /api/v1/mobile/invoices`

### Tickets
`GET /api/v1/mobile/tickets`

### Files
`GET /api/v1/mobile/files`
`GET /api/v1/mobile/files/{id}/download-url`

### Device
`POST /api/v1/mobile/devices`

### Notification preferences
`GET /api/v1/mobile/notification-preferences`
`PUT /api/v1/mobile/notification-preferences`

### Sync
`POST /api/v1/mobile/sync/events`
`GET /api/v1/mobile/sync/events`
