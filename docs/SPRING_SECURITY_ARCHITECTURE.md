# Spring Security Architecture: Từ Servlet Container đến Controller

> Tài liệu giải thích chi tiết kiến trúc Spring Security, bắt đầu từ nền tảng Servlet Container,
> Front Controller pattern, và chuỗi Security Filter trước khi request tới được Controller.

---

## Table of Contents

| # | Section | Mô tả |
|---|---------|-------|
| 1 | [Servlet Container](#1-servlet-container--nền-tảng-thực-sự-chạy-ứng-dụng-java-web) | Tomcat, Servlet, Filter — nền tảng dưới cùng |
| 2 | [DispatcherServlet — Front Controller](#2-dispatcherservlet--front-controller-pattern) | Front Controller pattern trong Spring MVC |
| 3 | [Spring Security Filter Chain](#3-spring-security-filter-chain--chi-tiết-từng-filter) | 3 tầng delegation + danh sách 15 filters |
| 4 | [Full Request Lifecycle](#4-full-request-lifecycle--từ-byte-đến-controller) | Diagram đầy đủ từ TCP byte → Controller |
| 5 | [Khi JWT Filter Reject](#5-khi-jwt-filter-reject--request-không-tới-controller) | Demo request bị chặn, controller không thấy |
| 6 | [Cho JWT App — Cần vs Không Cần](#6-cho-jwt-app--những-gì-cần-và-không-cần) | SecurityConfig thực tế cho JWT stateless |
| 7 | [SecurityContextHolder](#7-securitycontextholder--nơi-lưu-user-hiện-tại) | ThreadLocal pattern, đọc user ở mọi nơi |
| 8 | [Tóm tắt Mapping Concepts](#8-tóm-tắt-mapping-concepts) | Bảng tra nhanh tất cả concepts |
| 9 | [Đọc tiếp](#9-đọc-tiếp) | Official docs references |

---

## 1. Servlet Container — Nền tảng thực sự chạy ứng dụng Java Web

### 1.1 Servlet Container là gì?

**Servlet Container** (hay Servlet Engine) là một chương trình chạy phía server, chịu trách nhiệm:
- Lắng nghe HTTP request từ client (trên port 8080, 443, etc.)
- Tạo object `HttpServletRequest` và `HttpServletResponse` từ raw HTTP bytes
- Gọi đúng `Servlet` hoặc `Filter` để xử lý request
- Quản lý lifecycle của Servlet (init, service, destroy)

**Ví dụ Servlet Container:** Apache Tomcat, Jetty, Undertow.

Khi bạn chạy Spring Boot, nó **nhúng (embed) Tomcat** vào trong ứng dụng. Bạn không cần deploy WAR file ra ngoài — Tomcat chạy bên trong cùng JVM.

```
┌─────────────────────────────────────────────────────────────┐
│                     JVM Process                              │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Embedded Tomcat (Servlet Container)       │  │
│  │                                                       │  │
│  │   ┌─────────────────────────────────────────────────┐ │  │
│  │   │          Servlet FilterChain                     │ │  │
│  │   │                                                  │ │  │
│  │   │  Filter 1 → Filter 2 → ... → DispatcherServlet  │ │  │
│  │   └─────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Spring ApplicationContext                 │  │
│  │   (Beans: Controllers, Services, Repositories, ...)   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Servlet là gì?

**Servlet** là một Java class implement interface `jakarta.servlet.Servlet`. Nó nhận HTTP request và trả HTTP response. Đây là đơn vị xử lý cơ bản nhất của Java web app.

```java
// Servlet interface cơ bản (simplified)
public interface Servlet {
    void init(ServletConfig config);
    void service(ServletRequest req, ServletResponse res);  // xử lý request
    void destroy();
}
```

Trong Spring MVC, bạn KHÔNG bao giờ viết Servlet trực tiếp. Thay vào đó, Spring cung cấp **một Servlet duy nhất** xử lý TẤT CẢ request: `DispatcherServlet`.

### 1.3 Filter là gì?

**Filter** là một Java class implement interface `jakarta.servlet.Filter`. Nó nằm TRƯỚC Servlet trong pipeline, có thể:
- Chặn request (không cho tới Servlet)
- Sửa đổi request/response
- Cho request đi tiếp (`chain.doFilter()`)

```java
public interface Filter {
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException;
}
```

```java
// Ví dụ một filter đơn giản
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        System.out.println(">>> Request đến: " + ((HttpServletRequest) request).getRequestURI());
        
        chain.doFilter(request, response);  // cho request đi tiếp
        
        System.out.println("<<< Response trả về: " + ((HttpServletResponse) response).getStatus());
    }
}
```

**Quan trọng:** `chain.doFilter()` là lệnh "cho đi tiếp". Nếu filter KHÔNG gọi `chain.doFilter()`, request bị CHẶN tại đây — Servlet (controller) sẽ KHÔNG BAO GIỜ nhận được request.

---

## 2. DispatcherServlet — Front Controller Pattern

### 2.1 Front Controller Design Pattern là gì?

**Front Controller** là một design pattern trong đó:
- **Một single entry point** (một controller duy nhất) nhận TẤT CẢ request
- Entry point này phân phối (dispatch) request tới handler phù hợp
- Tránh duplicate logic (auth check, logging, etc.) ở mỗi handler

```
Không có Front Controller:               Có Front Controller:
                                          
Client → /orders → OrderServlet           Client → /orders ─┐
Client → /users  → UserServlet                               ├──→ DispatcherServlet → OrderController
Client → /auth   → AuthServlet            Client → /users  ─┤                      → UserController
                                          Client → /auth   ─┘                      → AuthController
(mỗi Servlet tự check auth,                      (DispatcherServlet check auth 1 lần,
 tự parse request, duplicate)                      rồi route tới đúng controller)
```

### 2.2 DispatcherServlet = Front Controller của Spring MVC

**Đúng rồi** — `DispatcherServlet` chính là implementation của Front Controller pattern trong Spring.

Nó là **Servlet duy nhất** được đăng ký với Tomcat, mapped tới URL pattern `/` (tất cả request). Khi Tomcat nhận request, nó luôn giao cho `DispatcherServlet` xử lý.

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DispatcherServlet (Front Controller)              │
│                                                                     │
│  1. Nhận HttpServletRequest                                         │
│  2. Hỏi HandlerMapping: "URL /api/admin/accounts → handler nào?"   │
│  3. HandlerMapping trả: AccountAdminController.getAll()             │
│  4. Gọi HandlerAdapter để invoke method                             │
│  5. Controller return ResponseEntity/Object                         │
│  6. MessageConverter serialize → JSON                               │
│  7. Ghi vào HttpServletResponse                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.3 Vị trí trong pipeline

```
Client HTTP Request
       │
       ▼
┌──────────────────────────────────────────────────────────────────┐
│  Tomcat (Servlet Container)                                      │
│                                                                  │
│  FilterChain:                                                    │
│    ┌──────────────────────────────────────────────────────────┐  │
│    │ Filter 1: CharacterEncodingFilter (set UTF-8)            │  │
│    │     │                                                    │  │
│    │     ▼                                                    │  │
│    │ Filter 2: DelegatingFilterProxy (Spring Security)        │  │
│    │     │         └──→ FilterChainProxy                      │  │
│    │     │                  └──→ SecurityFilterChain           │  │
│    │     │                        (15+ security filters)      │  │
│    │     ▼                                                    │  │
│    │ Filter 3: ... (other filters)                            │  │
│    │     │                                                    │  │
│    │     ▼                                                    │  │
│    │ Servlet: DispatcherServlet (Front Controller)             │  │
│    │     │                                                    │  │
│    │     ├──→ HandlerMapping (tìm controller)                 │  │
│    │     ├──→ Controller method (xử lý business logic)        │  │
│    │     └──→ Response (JSON)                                 │  │
│    └──────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

**Thứ tự rõ ràng:**
1. Tomcat nhận raw HTTP bytes → tạo `HttpServletRequest`
2. Tomcat gọi FilterChain (bao gồm Spring Security filters)
3. Nếu Security filters cho phép → request tới `DispatcherServlet`
4. `DispatcherServlet` route tới Controller
5. Controller xử lý, trả response ngược lại qua filter chain

**Kết luận:** Spring Security filters chạy TRƯỚC `DispatcherServlet`. Nếu security filter reject request (trả 401/403), `DispatcherServlet` và Controller **KHÔNG BAO GIỜ** thấy request đó.

---

## 3. Spring Security Filter Chain — Chi tiết từng filter

### 3.1 Cách Spring Security "chui vào" Servlet Container

Servlet Container (Tomcat) chỉ biết Servlet Filter — nó không biết Spring Bean. Vấn đề: Spring Security là Spring Bean, nhưng cần chạy như Servlet Filter.

Giải pháp: **3 tầng delegation**

```
Tomcat FilterChain
  │
  ├── ... (other Servlet filters)
  │
  ├── DelegatingFilterProxy                    ← Servlet Filter (Tomcat biết)
  │         │
  │         │  delegate tới Spring Bean có tên "springSecurityFilterChain"
  │         ▼
  │   FilterChainProxy                         ← Spring Bean (Spring quản lý)
  │         │
  │         │  chọn SecurityFilterChain phù hợp theo URL
  │         ▼
  │   SecurityFilterChain                      ← Danh sách Security Filters
  │     ├── Filter 1
  │     ├── Filter 2
  │     ├── ...
  │     └── Filter N
  │
  ├── ... (other Servlet filters)
  │
  └── DispatcherServlet
```

**Tại sao cần 3 tầng?**

| Tầng | Lý do tồn tại |
|------|----------------|
| `DelegatingFilterProxy` | Tomcat cần đăng ký filter TRƯỚC Spring context khởi tạo. Filter này "lazy load" — đợi Spring ready rồi mới delegate. |
| `FilterChainProxy` | Cho phép có NHIỀU `SecurityFilterChain` (VD: `/api/**` dùng JWT, `/web/**` dùng session). Nó chọn chain đầu tiên match. |
| `SecurityFilterChain` | Danh sách actual security filters chạy theo thứ tự. |

### 3.2 SecurityFilterChain — Danh sách đầy đủ các filter

Khi Spring Security khởi tạo, nó log ra danh sách filters (ở DEBUG level). Đây là danh sách đầy đủ cho một ứng dụng web thông thường:

```
DefaultSecurityFilterChain: Will secure any request with [
  1.  DisableEncodeUrlFilter
  2.  WebAsyncManagerIntegrationFilter
  3.  SecurityContextHolderFilter
  4.  HeaderWriterFilter
  5.  CsrfFilter
  6.  LogoutFilter
  7.  UsernamePasswordAuthenticationFilter     ← (hoặc custom JWT filter)
  8.  DefaultLoginPageGeneratingFilter
  9.  DefaultLogoutPageGeneratingFilter
  10. BasicAuthenticationFilter
  11. RequestCacheAwareFilter
  12. SecurityContextHolderAwareRequestFilter
  13. AnonymousAuthenticationFilter
  14. ExceptionTranslationFilter
  15. AuthorizationFilter
]
```

### 3.3 Chi tiết từng filter (theo thứ tự thực thi)

#### Filter 1: `DisableEncodeUrlFilter`
**Nhiệm vụ:** Ngăn Servlet container encode session ID vào URL (VD: `/page;jsessionid=ABC123`).
**Tại sao:** Để tránh leak session ID qua URL (security risk).
**Với JWT app:** Không ảnh hưởng, nhưng vẫn có mặt.

#### Filter 2: `WebAsyncManagerIntegrationFilter`
**Nhiệm vụ:** Đảm bảo SecurityContext được propagate khi dùng async request (VD: `Callable`, `DeferredResult`).
**Tại sao:** SecurityContext là ThreadLocal — khi Spring MVC chuyển sang thread khác để xử lý async, filter này copy context sang thread mới.
**Với JWT app:** Cần nếu bạn dùng async controller.

#### Filter 3: `SecurityContextHolderFilter`
**Nhiệm vụ:** Load SecurityContext từ `SecurityContextRepository` (thường là HttpSession) vào `SecurityContextHolder` ở đầu request, và clear khi request kết thúc.

```java
// Pseudo code
void doFilter(request, response, chain) {
    SecurityContext context = securityContextRepository.loadContext(request);
    SecurityContextHolder.setContext(context);
    try {
        chain.doFilter(request, response);
    } finally {
        SecurityContextHolder.clearContext();  // tránh memory leak (ThreadPool reuse thread)
    }
}
```

**Với JWT app (stateless):** SecurityContextRepository = `NullSecurityContextRepository` (không load gì từ session). Context trống cho tới khi JWT filter set vào.

#### Filter 4: `HeaderWriterFilter`
**Nhiệm vụ:** Thêm HTTP security headers vào response:
- `X-Content-Type-Options: nosniff` (chống MIME sniffing)
- `X-Frame-Options: DENY` (chống clickjacking)
- `X-XSS-Protection: 0` (disable browser XSS filter — đã deprecated)
- `Cache-Control: no-cache, no-store` (cho authenticated resources)
- `Strict-Transport-Security` (force HTTPS)

**Với JWT app:** Vẫn hữu ích cho API responses.

#### Filter 5: `CsrfFilter`
**Nhiệm vụ:** Bảo vệ chống Cross-Site Request Forgery.
- Với mỗi request không phải GET/HEAD/OPTIONS: yêu cầu CSRF token hợp lệ
- Token gắn trong session, client phải gửi lại qua header hoặc form field

```
POST /api/transfer HTTP/1.1
X-CSRF-TOKEN: abc123-def456     ← phải khớp với token trong session
```

**Với JWT app:** **DISABLE** — CSRF chỉ cần cho cookie/session-based auth. JWT bearer token tự thân đã là proof rằng client biết token, không cần CSRF.

#### Filter 6: `LogoutFilter`
**Nhiệm vụ:** Xử lý POST /logout — invalidate session, clear cookies, redirect.
**Với JWT app:** Ít dùng — logout = client xóa token ở phía client (hoặc bạn dùng session blacklist).

#### Filter 7: `UsernamePasswordAuthenticationFilter`
**Nhiệm vụ:** Xử lý form login (POST /login với `username` + `password` parameters).

```java
// Pseudo code
void doFilter(request, response, chain) {
    if (request matches POST /login) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication result = authenticationManager.authenticate(authRequest);
        
        SecurityContextHolder.getContext().setAuthentication(result);
        successHandler.onAuthenticationSuccess(request, response, result);
        return;  // không gọi chain.doFilter — redirect tới success URL
    }
    chain.doFilter(request, response);  // không phải login request → skip
}
```

**Với JWT app:** **KHÔNG DÙNG** — thay bằng custom `JwtAuthenticationFilter` ở vị trí này.

#### Filter 8-9: `DefaultLoginPageGeneratingFilter` / `DefaultLogoutPageGeneratingFilter`
**Nhiệm vụ:** Tự sinh trang HTML login/logout khi bạn chưa config custom page.
**Với JWT app:** **KHÔNG CẦN** — API không cần HTML page. Disable bằng `.formLogin(f -> f.disable())`.

#### Filter 10: `BasicAuthenticationFilter`
**Nhiệm vụ:** Xử lý HTTP Basic Auth (header `Authorization: Basic base64(user:pass)`).
**Với JWT app:** **KHÔNG DÙNG** — disable bằng `.httpBasic(b -> b.disable())`.

#### Filter 11: `RequestCacheAwareFilter`
**Nhiệm vụ:** Sau authentication thành công, replay request ban đầu (request bị intercept trước đó).
**Ví dụ:** User truy cập `/admin/dashboard` → bị redirect tới `/login` → login xong → redirect lại `/admin/dashboard`.
**Với JWT app:** Không cần — API client tự retry.

#### Filter 12: `SecurityContextHolderAwareRequestFilter`
**Nhiệm vụ:** Wrap `HttpServletRequest` để implement các method:
- `request.isUserInRole("ADMIN")` → check từ SecurityContext
- `request.getUserPrincipal()` → return Authentication object
- `request.getRemoteUser()` → return username

**Với JWT app:** Vẫn chạy, cho phép integration với Servlet API chuẩn.

#### Filter 13: `AnonymousAuthenticationFilter`
**Nhiệm vụ:** Nếu đến đây mà SecurityContext vẫn trống (không ai authenticate), set một `AnonymousAuthenticationToken`.

```java
void doFilter(request, response, chain) {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
        Authentication anon = new AnonymousAuthenticationToken("anonymous", "anonymousUser", 
                                                                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anon);
    }
    chain.doFilter(request, response);
}
```

**Tại sao:** Để AuthorizationFilter có thể phân biệt "chưa ai login" vs "không có Authentication". Một số rule cần check `isAnonymous()`.

#### Filter 14: `ExceptionTranslationFilter` ★ QUAN TRỌNG
**Nhiệm vụ:** Bắt exception từ filter phía sau (AuthorizationFilter) và convert thành HTTP response phù hợp.

```java
void doFilter(request, response, chain) {
    try {
        chain.doFilter(request, response);  // gọi AuthorizationFilter + DispatcherServlet
    } catch (AuthenticationException ex) {
        // User chưa login / token invalid
        authenticationEntryPoint.commence(request, response, ex);  // → 401
    } catch (AccessDeniedException ex) {
        if (isAnonymous(currentAuthentication)) {
            // Thực ra là chưa login, không phải "có login mà thiếu quyền"
            authenticationEntryPoint.commence(request, response, ex);  // → 401
        } else {
            // User đã login nhưng không đủ quyền
            accessDeniedHandler.handle(request, response, ex);  // → 403
        }
    }
}
```

**Với JWT app:** Custom `AuthenticationEntryPoint` và `AccessDeniedHandler` để trả JSON thay vì redirect.

#### Filter 15: `AuthorizationFilter` ★ QUAN TRỌNG
**Nhiệm vụ:** Kiểm tra request có được phép truy cập hay không, dựa trên rules bạn config:

```java
// Rules từ SecurityConfig:
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()          // ai cũng vào được
    .requestMatchers("/admin/**").hasAuthority("admin")  // cần authority "admin"
    .anyRequest().authenticated()                     // tối thiểu phải login
);
```

```java
// Pseudo code AuthorizationFilter
void doFilter(request, response, chain) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    AuthorizationDecision decision = authorizationManager.check(authentication, request);
    
    if (!decision.isGranted()) {
        throw new AccessDeniedException("Access denied");  
        // → bị ExceptionTranslationFilter bắt → 401 hoặc 403
    }
    
    chain.doFilter(request, response);  // → DispatcherServlet → Controller
}
```

---

## 4. Full Request Lifecycle — Từ byte đến Controller

```
Client gửi:
  POST /admin/accounts HTTP/1.1
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
  Content-Type: application/json
  {"name": "John"}

═══════════════════════════════════════════════════════════════════════════

TẦNG 1: NETWORK
  Tomcat nhận TCP bytes → parse HTTP protocol → tạo HttpServletRequest object

═══════════════════════════════════════════════════════════════════════════

TẦNG 2: SERVLET FILTER CHAIN
  
  ┌─ CharacterEncodingFilter ────────────────────────────────────────────┐
  │  Set request/response encoding = UTF-8                               │
  │  → chain.doFilter()                                                  │
  └──────────────────────────────────────────────────────────────────────┘
       │
       ▼
  ┌─ DelegatingFilterProxy ──────────────────────────────────────────────┐
  │  Lookup Spring Bean "springSecurityFilterChain" → delegate           │
  │                                                                      │
  │  ┌─ FilterChainProxy ─────────────────────────────────────────────┐  │
  │  │  Match URL /admin/accounts against SecurityFilterChain patterns │  │
  │  │  → Use SecurityFilterChain 0 (matches /**)                      │  │
  │  │                                                                 │  │
  │  │  ┌─ SecurityFilterChain ─────────────────────────────────────┐  │  │
  │  │  │                                                           │  │  │
  │  │  │  1. DisableEncodeUrlFilter → pass                         │  │  │
  │  │  │  2. WebAsyncManagerIntegrationFilter → pass                │  │  │
  │  │  │  3. SecurityContextHolderFilter                           │  │  │
  │  │  │     → Load context (empty for stateless)                  │  │  │
  │  │  │  4. HeaderWriterFilter → add security headers             │  │  │
  │  │  │  5. CsrfFilter → DISABLED (stateless)                    │  │  │
  │  │  │  6. LogoutFilter → not /logout, skip                     │  │  │
  │  │  │                                                           │  │  │
  │  │  │  7. ★ JwtAuthenticationFilter (CUSTOM)                    │  │  │
  │  │  │     a. Read header: "Bearer eyJhbG..."                    │  │  │
  │  │  │     b. Parse JWT (không validate) → read issuer "POS"     │  │  │
  │  │  │     c. Query DB: SELECT signing_key FROM app              │  │  │
  │  │  │        WHERE code = 'POS'                                 │  │  │
  │  │  │     d. Validate JWT signature với signing_key             │  │  │
  │  │  │     e. Validate expiry: exp > now ✓                       │  │  │
  │  │  │     f. Extract claims: {id, name, scope: "admin           │  │  │
  │  │  │        order.create order.view", roleCode: "manager"}     │  │  │
  │  │  │     g. Tạo Authentication object:                         │  │  │
  │  │  │        UsernamePasswordAuthenticationToken(                │  │  │
  │  │  │          principal = UserPrincipal{id, name, scope},      │  │  │
  │  │  │          credentials = null,                              │  │  │
  │  │  │          authorities = [admin, order.create, order.view]  │  │  │
  │  │  │        )                                                  │  │  │
  │  │  │     h. SecurityContextHolder.getContext()                 │  │  │
  │  │  │            .setAuthentication(authObject)                  │  │  │
  │  │  │     → chain.doFilter()                                    │  │  │
  │  │  │                                                           │  │  │
  │  │  │  8. RequestCacheAwareFilter → pass                        │  │  │
  │  │  │  9. SecurityContextHolderAwareRequestFilter → wrap        │  │  │
  │  │  │  10. AnonymousAuthenticationFilter                        │  │  │
  │  │  │      → Authentication != null → SKIP (đã có user)        │  │  │
  │  │  │                                                           │  │  │
  │  │  │  11. ExceptionTranslationFilter                           │  │  │
  │  │  │      try {                                                │  │  │
  │  │  │          chain.doFilter() → gọi AuthorizationFilter       │  │  │
  │  │  │      } catch (AccessDeniedException) → 403 JSON           │  │  │
  │  │  │                                                           │  │  │
  │  │  │  12. ★ AuthorizationFilter                                │  │  │
  │  │  │      → Check rule: /admin/** → authenticated? YES ✓       │  │  │
  │  │  │      → chain.doFilter()                                   │  │  │
  │  │  │                                                           │  │  │
  │  │  └───────────────────────────────────────────────────────────┘  │  │
  │  └─────────────────────────────────────────────────────────────────┘  │
  └──────────────────────────────────────────────────────────────────────┘
       │
       ▼
═══════════════════════════════════════════════════════════════════════════

TẦNG 3: DISPATCHER SERVLET (Front Controller)
  
  ┌─ DispatcherServlet ──────────────────────────────────────────────────┐
  │                                                                      │
  │  1. HandlerMapping: POST /admin/accounts                             │
  │     → AccountAdminController.createAccount()                         │
  │                                                                      │
  │  2. HandlerAdapter:                                                  │
  │     a. @RequestBody {"name":"John"} → deserialize → CreateAccountReq │
  │     b. @Valid → Bean Validation check                                │
  │     c. Invoke controller method                                      │
  │                                                                      │
  └──────────────────────────────────────────────────────────────────────┘
       │
       ▼
═══════════════════════════════════════════════════════════════════════════

TẦNG 4: CONTROLLER → SERVICE → REPOSITORY

  ┌─ AccountAdminController.createAccount(req) ──────────────────────────┐
  │                                                                      │
  │  // Lấy user hiện tại (đã được JWT filter set)                       │
  │  Authentication auth = SecurityContextHolder.getContext()             │
  │                            .getAuthentication();                      │
  │  // auth.getPrincipal() → {id: "uuid", name: "Admin", scope: "..."} │
  │                                                                      │
  │  // @RequireScope("admin") → AOP check scope chứa "admin" ✓         │
  │                                                                      │
  │  return accountService.createAccount(req);                           │
  └──────────────────────────────────────────────────────────────────────┘
       │
       ▼
  ┌─ Response path (ngược lại) ──────────────────────────────────────────┐
  │  Controller return ApiResponse{success: true, data: {id, name}}      │
  │  → Jackson serialize → JSON bytes                                    │
  │  → HeaderWriterFilter thêm security headers                          │
  │  → Tomcat gửi HTTP response bytes về client                          │
  └──────────────────────────────────────────────────────────────────────┘
```

---

## 5. Khi JWT Filter Reject — Request KHÔNG tới Controller

```
Client gửi request KHÔNG có token:
  GET /admin/accounts HTTP/1.1
  (không có Authorization header)

Security Filter Chain:
  ...
  7. JwtAuthenticationFilter:
     → Không có Authorization header
     → KHÔNG set Authentication
     → chain.doFilter() (cho đi tiếp, nhưng context trống)
  ...
  10. AnonymousAuthenticationFilter:
      → Authentication == null
      → Set AnonymousAuthenticationToken
  
  11. ExceptionTranslationFilter: try { chain.doFilter() }
  
  12. AuthorizationFilter:
      → Check: /admin/** requires authenticated
      → Current auth = AnonymousAuthenticationToken (không phải authenticated)
      → throw AccessDeniedException ← CHẶN TẠI ĐÂY
  
  11. ExceptionTranslationFilter: catch (AccessDeniedException)
      → isAnonymous? YES → gọi AuthenticationEntryPoint
      → Response: 401 {"success":false,"error":{"code":"UNAUTHORIZED"}}

  DispatcherServlet: KHÔNG BAO GIỜ ĐƯỢC GỌI
  Controller: KHÔNG BAO GIỜ THẤY REQUEST NÀY
```

---

## 6. Cho JWT App — Những gì CẦN và KHÔNG CẦN

### Filter chain sau khi config cho JWT stateless:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
    http
        .csrf(csrf -> csrf.disable())                              // bỏ CsrfFilter
        .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS)) // không tạo session
        .formLogin(form -> form.disable())                         // bỏ UsernamePasswordFilter + Login pages
        .httpBasic(basic -> basic.disable())                       // bỏ BasicAuthFilter
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)  // thêm JWT filter
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(customEntryPoint)             // trả 401 JSON
            .accessDeniedHandler(customDeniedHandler)               // trả 403 JSON
        );
    return http.build();
}
```

### Kết quả filter chain (simplified):

```
SecurityFilterChain cho JWT app:
  1. DisableEncodeUrlFilter         ← giữ (nhẹ, không ảnh hưởng)
  2. WebAsyncManagerIntegrationFilter ← giữ
  3. SecurityContextHolderFilter    ← giữ (clear context mỗi request)
  4. HeaderWriterFilter             ← giữ (security headers)
  5. [CsrfFilter]                   ← DISABLED
  6. LogoutFilter                   ← giữ (nhẹ, skip nếu không match)
  7. ★ JwtAuthenticationFilter      ← CUSTOM (thay UsernamePasswordFilter)
  8. [LoginPageFilter]              ← DISABLED
  9. [LogoutPageFilter]             ← DISABLED
  10. [BasicAuthFilter]             ← DISABLED
  11. RequestCacheAwareFilter       ← giữ
  12. SecurityContextHolderAwareRequestFilter ← giữ
  13. AnonymousAuthenticationFilter  ← giữ
  14. ExceptionTranslationFilter    ← giữ (custom handlers)
  15. AuthorizationFilter           ← giữ (check URL rules)
```

---

## 7. SecurityContextHolder — Nơi lưu "user hiện tại"

### ThreadLocal pattern

```java
// Simplified internal implementation
public class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();
    
    public static SecurityContext getContext() {
        SecurityContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = new SecurityContextImpl();
            contextHolder.set(ctx);
        }
        return ctx;
    }
    
    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }
    
    public static void clearContext() {
        contextHolder.remove();
    }
}
```

**ThreadLocal** = mỗi thread có bản copy riêng. Tomcat dùng thread pool — mỗi request được xử lý bởi 1 thread. Do đó:
- Request A trên Thread-1: SecurityContext chứa User A
- Request B trên Thread-2: SecurityContext chứa User B
- Chúng không xung đột nhau

**Quan trọng:** Phải `clearContext()` sau mỗi request (SecurityContextHolderFilter làm việc này), vì thread sẽ được reuse cho request khác.

### Đọc user ở bất kì đâu trong app

```java
// Trong Controller
@GetMapping("/me")
public ProfileResponse getProfile() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal user = (UserPrincipal) auth.getPrincipal();
    return new ProfileResponse(user.getId(), user.getName(), user.getScope());
}

// Trong Service
public Order createOrder(CreateOrderRequest req) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UUID currentUserId = ((UserPrincipal) auth.getPrincipal()).getId();
    // ... tạo order với currentUserId
}
```

---

## 8. Tóm tắt: Mapping concepts

| Khái niệm | Giải thích |
|------------|------------|
| **Servlet Container** | Tomcat — nhận HTTP, quản lý Filter + Servlet lifecycle |
| **Servlet** | Java class xử lý request. Trong Spring = `DispatcherServlet` duy nhất |
| **Filter** | Chạy TRƯỚC Servlet, có thể chặn/sửa request |
| **DispatcherServlet** | Front Controller — nhận mọi request, route tới đúng Controller |
| **DelegatingFilterProxy** | Cầu nối Tomcat ↔ Spring Bean |
| **FilterChainProxy** | Spring Bean quản lý nhiều SecurityFilterChain |
| **SecurityFilterChain** | Danh sách Security Filters cho một URL pattern |
| **JwtAuthenticationFilter** | Custom filter: validate JWT → set SecurityContext |
| **AuthorizationFilter** | Check user có quyền access URL không |
| **ExceptionTranslationFilter** | Bắt exception → trả 401/403 |
| **SecurityContextHolder** | ThreadLocal lưu thông tin user đã authenticated |
| **Authentication** | Object chứa principal + authorities (user info + permissions) |

---

## 9. Đọc tiếp

- [Spring Security Architecture (official)](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)
- [Authorization Architecture](https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html)
