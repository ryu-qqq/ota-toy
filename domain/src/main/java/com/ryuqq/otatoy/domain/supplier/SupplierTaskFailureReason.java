package com.ryuqq.otatoy.domain.supplier;

import java.time.Instant;

/**
 * 공급자 작업 실패 사유를 구조화하는 VO.
 * JSON 직렬화/역직렬화는 순수 Java로 구현한다 (Jackson 의존 금지).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierTaskFailureReason(
        Integer httpStatus,
        String errorCode,
        String errorMessage,
        Instant occurredAt
) {

    public SupplierTaskFailureReason {
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("에러 메시지는 필수입니다");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("실패 시각은 필수입니다");
        }
    }

    public static SupplierTaskFailureReason of(Integer httpStatus, String errorCode, String errorMessage, Instant occurredAt) {
        return new SupplierTaskFailureReason(httpStatus, errorCode, errorMessage, occurredAt);
    }

    /**
     * JSON 문자열로 직렬화한다.
     * SupplierTask.failureReason 필드에 저장된다.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"httpStatus\":").append(httpStatus != null ? httpStatus : "null");
        sb.append(",\"errorCode\":").append(errorCode != null ? escapeJsonString(errorCode) : "null");
        sb.append(",\"errorMessage\":").append(escapeJsonString(errorMessage));
        sb.append(",\"occurredAt\":\"").append(occurredAt.toString()).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * JSON 문자열에서 복원한다.
     */
    public static SupplierTaskFailureReason fromJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON 문자열은 필수입니다");
        }

        Integer httpStatus = parseNullableInt(extractValue(json, "httpStatus"));
        String errorCode = parseNullableString(extractValue(json, "errorCode"));
        String errorMessage = parseNullableString(extractValue(json, "errorMessage"));
        Instant occurredAt = Instant.parse(parseNullableString(extractValue(json, "occurredAt")));

        return new SupplierTaskFailureReason(httpStatus, errorCode, errorMessage, occurredAt);
    }

    private static String escapeJsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) {
            return "null";
        }
        startIdx += searchKey.length();

        if (json.charAt(startIdx) == '"') {
            int endIdx = findClosingQuote(json, startIdx + 1);
            return json.substring(startIdx, endIdx + 1);
        }

        int endIdx = json.indexOf(',', startIdx);
        if (endIdx == -1) {
            endIdx = json.indexOf('}', startIdx);
        }
        return json.substring(startIdx, endIdx).trim();
    }

    private static int findClosingQuote(String json, int fromIdx) {
        for (int i = fromIdx; i < json.length(); i++) {
            if (json.charAt(i) == '"' && json.charAt(i - 1) != '\\') {
                return i;
            }
        }
        return json.length() - 1;
    }

    private static Integer parseNullableInt(String value) {
        if (value == null || "null".equals(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private static String parseNullableString(String value) {
        if (value == null || "null".equals(value)) {
            return null;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return value;
    }
}
