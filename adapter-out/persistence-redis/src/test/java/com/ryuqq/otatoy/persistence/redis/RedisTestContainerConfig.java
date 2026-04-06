package com.ryuqq.otatoy.persistence.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers Redis 공통 설정 (Singleton 패턴).
 * static 블록에서 Redis 컨테이너를 기동하여 JVM 수명 동안 1회만 실행한다.
 * MySQL Testcontainers와 동일한 org.testcontainers 기반.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public abstract class RedisTestContainerConfig {

    @SuppressWarnings("resource")
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine"))
        .withExposedPorts(6379)
        .withReuse(true);

    static {
        redis.start();
    }

    /**
     * 테스트용 RedissonClient를 생성한다.
     * 각 테스트 클래스에서 @BeforeAll/@BeforeEach에서 호출하여 사용.
     */
    protected static RedissonClient createRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
        return Redisson.create(config);
    }
}
