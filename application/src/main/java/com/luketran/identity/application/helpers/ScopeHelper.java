package com.luketran.identity.application.helpers;

import com.luketran.identity.domain.entities.AppPermission;
import com.luketran.identity.domain.entities.AppRolePermission;

import java.util.*;

/**
 * Core scope resolution logic.
 *
 * Algorithm (simplified, không có site-level):
 *   1. Parse scope string → positive set + negative set
 *   2. Get role permissions (nếu có role)
 *   3. effective = rolePermissions ∪ positive
 *   4. Expand parent-child (includePermissionCodes)
 *   5. effective = effective - negative
 *   6. Return space-separated string
 */
public class ScopeHelper {

    /**
     * Resolve effective scope cho một AppAccess.
     *
     * @param scopeString       raw scope từ AppAccess.scope (VD: "admin +order.create -order.delete")
     * @param rolePermissionCodes  permission codes từ role (nếu có role)
     * @param allPermissions    tất cả permissions của app (để expand parent-child)
     * @return effective scope string (space-separated permission codes)
     */
    public static String resolveEffectiveScope(
            String scopeString,
            Set<String> rolePermissionCodes,
            List<AppPermission> allPermissions) {

        // 1. Parse scope string
        Set<String> positive = new HashSet<>();
        Set<String> negative = new HashSet<>();
        List<String> nonPermissionScopes = new ArrayList<>();

        if (scopeString != null && !scopeString.isBlank()) {
            String[] parts = scopeString.trim().split("\\s+");
            Set<String> allCodes = new HashSet<>();
            for (AppPermission p : allPermissions) {
                allCodes.add(p.getCode());
            }

            for (String part : parts) {
                if (part.startsWith("-")) {
                    String code = part.substring(1);
                    if (allCodes.contains(code)) {
                        negative.add(code);
                    }
                } else {
                    String code = part.startsWith("+") ? part.substring(1) : part;
                    if (allCodes.contains(code)) {
                        positive.add(code);
                    } else {
                        // Non-permission scope (VD: "admin") — giữ nguyên
                        nonPermissionScopes.add(part);
                    }
                }
            }
        }

        // 2. Start with role permissions; if role has none, use positive scope entries
        Set<String> effective = new HashSet<>(rolePermissionCodes);
        if (effective.isEmpty()) {
            effective.addAll(positive);
        } else {
            // Add explicit positive overrides (thêm permission ngoài role)
            effective.addAll(positive);
        }

        // 3. Build parent-child map
        Map<String, List<String>> parentChildMap = new HashMap<>();
        for (AppPermission perm : allPermissions) {
            if (perm.getIncludePermissionCodes() != null && !perm.getIncludePermissionCodes().isEmpty()) {
                parentChildMap.put(perm.getCode(), perm.getIncludePermissionCodes());
            }
        }

        // 4. Apply negations (cascade to children)
        for (String denied : negative) {
            effective.remove(denied);
            if (parentChildMap.containsKey(denied)) {
                for (String child : parentChildMap.get(denied)) {
                    effective.remove(child);
                }
            }
        }

        // 5. Expand parent-child (gán parent → auto include children)
        Set<String> expanded = new HashSet<>();
        for (String code : effective) {
            expanded.add(code);
            if (parentChildMap.containsKey(code)) {
                expanded.addAll(parentChildMap.get(code));
            }
        }

        // 6. Final negation pass
        for (String denied : negative) {
            expanded.remove(denied);
        }

        // 7. Assemble: non-permission scopes first, then permission codes
        StringBuilder sb = new StringBuilder();
        for (String nonPerm : nonPermissionScopes) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(nonPerm);
        }
        for (String code : expanded) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(code);
        }

        return sb.toString().trim();
    }

    /**
     * Lọc scope string, chỉ giữ lại tokens hợp lệ:
     * - Non-permission tokens (VD: "admin") — luôn giữ
     * - Permission codes (+code hoặc code) — giữ nếu nằm trong knownCodes
     * - Negations (-code) — giữ nếu code nằm trong knownCodes
     *
     * Dùng sau khi Setup xóa permissions: loại bỏ codes "ma" khỏi scope.
     */
    public static String filterToKnownPermissions(String scopeString, Set<String> knownCodes) {
        if (scopeString == null || scopeString.isBlank()) {
            return "";
        }

        String[] parts = scopeString.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (part.startsWith("-")) {
                String code = part.substring(1);
                if (knownCodes.contains(code)) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(part);
                }
            } else if (part.startsWith("+")) {
                String code = part.substring(1);
                if (knownCodes.contains(code)) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(part);
                }
            } else {
                // Nếu là permission code đã biết, hoặc non-permission token (VD: "admin") → giữ
                if (knownCodes.contains(part) || !isPermissionLike(part, knownCodes)) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(part);
                }
            }
        }

        return sb.toString().trim();
    }

    /**
     * Kiểm tra token có "giống" permission code không.
     * Nếu token chứa dấu "." thì coi là permission code (VD: order.create).
     * Token đơn giản (admin, manager) thì coi là non-permission → luôn giữ.
     */
    private static boolean isPermissionLike(String token, Set<String> knownCodes) {
        // Nếu code nằm trong known → chắc chắn là permission
        if (knownCodes.contains(token)) return true;
        // Nếu chứa "." → likely permission format (order.create, product.view)
        return token.contains(".");
    }
}
