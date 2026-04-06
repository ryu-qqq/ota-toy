package com.ryuqq.otatoy.persistence.redis.support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Inventory Redis Lua 스크립트 로더.
 * classpath에서 .lua 파일을 로드하여 캐싱한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class InventoryLuaScriptHolder {

    private final String decrementScript;
    private final String incrementScript;
    private final String initializeScript;

    public InventoryLuaScriptHolder() {
        this.decrementScript = loadLuaScript("lua/inventory_decrement.lua");
        this.incrementScript = loadLuaScript("lua/inventory_increment.lua");
        this.initializeScript = loadLuaScript("lua/inventory_initialize.lua");
    }

    public String decrementScript() {
        return decrementScript;
    }

    public String incrementScript() {
        return incrementScript;
    }

    public String initializeScript() {
        return initializeScript;
    }

    private String loadLuaScript(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (InputStream inputStream = resource.getInputStream()) {
                return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lua script 로드 실패: " + path, e);
        }
    }
}
