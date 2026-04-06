package com.ryuqq.otatoy.api.core.logging;

/**
 * 개인정보(PII) 마스킹 유틸리티.
 * 로그 출력 시 민감한 개인정보를 마스킹하여 노출을 방지한다.
 *
 * <p>지원하는 마스킹 유형:</p>
 * <ul>
 *   <li>전화번호: 010-****-1234</li>
 *   <li>이메일: r***@example.com</li>
 *   <li>이름: 홍*동</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class PiiMaskingUtils {

    private PiiMaskingUtils() {
        // 인스턴스 생성 방지
    }

    /**
     * 전화번호를 마스킹한다.
     * 앞 3자리와 뒤 4자리만 노출하고 중간을 ****로 대체한다.
     *
     * @param phone 원본 전화번호 (예: 01012345678, 010-1234-5678)
     * @return 마스킹된 전화번호 (예: 010-****-5678)
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return "***";
        }
        return phone.substring(0, 3) + "-****-" + phone.substring(phone.length() - 4);
    }

    /**
     * 이메일을 마스킹한다.
     * 로컬 파트의 첫 글자만 노출하고 나머지를 ***로 대체한다.
     *
     * @param email 원본 이메일 (예: ryu@example.com)
     * @return 마스킹된 이메일 (예: r***@example.com)
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String masked = local.charAt(0) + "***";
        return masked + "@" + parts[1];
    }

    /**
     * 이름을 마스킹한다.
     * 첫 글자와 마지막 글자만 노출하고 중간을 *로 대체한다.
     *
     * @param name 원본 이름 (예: 홍길동)
     * @return 마스킹된 이름 (예: 홍*동)
     */
    public static String maskName(String name) {
        if (name == null || name.length() < 2) {
            return "***";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
