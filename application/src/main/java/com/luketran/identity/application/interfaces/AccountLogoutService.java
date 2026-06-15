package com.luketran.identity.application.interfaces;

import java.util.UUID;

public interface AccountLogoutService {
    /**
     * User tự check mình có bị force-logout không (one-shot: consume flag sau khi check).
     */
    boolean checkForceLogout(UUID accountId);

    /**
     * Admin ép đăng xuất: xóa toàn bộ sessions + tạo flag AccountLogout.
     */
    void createForceLogout(UUID accountId);

    /**
     * Admin check trạng thái force-logout (không consume flag).
     */
    boolean isForceLoggedOut(UUID accountId);
}
