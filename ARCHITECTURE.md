# EggstechIdentity.API - Tài liệu Kiến trúc Hệ thống

> Tài liệu này mô tả toàn bộ kiến trúc, chức năng và kỹ thuật xây dựng hệ thống phân quyền Identity API.
> Dành cho người mới tiếp cận source code - giải thích từ tổng quan đến chi tiết.

---

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Kiến trúc phân lớp (Clean Architecture)](#2-kiến-trúc-phân-lớp-clean-architecture)
3. [Cấu trúc thư mục](#3-cấu-trúc-thư-mục)
4. [Database Schema & Quan hệ giữa các Entity](#4-database-schema--quan-hệ-giữa-các-entity)
5. [Hệ thống Authentication (Xác thực)](#5-hệ-thống-authentication-xác-thực)
6. [Hệ thống Authorization (Phân quyền)](#6-hệ-thống-authorization-phân-quyền)
7. [Scope - Cơ chế phân quyền linh hoạt](#7-scope---cơ-chế-phân-quyền-linh-hoạt)
8. [JWT Token - Cấu trúc và vòng đời](#8-jwt-token---cấu-trúc-và-vòng-đời)
9. [Multi-tenant theo App](#9-multi-tenant-theo-app)
10. [Phân quyền 2 cấp: App-level & Site-level](#10-phân-quyền-2-cấp-app-level--site-level)
11. [OAuth Integration (Google, Facebook, Zalo)](#11-oauth-integration-google-facebook-zalo)
12. [Bảo mật & Chống tấn công](#12-bảo-mật--chống-tấn-công)
13. [Background Jobs & Maintenance](#13-background-jobs--maintenance)
14. [Kỹ thuật nổi bật & Design Patterns](#14-kỹ-thuật-nổi-bật--design-patterns)
15. [Hướng dẫn cho Developer mới](#15-hướng-dẫn-cho-developer-mới)

---

## 1. Tổng quan hệ thống

**EggstechIdentity.API** là một hệ thống **Identity & Access Management (IAM)** được xây dựng để:

- Quản lý tài khoản người dùng (đăng ký, đăng nhập, mật khẩu)
- Xác thực đa phương thức (password, Google, Facebook, Zalo)
- Phân quyền linh hoạt với hệ thống Role + Permission + Scope
- Phục vụ nhiều ứng dụng (multi-tenant) - mỗi app có role/permission riêng
- Hỗ trợ phân quyền theo chi nhánh (site/branch)

**Tech Stack:**
| Thành phần | Công nghệ |
|------------|-----------|
| Framework | ASP.NET Core (.NET) |
| Database | PostgreSQL (EF Core + Npgsql) |
| DI Container | Autofac |
| Authentication | JWT (HMAC-SHA256) |
| Background Jobs | Hangfire |
| API Documentation | ReDoc / Swagger |
| Base Framework | TripleSix.Core (custom) |

---

## 2. Kiến trúc phân lớp (Clean Architecture)

Hệ thống tuân theo **Clean Architecture** với 4 lớp, dependency flow từ ngoài vào trong:

```
┌─────────────────────────────────────────────────────────┐
│                      WebApi Layer                         │
│  (Controllers, Startup, Middleware, DI Registration)      │
├─────────────────────────────────────────────────────────┤
│                   Application Layer                       │
│  (Services, DTOs, Helpers, Request Clients)               │
├─────────────────────────────────────────────────────────┤
│                     Domain Layer                          │
│  (Entities, Interfaces, Constants, Exceptions, Identity)  │
├─────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                     │
│  (DbContext, Migrations, Data Seeds)                      │
└─────────────────────────────────────────────────────────┘
```

**Nguyên tắc dependency:**
- `WebApi` → phụ thuộc `Application`, `Domain`, `Infrastructure`
- `Application` → phụ thuộc `Domain`
- `Infrastructure` → phụ thuộc `Domain` (implement interface)
- `Domain` → **KHÔNG phụ thuộc ai** (lớp lõi, thuần logic)

**Tại sao dùng Clean Architecture?**
- Tách biệt business logic khỏi framework/database
- Dễ test unit cho từng layer
- Thay đổi database (ví dụ từ PostgreSQL → SQL Server) chỉ ảnh hưởng Infrastructure
- Thay đổi API framework không ảnh hưởng logic nghiệp vụ

---

## 3. Cấu trúc thư mục

```
identity.api/
├── Identity.sln                         # Solution file
├── Document/Diagram.mdj                 # DB diagram
└── Src/
    ├── Domain/                          # ★ LỚP LÕI - Business entities
    │   ├── Constants/                   # Enum/constants (AccountAuthFieldTypes, AppPermissionTypes)
    │   ├── DataContext/                 # Interface IApplicationDbContext
    │   ├── Entities/                    # 16 entities (Account, App, AppRole, AppPermission, ...)
    │   ├── Exceptions/                  # Custom exceptions (LoginInvalid, SessionInvalid, ...)
    │   ├── Identity/                    # IdentityContext - parse JWT claims
    │   └── Types/                       # OAuth DTOs (Google, Facebook, Zalo response types)
    │
    ├── Application/                     # ★ LOGIC NGHIỆP VỤ - Services
    │   ├── Dto/                         # Data Transfer Objects
    │   │   ├── Admins/                  # Admin CRUD DTOs (Account, AppRole, AppPermission, ...)
    │   │   ├── Headers/                 # Request header DTOs
    │   │   └── Identity/               # Login/Token/Profile DTOs
    │   ├── Helpers/                     # Utility classes
    │   │   ├── PasswordHelper.cs        # MD5 hash password
    │   │   ├── ScopeHelper.cs           # ★ QUAN TRỌNG: Xử lý scope (+/- permission)
    │   │   └── AppPermissionGroupHelper.cs  # Parse permission group tree
    │   ├── RequestClients/             # HTTP clients cho OAuth providers
    │   └── Services/                   # ★ Business services (12 services)
    │       ├── IdentityService.cs       # ★ CORE: Login, Token, Refresh
    │       ├── AppPermissionService.cs  # Permission CRUD & scope expansion
    │       ├── AppRoleService.cs        # Role CRUD & permission assignment
    │       └── ...
    │
    ├── Infrastructure/                  # ★ DATA ACCESS
    │   ├── DataContext/                 # EF Core DbContext (PostgreSQL)
    │   ├── DataSeeds/                   # Seed data (root account, IDENTITY app)
    │   └── Migrations/                  # 15+ migrations (2024-07 đến 2026-06)
    │
    └── WebApi/                          # ★ PRESENTATION LAYER
        ├── Program.cs                   # Entry point
        ├── Startup.cs                   # ★ DI, JWT config, middleware pipeline
        ├── Abstracts/                   # Base controllers (AdminController, AppController, ...)
        ├── Controllers/
        │   ├── Admins/                  # Protected admin endpoints
        │   └── Commons/                 # Public endpoints (Login, RefreshToken, ...)
        └── Hangfire/                    # Background job scheduling
```

---

## 4. Database Schema & Quan hệ giữa các Entity

### Sơ đồ quan hệ

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Account    │────1:N──│  AccountAuth  │         │   Setting    │
│              │         │ (login methods)│         │ (key-value)  │
│ - Name       │         │ - Field (type) │         └──────────────┘
│ - SecretKey  │         │ - Value        │
│ - Password   │         └──────────────┘
│ - GoogleSubId│
│ - FacebookId │         ┌──────────────┐
│ - ZaloId     │────1:N──│  AppAccess    │──N:1──┐
└──────┬───────┘         │ (app-level)  │        │
       │                 │ - Scope       │        │
       │                 │ - RoleId?     │        │
       │                 └──────────────┘        │
       │                                         │
       │    ┌──────────────┐                     │    ┌──────────────┐
       ├─1:N│ AccountSite  │──N:1──┐             ├────│     App      │
       │    │ (site-level) │       │             │    │              │
       │    │ - Scope      │       │             │    │ - Code       │
       │    │ - RoleId?    │       │             │    │ - SigningKey  │
       │    └──────────────┘       │             │    │ - TokenLife  │
       │                           │             │    │ - SessionLife│
       │    ┌──────────────┐       │             │    │ - HasBranch  │
       ├─1:N│AccountSession│       │             │    └──────┬───────┘
       │    │ (refresh tok)│       │             │           │
       │    │ - ExpiredAt  │       │             │           │
       │    └──────────────┘       │             │    ┌──────┴───────┐
       │                           │             │    │              │
       │    ┌──────────────┐       │             │    │              │
       └─1:N│AccountLogout │       │             │    │              │
            │ (force logout)│      │             │    ▼              ▼
            └──────────────┘       │       ┌──────────┐     ┌──────────────┐
                                   │       │  AppRole  │     │AppPermission │
                                   │       │           │     │              │
                                   │       │ - Code    │     │ - Code       │
                                   ▼       │ - Name    │     │ - Group      │
                             ┌──────────┐  └─────┬────┘     │ - Type       │
                             │   Site   │        │           │ - Include... │
                             │          │        │           └──────┬───────┘
                             │ - Code   │        │                  │
                             │ - Name   │        └────N:M───────────┘
                             │ - AppId  │         (AppRolePermissionItem)
                             └──────────┘
```

### Giải thích các Entity chính

| Entity | Vai trò | Ghi chú |
|--------|---------|---------|
| **Account** | Tài khoản người dùng | Chứa thông tin cá nhân, social login IDs, SecretKey cho hash password |
| **AccountAuth** | Phương thức đăng nhập | 1 account có thể đăng nhập bằng username, email, hoặc phone |
| **App** | Ứng dụng | Mỗi app có SigningKey riêng, cấu hình OAuth, lifetime settings |
| **AppRole** | Vai trò/chức vụ | Thuộc về 1 App, chứa danh sách permissions |
| **AppPermission** | Quyền chức năng | Mã quyền (VD: `order.create`), có thể kế thừa (include) quyền khác |
| **AppAccess** | Quyền truy cập App | Gắn Account ↔ App + Role + Scope (cấp app) |
| **AccountSite** | Quyền truy cập Site | Gắn Account ↔ Site + Role + Scope (cấp chi nhánh) |
| **Site** | Chi nhánh | Thuộc về 1 App, dùng cho phân quyền theo branch |
| **AccountSession** | Phiên đăng nhập | Refresh token = Session ID, có ExpiredAt |
| **AccountLogout** | Force logout | Admin buộc user logout |
| **Setting** | Cấu hình hệ thống | Key-value (max login sai, thời gian chờ, email config) |

---

## 5. Hệ thống Authentication (Xác thực)

### 5.1 Đăng nhập bằng Password

```
Client                  IdentityController           IdentityService              Database
  │                           │                           │                          │
  │── POST /Login/Password ──▶│                           │                          │
  │   {username, password}    │── LoginByPassword() ────▶│                          │
  │                           │                           │── Tìm AccountAuth ──────▶│
  │                           │                           │◀─ Account + SecretKey ───│
  │                           │                           │                          │
  │                           │                           │── MD5(password + secret) │
  │                           │                           │── So sánh hash ──────────│
  │                           │                           │                          │
  │                           │                           │── Check brute force ─────│
  │                           │                           │── Check account active ──│
  │                           │                           │── Check app access ──────│
  │                           │                           │                          │
  │                           │                           │── Tạo/Extend Session ───▶│
  │                           │                           │── Generate JWT ──────────│
  │                           │                           │                          │
  │◀── {AccessToken, RefreshToken, ...} ─────────────────│                          │
```

**Chi tiết flow:**
1. Client gửi `username/email/phone` + `password` + `AppCode` (header)
2. Tìm `AccountAuth` theo field + value → lấy ra `Account`
3. Hash password: `MD5(password + account.SecretKey)` → so sánh với `Account.Password`
4. Kiểm tra brute force: nếu `WrongLoginCount >= MaxAllowed` → tính thời gian bị khóa
5. Kiểm tra `Account.DeleteAt` (soft delete = inactive)
6. Kiểm tra `AppAccess` tồn tại cho account + app
7. Tạo `AccountSession` (hoặc extend nếu session cũ còn sống)
8. Generate JWT access token với đầy đủ claims
9. Trả về `{AccessToken, RefreshToken, Scope, RoleCode, ...}`

### 5.2 Đăng nhập bằng OAuth (Google/Facebook/Zalo)

```
Client          Identity API         OAuth Provider
  │                  │                      │
  │── GET Auth URL ─▶│                      │
  │◀── URL + State ──│── Save State to DB ─▶│
  │                  │                      │
  │── Redirect to OAuth Provider ──────────▶│
  │◀── Authorization Code ─────────────────│
  │                  │                      │
  │── POST Login ───▶│── Exchange Code ────▶│
  │                  │◀── Access Token ─────│
  │                  │── Get User Info ────▶│
  │                  │◀── Profile Data ─────│
  │                  │                      │
  │                  │── Find/Create Account │
  │                  │── Generate JWT        │
  │◀── Token ────────│                      │
```

**State Management:**
- Mỗi OAuth request tạo 1 record `AppGoogleAccountState` / `AppFacebookAccountState`
- State có thời gian sống (configurable per App)
- Khi callback về, validate state tồn tại và chưa hết hạn
- Hangfire job dọn dẹp state hết hạn hàng ngày

### 5.3 Hash Password

```csharp
// PasswordHelper.cs
public static string HashPassword(string password, string secretKey)
{
    var data = MD5.HashData(Encoding.UTF8.GetBytes(password + secretKey));
    // Trả về chuỗi hex (32 chars)
}
```

- Mỗi Account có `SecretKey` riêng (unique salt)
- Password được hash = `MD5(rawPassword + accountSecretKey)`
- Khi tạo account mới, SecretKey được generate random

---

## 6. Hệ thống Authorization (Phân quyền)

### 6.1 Mô hình RBAC mở rộng

Hệ thống sử dụng **RBAC (Role-Based Access Control)** với mở rộng:

```
Account ──has──▶ AppAccess ──has──▶ Role ──has──▶ Permissions
                     │
                     └──has──▶ Scope (override +/- permissions)
```

**Điểm khác biệt so với RBAC truyền thống:**
- Ngoài Role, mỗi account còn có **Scope** - cho phép thêm (+) hoặc bớt (-) permission
- Permission có **hierarchy** (cha-con) - gán permission cha tự động có quyền con
- Phân quyền **2 cấp**: App-level (toàn bộ app) và Site-level (theo chi nhánh)

### 6.2 Controller-level Authorization

```csharp
// AdminController.cs - Yêu cầu: đăng nhập + scope "admin" + issuer "IDENTITY"
[Authorize]
[RequireScope("admin")]
[RequireIssuer("IDENTITY")]
public abstract class AdminController : BaseController { }

// AppController.cs - Yêu cầu: đăng nhập (bất kỳ app nào)
[Authorize]
public abstract class AppController : BaseController { }

// CommonController.cs - Không yêu cầu đăng nhập
public abstract class CommonController : BaseController { }
```

**Giải thích attributes:**
- `[Authorize]` - ASP.NET Core built-in, yêu cầu JWT hợp lệ
- `[RequireScope("admin")]` - Custom attribute từ TripleSix.Core, check claim `scope` chứa "admin"
- `[RequireIssuer("IDENTITY")]` - Custom attribute, check JWT issuer = "IDENTITY"

### 6.3 Permission Types

```csharp
public enum AppPermissionTypes
{
    Global = 1,  // Quyền toàn hệ thống (chỉ xuất hiện trong app-level token)
    Site = 2,    // Quyền chi nhánh (chỉ xuất hiện trong site-level token)
    Both = 3     // Xuất hiện ở cả hai cấp
}
```

---

## 7. Scope - Cơ chế phân quyền linh hoạt

### 7.1 Scope là gì?

**Scope** là một chuỗi các mã permission, cách nhau bởi dấu cách, cho phép thêm/bớt quyền:

```
"admin order.create order.view -order.delete payment.view"
```

- `admin` → có quyền admin
- `order.create` → có quyền tạo đơn
- `-order.delete` → BỊ TỪ CHỐI quyền xóa đơn (dù role có)

### 7.2 Quy tắc giải quyết Scope

Khi generate JWT token, scope được tính theo thứ tự:

```
Bước 1: Lấy Scope từ AppAccess (hoặc AccountSite)
        VD: "admin order.create -order.delete"

Bước 2: Normalize - đếm +/- cho mỗi code
        "admin" → +1 → giữ
        "order.create" → +1 → giữ
        "-order.delete" → -1 → đánh dấu loại bỏ

Bước 3: Merge với Role permissions
        Role "manager" có: [order.create, order.view, order.delete, payment.view]
        Kết quả: order.create, order.view, order.delete, payment.view

Bước 4: Expand parent-child
        order.create includes → [order.view]
        Thêm: order.view (nếu chưa có)

Bước 5: Áp dụng negative overrides
        -order.delete → loại bỏ order.delete khỏi kết quả

Bước 6: Lọc theo Permission Type
        Nếu token app-level: chỉ giữ Global + Both
        Nếu token site-level: chỉ giữ Site + Both

KẾT QUẢ: "admin order.create order.view payment.view"
```

### 7.3 ScopeHelper - Code quan trọng nhất

File `Application/Helpers/ScopeHelper.cs` chứa toàn bộ logic scope:

| Method | Chức năng |
|--------|-----------|
| `NormalizeScope()` | Loại bỏ trùng lặp, tính net +/- cho mỗi code |
| `ToPersistedPermissionScope()` | Chuẩn hóa scope trước khi lưu DB (map đúng case) |
| `ExpandScopeWithIncludedPermissions()` | Expand permission cha → tự động thêm con |
| `ResolveMergedPermissionScope()` | ★ Merge role permissions + scope overrides → kết quả cuối |

### 7.4 Ví dụ thực tế

**Tình huống:** Account "Nguyễn Văn A" truy cập App "POS"

```
AppAccess:
  - RoleId: "manager" (role có permissions: pos.sale, pos.refund, pos.report, pos.setting)
  - Scope: "-pos.setting pos.export"

Kết quả token scope:
  - pos.sale ✓ (từ role)
  - pos.refund ✓ (từ role)
  - pos.report ✓ (từ role)
  - pos.setting ✗ (bị scope override bằng -)
  - pos.export ✓ (thêm bởi scope, dù role không có)
```

---

## 8. JWT Token - Cấu trúc và vòng đời

### 8.1 Cấu trúc Access Token

```json
{
  "iss": "POS",                              // App.Code - issuer
  "exp": 1719000000,                         // UTC + App.TokenLifetime (phút)
  "id": "a1b2c3d4-...",                      // Account.Id (GUID)
  "name": "Nguyễn Văn A",                    // Account.Name
  "avatarLink": "https://...",               // (optional)
  "googleEmail": "a@gmail.com",             // (optional)
  "phoneNumber": "0901234567",              // (optional)
  "roleCode": "manager",                    // AppRole.Code
  "roleName": "Quản lý",                   // AppRole.Name
  "lastSiteId": "site-guid-...",            // Site cuối cùng hoạt động
  "scope": "pos.sale pos.refund pos.report pos.export",  // ★ Danh sách quyền
  "sites": "[{\"siteId\":\"...\",\"roleCode\":\"cashier\",\"scope\":\"pos.sale\"}]"
  //         ↑ Chỉ có khi App.HasBranchPermissions = true
}
```

### 8.2 Dynamic Signing Key

Mỗi App có SigningKey riêng (128 ký tự). Khi validate token:

```csharp
// Startup.cs - resolve signing key dynamically
private static string? GetSigningKey(IdentityAppsetting setting, JwtSecurityToken token)
{
    // 1. Đọc token.Issuer (= App.Code)
    // 2. Query PostgreSQL: SELECT "SigningKey" FROM "App" WHERE "Code" = @AppCode
    // 3. Cache kết quả 1800 giây
    // 4. Return signing key để validate signature
}
```

**Tại sao dùng Dynamic Signing Key?**
- Mỗi app có key riêng → token app A không thể dùng cho app B
- Có thể rotate key mà không ảnh hưởng app khác
- Cache 30 phút để giảm DB query

### 8.3 Vòng đời Token

```
┌─────────────────────────────────────────────────────────────┐
│                      TOKEN LIFECYCLE                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Login ──▶ AccessToken (sống 10 phút, configurable)         │
│         └▶ RefreshToken = SessionId (sống 240 phút)         │
│                                                              │
│  Access Token hết hạn?                                       │
│  └── POST /RefreshToken {refreshToken, appCode}              │
│       ├── Validate session chưa expire                       │
│       ├── Extend session.ExpiredAt                           │
│       └── Generate access token MỚI                          │
│                                                              │
│  Session hết hạn?                                            │
│  └── User phải login lại                                     │
│                                                              │
│  Force Logout?                                               │
│  └── Admin tạo AccountLogout record                          │
│  └── Client check GET /ForceLogout → redirect login          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 8.4 Refresh Token Flow

```csharp
// Simplified logic
public async Task<TokenDataDto> RefreshToken(string refreshToken, string appCode)
{
    // 1. Tìm session by Id = refreshToken
    var session = await FindSession(refreshToken);

    // 2. Check session chưa expire
    if (session.ExpiredAt < DateTime.UtcNow) throw SessionInvalidException;

    // 3. Extend session lifetime
    session.ExpiredAt = DateTime.UtcNow + app.SessionLifetime;

    // 4. Generate new access token (claims mới nhất từ DB)
    var newAccessToken = GenerateAccessToken(account, app);

    return new TokenDataDto { AccessToken = newAccessToken, RefreshToken = session.Id };
}
```

---

## 9. Multi-tenant theo App

### 9.1 Khái niệm

Hệ thống Identity phục vụ **nhiều ứng dụng** (App) khác nhau. Mỗi App hoàn toàn độc lập:

```
Identity Service
├── App: "IDENTITY" (chính nó - quản lý admin)
├── App: "POS" (ứng dụng bán hàng)
├── App: "HRM" (quản lý nhân sự)
└── App: "CRM" (quản lý khách hàng)
```

### 9.2 Mỗi App có gì riêng?

| Thuộc tính | Ý nghĩa |
|-----------|---------|
| `Code` | Mã định danh duy nhất (= JWT Issuer) |
| `SigningKey` | 128-char HMAC key riêng |
| `TokenLifetime` | Thời gian sống access token (phút) |
| `SessionLifetime` | Thời gian sống session/refresh token (phút) |
| `GoogleAuthorizationClientId/Secret` | OAuth config riêng |
| `FacebookAuthorizationClientId/Secret` | OAuth config riêng |
| `HasBranchPermissions` | Có dùng phân quyền chi nhánh không |
| `Roles[]` | Danh sách vai trò riêng |
| `Permissions[]` | Danh sách quyền riêng |

### 9.3 Flow đăng nhập với App

```
Client gửi request với Header:
  x-app-code: "POS"    ← App nào muốn đăng nhập

Identity Service:
  1. Tìm App by Code = "POS"
  2. Tìm AppAccess(AccountId, AppId) → user có quyền truy cập app này?
  3. Generate token signed bằng App.SigningKey của "POS"
  4. Token có issuer = "POS"
```

---

## 10. Phân quyền 2 cấp: App-level & Site-level

### 10.1 Tại sao cần 2 cấp?

Trong thực tế, một hệ thống bán hàng có nhiều chi nhánh:
- **Quản lý vùng** → có quyền xem báo cáo TẤT CẢ chi nhánh (App-level)
- **Quản lý chi nhánh A** → chỉ có quyền quản lý chi nhánh A (Site-level)
- **Thu ngân chi nhánh B** → chỉ có quyền bán hàng tại chi nhánh B (Site-level)

### 10.2 Cấu trúc

```
Account "Nguyễn Văn A"
├── AppAccess (App-level)
│   ├── App: "POS"
│   ├── Role: "area-manager"
│   └── Scope: "report.view"          ← Quyền toàn app
│
└── AccountSite[] (Site-level)
    ├── Site: "Chi nhánh HCM"
    │   ├── Role: "manager"
    │   └── Scope: "pos.sale pos.refund pos.report"
    │
    └── Site: "Chi nhánh HN"
        ├── Role: "viewer"
        └── Scope: "pos.report"
```

### 10.3 JWT Token khi App có HasBranchPermissions = true

```json
{
  "scope": "report.view",                    // App-level scope (Global permissions)
  "sites": [                                  // Site-level scopes
    {
      "siteId": "guid-hcm",
      "roleCode": "manager",
      "scope": "pos.sale pos.refund pos.report"
    },
    {
      "siteId": "guid-hn",
      "roleCode": "viewer",
      "scope": "pos.report"
    }
  ]
}
```

### 10.4 Permission Type Filter

Khi tính scope cho mỗi cấp:
- **App-level scope**: chỉ lấy permissions có `Type = Global(1)` hoặc `Both(3)`
- **Site-level scope**: chỉ lấy permissions có `Type = Site(2)` hoặc `Both(3)`

---

## 11. OAuth Integration (Google, Facebook, Zalo)

### 11.1 Google OAuth Flow

```
1. Client gọi POST /Auth/Google {appCode, redirectUri}
2. Server tạo AppGoogleAccountState (state token + expiry) → lưu DB
3. Server trả về Google authorization URL (with state)
4. User redirect → Google → login → consent → redirect back với code + state
5. Client gọi POST /Login/Google {code, state, redirectUri, appCode}
6. Server validate state (tồn tại trong DB, chưa expire)
7. Server exchange code → Google access token
8. Server call Google userinfo API → lấy profile
9. Server tìm Account by GoogleSubId (hoặc tạo mới nếu chưa có)
10. Server generate JWT → trả cho client
```

### 11.2 Auto-provisioning

Khi user đăng nhập Google lần đầu:
- Account mới được tạo tự động
- `GoogleSubId`, `GoogleEmail`, `GoogleGivenName`, `GoogleFamilyName` được lưu
- `AppAccess` được tạo với scope mặc định (configurable)

### 11.3 Domain Restriction

App có thể cấu hình `GoogleAllowedDomain`:
```
GoogleAllowedDomain: "company.com,partner.com"
```
Chỉ email thuộc domain cho phép mới đăng nhập được.

---

## 12. Bảo mật & Chống tấn công

### 12.1 Brute Force Protection

```csharp
// Mỗi lần login sai:
account.WrongLoginCount++;

// Kiểm tra trước khi cho login:
if (account.WrongLoginCount >= maxWrongLoginAllowed)
{
    var waitMinutes = (account.WrongLoginCount - maxWrongLoginAllowed + 1) * waitMinutePerWrongLogin;
    account.AccessDeniedUntilAt = DateTime.UtcNow.AddMinutes(waitMinutes);
    // → Throw LoginAccessDeniedException
}

// Khi login thành công:
account.WrongLoginCount = 0;
account.AccessDeniedUntilAt = null;
```

**Config mặc định:**
- `MaxWrongLoginAllowed`: 3 lần
- `WaitMinutePerWrongLogin`: 5 phút
- Lần 4 sai → chờ 5 phút, lần 5 → chờ 10 phút, ...
- Admin có thể unlock qua API

### 12.2 Per-Account Salt

Mỗi account có `SecretKey` riêng:
- Password hash = `MD5(password + secretKey)`
- Nếu DB bị leak, không thể dùng rainbow table cho tất cả account
- Mỗi account cần brute-force riêng lẻ

### 12.3 OAuth State Protection

- State token lưu trong DB với thời gian hết hạn
- Ngăn CSRF attack: client phải gửi đúng state nhận từ server
- State bị xóa sau khi sử dụng hoặc khi hết hạn

### 12.4 Force Logout

Admin có thể buộc user logout:
```
POST /Admin/Account/{id}/ForceLogout → tạo AccountLogout record
Client check: GET /Identity/ForceLogout → nếu có record → phải logout
```

### 12.5 Token Security

- Access token ngắn hạn (mặc định 10 phút) → giảm thiểu rủi ro nếu bị lộ
- Refresh token dài hạn hơn (240 phút) nhưng chỉ dùng 1 lần rồi extend
- Per-app signing key → token app này không validate được ở app khác
- Dynamic key resolution with cache → có thể rotate key khi cần

---

## 13. Background Jobs & Maintenance

### Hangfire Jobs (chạy hàng ngày lúc 17:00 UTC)

| Job | Chức năng |
|-----|-----------|
| `HardDeleteAllExpired` (AccountSession) | Xóa vĩnh viễn các session hết hạn |
| `HardDeleteAllResetPasswordExpired` | Xóa các request reset password hết hạn |
| `HardDeleteAllGoogleAccountStateExpired` | Xóa các OAuth state hết hạn |

**Tại sao cần?**
- Session/state records tích lũy theo thời gian
- Expired records không còn giá trị nhưng chiếm DB space
- Hard delete (không phải soft delete) vì đây là data tạm thời

---

## 14. Kỹ thuật nổi bật & Design Patterns

### 14.1 Generic CRUD Endpoints (Attribute-based)

```csharp
// Thay vì viết 5 action methods cho mỗi entity, chỉ cần:
[AdminReadEndpoint<AppRoleService, AppRole, Guid, AppRoleDetailDto, AppRoleFilterDto>]
[AdminCreateEndpoint<AppRoleService, AppRole, AppRoleCreateDto>]
[AdminUpdateEndpoint<AppRoleService, AppRole, Guid, AppRoleUpdateDto>]
[AdminSoftDeleteEndpoint<AppRoleService, AppRole, Guid>]
public class AppRoleController : AdminController { }
```

Framework tự generate: List (paging), GetById, Create, Update, SoftDelete

### 14.2 Soft Delete Pattern

```csharp
// Entity có field DeleteAt
public DateTime? DeleteAt { get; set; }

// Query luôn dùng:
query.WhereNotDeleted()  // WHERE DeleteAt IS NULL

// Xóa = set DeleteAt:
entity.DeleteAt = DateTime.UtcNow;

// Khôi phục = set null:
entity.DeleteAt = null;
```

### 14.3 Permission Group Hierarchy (UI friendly)

Permissions được nhóm bằng chuỗi `Group` với separator `>>`:

```
Group: "Đơn hàng>>Quản lý>>Tạo mới"
       ↓ parse thành tree:
Đơn hàng
└── Quản lý
    └── Tạo mới
```

`AppPermissionGroupHelper` parse chuỗi này thành cây nested để hiển thị trên UI.

### 14.4 Permission Include (Parent-Child)

```json
// AppPermission: "order.manage"
{
  "Code": "order.manage",
  "IncludePermissionCodes": "[\"order.create\", \"order.view\", \"order.edit\"]"
}
```

Khi user được gán `order.manage` → tự động có `order.create`, `order.view`, `order.edit`

### 14.5 Autofac Module Pattern

Mỗi layer đăng ký dependencies riêng:

```csharp
// Domain/AutofacModule.cs
builder.RegisterType<IdentityContext>();
builder.RegisterAppsetting<IdentityAppsetting>(configuration);

// Application/AutofacModule.cs
builder.RegisterAllMapper(Assembly);       // Auto-scan và register mappers
builder.RegisterAllRepository(Assembly);   // Auto-scan repositories
builder.RegisterAllService(Assembly);      // Auto-scan services

// Infrastructure/AutofacModule.cs
builder.RegisterType<ApplicationDbContext>().As<IApplicationDbContext>();

// WebApi/AutofacModule.cs
builder.RegisterAllController(Assembly);   // Auto-scan controllers
```

### 14.6 IdentityContext (Claims Parser)

```csharp
public class IdentityContext : BaseIdentityContext
{
    // Parse JWT claims thành strongly-typed properties:
    public string Name { get; set; }        // claim "name"
    public string? RoleCode { get; set; }   // claim "roleCode"
    public string? LastSiteId { get; set; } // claim "lastSiteId"
    // ...

    // Inject vào service/controller để đọc thông tin user hiện tại
}
```

Trong controller/service:
```csharp
public IdentityContext IdentityContext { get; set; } // Autofac property injection

// Sử dụng:
var currentUserId = IdentityContext.Id;
var currentRole = IdentityContext.RoleCode;
```

---

## 15. Hướng dẫn cho Developer mới

### 15.1 Để hiểu hệ thống, đọc theo thứ tự:

1. **Domain/Entities/** → Hiểu data model (Account, App, AppRole, AppPermission, AppAccess)
2. **Domain/Constants/** → Hiểu các enum/constant quan trọng
3. **Application/Helpers/ScopeHelper.cs** → Hiểu cơ chế scope (★ file quan trọng nhất)
4. **Application/Services/IdentityService.cs** → Hiểu flow login + token generation
5. **WebApi/Startup.cs** → Hiểu cách wire up mọi thứ
6. **WebApi/Abstracts/** → Hiểu 3 cấp controller (Admin/App/Common)

### 15.2 Khi cần thêm Permission mới cho một App:

1. Tạo record `AppPermission` (Code, Name, Group, Type, AppId)
2. (Optional) Set `IncludePermissionCodes` nếu permission này bao gồm quyền con
3. Gắn permission vào Role: tạo `AppRolePermissionItem`
4. Hoặc gắn trực tiếp vào account qua `AppAccess.Scope`: `"+permission.code"`

### 15.3 Khi cần tạo App mới:

1. `INSERT App` với Code, SigningKey (128 chars), TokenLifetime, SessionLifetime
2. Định nghĩa `AppPermission[]` cho app
3. Định nghĩa `AppRole[]` và gắn permissions
4. Tạo `AppAccess` cho admin account với scope `"admin"`
5. (Optional) Tạo `Site[]` nếu cần phân quyền chi nhánh

### 15.4 Khi cần debug token:

1. Decode JWT tại jwt.io (KHÔNG cần signing key để đọc payload)
2. Check claim `iss` → biết App nào
3. Check claim `scope` → danh sách quyền
4. Check claim `exp` → thời gian hết hạn
5. Check claim `sites` → quyền theo chi nhánh (nếu có)

### 15.5 Các convention quan trọng:

- **Scope `"admin"`** = super permission, bypass mọi check
- **Soft delete** = set `DeleteAt`, KHÔNG xóa record
- **Header `x-app-code`** = xác định app đang request
- **RefreshToken = Session GUID** (không phải JWT)
- **Password hash** = MD5, KHÔNG PHẢI bcrypt/argon2 (legacy, nên cân nhắc upgrade)

### 15.6 Seed Data mặc định:

| Data | Giá trị |
|------|---------|
| Root Account | username: `root`, password: `root` |
| Identity App | code: `IDENTITY`, token: 10 phút, session: 240 phút |
| Root Access | scope: `"admin"` |

---

## Sơ đồ tổng hợp: Flow xác thực & phân quyền

```
                            ┌─────────────────┐
                            │   Client App    │
                            └────────┬────────┘
                                     │
                         POST /Login/Password
                         Header: x-app-code: "POS"
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                        IDENTITY API                              │
│                                                                  │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────┐    │
│  │ Validate    │───▶│ Check Access │───▶│ Resolve Scope   │    │
│  │ Credentials │    │ (AppAccess)  │    │ (Role + Override)│    │
│  └─────────────┘    └──────────────┘    └────────┬────────┘    │
│                                                   │              │
│  ┌─────────────┐    ┌──────────────┐             │              │
│  │ Brute Force │    │ Site Perms   │◀────────────┘              │
│  │ Check       │    │ (if branch)  │                            │
│  └─────────────┘    └──────┬───────┘                            │
│                            │                                     │
│                            ▼                                     │
│              ┌──────────────────────────┐                       │
│              │    Generate JWT Token     │                       │
│              │  (signed with App key)   │                       │
│              └──────────────────────────┘                       │
│                            │                                     │
└────────────────────────────┼─────────────────────────────────────┘
                             │
                             ▼
                   {AccessToken, RefreshToken}
                             │
                             ▼
              ┌──────────────────────────────┐
              │        Target App (POS)       │
              │                               │
              │  Validate JWT:                │
              │  1. Verify signature          │
              │     (SigningKey from DB)       │
              │  2. Check expiry              │
              │  3. Read scope claim          │
              │  4. Authorize endpoint        │
              │     [RequireScope("pos.sale")]│
              └──────────────────────────────┘
```

---

> **Lưu ý cuối:** Hệ thống này được thiết kế để phục vụ nhiều ứng dụng khác nhau từ một Identity Service duy nhất. Mọi app đăng ký vào hệ thống đều có bộ Role/Permission riêng biệt, signing key riêng, và có thể cấu hình OAuth providers riêng. Đây là mô hình phổ biến trong kiến trúc microservices.
