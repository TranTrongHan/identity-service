# API Implementation Checklist

Danh sach cac API can implement cho identity-service.

---

## DA HOAN THANH

| # | Method | Endpoint | Mo ta |
|---|--------|----------|-------|
| 1 | POST | `/Identity/Login/Password` | Dang nhap bang mat khau |
| 2 | POST | `/Identity/RefreshToken` | Lam moi Access Token bang Refresh Token |
| 3 | GET | `/Identity/ForceLogout` | User tu check minh co bi ep dang xuat khong (one-shot) |
| 4 | PUT | `/Admin/Account/{id}/ForceLogout` | Admin ep dang xuat 1 account |
| 5 | GET | `/Admin/Account/{id}/ForceLogout` | Admin check trang thai force-logout |

---

## MUC 1: CO BAN — He thong IAM khong hoat dong neu thieu

| # | Method | Endpoint | Mo ta | Request Body | Response |
|---|--------|----------|-------|--------------|----------|
| 1 | POST | `/Admin/Account` | Tao account moi | `{ name, username, email?, phoneNumber?, password }` | `{ id }` (UUID) |
| 2 | PUT | `/Admin/Account/{id}/Password` | Set/reset password | `{ password }` | Success |
| 3 | PUT | `/Admin/Account/{id}/Auth` | Set thong tin dang nhap (username/email/phone) | `{ field: "USERNAME"/"EMAIL"/"PHONE", value: "..." }` | Success |
| 4 | PUT | `/Admin/Account/{id}/Unlock` | Mo khoa account bi brute-force lock | (no body) | Success |
| 5 | GET | `/Admin/Account/{id}` | Xem chi tiet account | — | Account detail |
| 6 | GET | `/Admin/Account` | List accounts (paginated) | Query params: `page`, `size`, `search?` | Page of accounts |
| 7 | DELETE | `/Admin/Account/{id}` | Soft-delete account | — | Success |

### Logic can luu y:
- **POST /Admin/Account**: Tao account + hash password voi secretKey random + tao AccountAuth record
- **PUT /{id}/Auth**: Check trung (field_type + field_value unique), update hoac tao moi AccountAuth
- **PUT /{id}/Unlock**: Reset `wrongLoginCount = 0`, `accessDeniedUntil = null`
- **DELETE /{id}**: Set `deletedAt = now()` (soft delete, khong xoa thuc)

---

## MUC 2: QUAN TRI RBAC — Can cho phan quyen

| # | Method | Endpoint | Mo ta | Request Body | Response |
|---|--------|----------|-------|--------------|----------|
| 8 | POST | `/Admin/App` | Tao App moi | `{ code, name, description?, signingKey, tokenLifetimeMinutes, sessionLifetimeMinutes }` | `{ id }` |
| 9 | GET | `/Admin/App/All` | List tat ca apps | — | List apps |
| 10 | PUT | `/Admin/App/{id}` | Cap nhat App | `{ name?, description?, tokenLifetimeMinutes?, sessionLifetimeMinutes? }` | Success |
| 11 | DELETE | `/Admin/App/{id}` | Soft-delete App | — | Success |
| 12 | POST | `/Admin/AppRole` | Tao role | `{ appId, code, name }` | `{ id }` |
| 13 | GET | `/Admin/AppRole/All` | List tat ca roles | — | List roles |
| 14 | PUT | `/Admin/AppRole/{id}` | Cap nhat role | `{ name? }` | Success |
| 15 | PUT | `/Admin/AppRole/{id}/Permission` | Gan permissions cho role | `{ permissionIds: [...] }` | Success |
| 16 | DELETE | `/Admin/AppRole/{id}` | Soft-delete role | — | Success |
| 17 | POST | `/Admin/AppPermission` | Tao permission | `{ appId, code, name, groupName?, description?, includePermissionCodes? }` | `{ id }` |
| 18 | POST | `/Admin/AppPermission/Setup` | Bulk setup permissions (upsert by code) | `{ appCode, permissions: [...] }` | Success |
| 19 | GET | `/Admin/AppPermission/All` | List tat ca permissions | — | List permissions |
| 20 | GET | `/Admin/AppPermission/GroupTree` | Lay permission group tree | — | Tree structure |
| 21 | DELETE | `/Admin/AppPermission/{id}` | Soft-delete permission | — | Success |
| 22 | POST | `/Admin/AppAccess` | Gan account vao app (tao AppAccess) | `{ accountId, appCode, roleCode?, scope? }` | `{ id }` |
| 23 | PUT | `/Admin/AppAccess/{id}` | Cap nhat quyen truy cap | `{ roleCode?, scope? }` | Success |
| 24 | GET | `/Admin/AppAccess/{appCode}/{accountId}` | Lay AppAccess theo app + account | — | AppAccess detail |
| 25 | DELETE | `/Admin/AppAccess/{id}` | Soft-delete AppAccess | — | Success |
| 26 | GET | `/AppRole/All` | (Public) Lay danh sach roles | — | List roles |
| 27 | GET | `/AppPermission/All` | (Public) Lay danh sach permissions | — | List permissions |
| 28 | GET | `/AppPermission/GroupTree` | (Public) Permission group tree | — | Tree structure |

### Logic can luu y:
- **PUT /AppRole/{id}/Permission**: Xoa toan bo AppRolePermission cu cua role, insert lai theo list moi
- **POST /AppPermission/Setup**: Upsert — tim by appId + code, neu co thi update, chua co thi create
- **POST /AppAccess**: Tim App by code, tim Role by code + appId, tao AppAccess record

---

## MUC 3: NANG CAO — Co thi tot, chua can gap

| # | Method | Endpoint | Mo ta |
|---|--------|----------|-------|
| 29 | POST | `/Identity/Login/Google` | Dang nhap Google OAuth |
| 30 | POST | `/Identity/Login/Google/WithAccess` | Dang nhap Google + tu tao AppAccess |
| 31 | POST | `/Identity/Login/Facebook` | Dang nhap Facebook OAuth |
| 32 | POST | `/Identity/Login/Zalo` | Dang nhap Zalo |
| 33 | POST | `/Identity/Auth/Google` | Lay link dang nhap Google |
| 34 | POST | `/Identity/Auth/Facebook` | Lay link dang nhap Facebook |
| 35 | GET | `/Identity/ResetPassword/{id}` | Lay yeu cau reset password |
| 36 | POST | `/Identity/ResetPassword` | Tao yeu cau reset password |
| 37 | PUT | `/Identity/ResetPassword` | Dat lai password |
| 38 | POST | `/Admin/Account/CheckUsername` | Kiem tra username co ton tai |
| 39 | POST | `/Admin/Account/{id}/CheckCurrentPassword` | Xac nhan password hien tai |
| 40 | POST | `/Admin/Account/WithAccess` | Tao account kem AppAccess + Sites |
| 41 | POST | `/Admin/Account/All/ByScope` | Lay accounts theo permission scope |
| 42 | PUT | `/Identity/SetSite` | Cap nhat site session |
| 43 | PUT | `/Identity/SetField` | Cap nhat truong du lieu account |
| 44 | GET | `/Identity/CurrentSite` | Lay chi nhanh dang lam viec |
| 45 | GET | `/Identity/Field` | Lay truong du lieu cua app voi account |

### Entity bo sung can tao (neu implement muc 3):
- `Site` — Chi nhanh
- `AccountSite` — Account - Chi nhanh (phan quyen theo site)
- `AccountSiteSession` — Phien lam viec tai chi nhanh
- `AccountField` — Truong du lieu tu dinh nghia
- `ResetPasswordRequest` — Yeu cau reset mat khau
- `AppGoogleAccountState` / `AppFacebookAccountState` — OAuth state management

---

## BUGS CAN FIX TRUOC KHI IMPLEMENT TIEP

| # | File | Van de | Cach fix |
|---|------|--------|----------|
| 1 | `RefreshTokenRequest.java` | `@NotBlank` tren field `UUID` — khong hoat dong | Doi field thanh `String`, parse UUID trong service |
| 2 | `AccountLogoutJpaEntity.java` | Field `accountID` (viet hoa ID) — Spring Data query fail | Doi thanh `accountId` |
| 3 | `AccountLogoutRepositoryAdapter.java` | `deleteByAccountID()` body rong, `save()` return null | Implement goi `jpaRepository` |
| 4 | `IdentityService.loginByPassword()` | Chua xoa AccountLogout flag khi login thanh cong | Inject `AccountLogoutRepository`, them `deleteByAccountID(account.getId())` sau `onSuccessLogin` |

---

## THU TU IMPLEMENT KHUYEN NGHI

```
1. Fix 4 bugs tren
2. Muc 1: Account CRUD (POST, GET, GET/{id}, PUT/Password, PUT/Auth, PUT/Unlock, DELETE)
3. Muc 2: App + AppRole + AppPermission + AppAccess CRUD
4. Muc 3: OAuth login, reset password, site management (khi can)
```

---

## GHI CHU CHUNG

- Tat ca Admin endpoints yeu cau: `@PreAuthorize("hasAuthority('admin')")` + `@SecurityRequirement(name = "bearerAuth")`
- Tat ca write operations nen co `@Transactional`
- Soft-delete: set `deletedAt = LocalDateTime.now()`, KHONG xoa record thuc
- Pagination: dung Spring `Pageable` + `Page<T>` response
- Validation: dung `@Valid` + Jakarta Bean Validation annotations tren request DTOs
