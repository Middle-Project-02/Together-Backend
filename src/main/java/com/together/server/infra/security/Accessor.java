package com.together.server.infra.security;

/**
 * 인증된 사용자의 식별자(ID)를 보관하는 값 객체입니다.
 * GUEST 상수를 통해 비로그인(게스트) 사용자를 표현할 수 있습니다.
 * isGuest()로 게스트 여부를 확인할 수 있습니다.
 */

public record Accessor(String id) {

    private static final String GUEST_ID = "GUEST";
    public static final Accessor GUEST = new Accessor(GUEST_ID);

    public boolean isGuest() {
        return GUEST_ID.equals(id);
    }
}
