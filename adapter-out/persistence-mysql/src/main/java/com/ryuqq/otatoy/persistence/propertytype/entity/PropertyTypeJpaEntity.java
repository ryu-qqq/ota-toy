package com.ryuqq.otatoy.persistence.propertytype.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * PropertyType JPA Entity.
 * 숙소 유형 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "property_type")
public class PropertyTypeJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    protected PropertyTypeJpaEntity() {
        super();
    }

    private PropertyTypeJpaEntity(Long id, String code, String name, String description,
                                   Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static PropertyTypeJpaEntity create(Long id, String code, String name, String description,
                                                Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyTypeJpaEntity(id, code, name, description, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
