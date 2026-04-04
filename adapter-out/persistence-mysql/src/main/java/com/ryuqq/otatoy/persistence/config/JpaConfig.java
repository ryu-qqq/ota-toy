package com.ryuqq.otatoy.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "com.ryuqq.otatoy.persistence")
@EnableJpaRepositories(basePackages = "com.ryuqq.otatoy.persistence")
public class JpaConfig {
}
