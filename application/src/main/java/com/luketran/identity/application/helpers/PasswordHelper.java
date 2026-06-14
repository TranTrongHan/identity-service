package com.luketran.identity.application.helpers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Helper class cho password hashing và verification.
 *
 * Dùng BCrypt — algorithm tự tạo salt ngẫu nhiên mỗi lần hash,
 * nên cùng 1 password sẽ cho ra hash khác nhau (an toàn hơn MD5/SHA).
 *
 * Cách BCrypt hoạt động:
 *   hash = BCrypt(password) → "$2a$10$randomSalt22chars...hashedResult31chars..."
 *                                    ↑ cost factor (10 = 2^10 rounds)
 *
 * Verify: BCrypt extract salt từ stored hash, hash lại password, so sánh.
 * → Không cần lưu salt riêng (salt nằm trong hash string).
 *
 * secretKey dùng làm "pepper" — thêm vào password trước khi hash.
 * Pepper = secret chung lưu per-account, nếu DB bị leak mà attacker không có pepper
 * thì không brute-force được.
 */
public class PasswordHelper {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hash password với secretKey (pepper).
     *
     * @param rawPassword password người dùng nhập
     * @param secretKey   per-account secret (pepper)
     * @return BCrypt hash string (60 chars)
     */
    public static String hashPassword(String rawPassword, String secretKey) {
        String peppered = rawPassword + secretKey;
        return encoder.encode(peppered);
    }

    /**
     * Verify password: so sánh raw input với stored hash.
     *
     * @param rawPassword  password người dùng nhập khi login
     * @param secretKey    per-account secret (pepper)
     * @param storedHash   BCrypt hash lưu trong DB (từ lần tạo account)
     * @return true nếu password đúng
     */
    public static boolean verifyPassword(String rawPassword, String secretKey, String storedHash) {
        String peppered = rawPassword + secretKey;
        return encoder.matches(peppered, storedHash);
    }
    
}
