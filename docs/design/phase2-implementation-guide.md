# Phase 2 구현 가이드 — 숙소 등록 E2E

> 작성일: 2026-04-04
> 작성자: project-lead
> 대상: STORY-103, STORY-104, STORY-105
> 전제: STORY-101(Partner 도메인), STORY-102(공통 VO) 완료 상태

---

## 개요

Phase 2는 Domain -> Application -> Persistence -> REST API 전 레이어를 관통하는 **첫 번째 E2E 흐름**이다.
파트너가 숙소를 등록하면 Property + 하위 엔티티(편의시설, 사진, 속성값)가 원자적으로 저장되고, PropertyId가 반환되는 흐름을 완성한다.

구현 순서는 반드시 **STORY-103 -> STORY-104 -> STORY-105** 순서를 따른다. Application 레이어(Port 정의)가 먼저 완성되어야 Adapter가 구현할 수 있고, API는 UseCase가 정의된 후에야 Controller를 작성할 수 있다.

---

## STORY-103: 숙소 등록 UseCase 구현

### 구현 순서

```
1. Port-Out 인터페이스 (Persistence/외부 시스템과의 계약)
2. Command DTO (record)
3. Factory (도메인 객체 생성)
4. Manager (트랜잭션 경계)
5. PersistenceFacade (원자적 저장)
6. Port-In 인터페이스 (UseCase)
7. Service (오케스트레이션)
```

### 1단계: Port-Out 인터페이스 정의

패키지: `application/src/main/java/com/ryuqq/otatoy/application/port/out/persistence/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `PropertyCommandPort` | Property 저장 (persist/persistAll) | APP-PRT-001 |
| `PropertyQueryPort` | Property 조회 (findById/findByCondition) | APP-PRT-001 |
| `PropertyAmenityCommandPort` | PropertyAmenity 벌크 저장 | APP-PRT-001 |
| `PropertyPhotoCommandPort` | PropertyPhoto 벌크 저장 | APP-PRT-001 |
| `PropertyAttributeValueCommandPort` | PropertyAttributeValue 벌크 저장 | APP-PRT-001 |
| `PartnerQueryPort` | Partner 조회 (findById) | APP-PRT-001 |
| `PropertyTypeQueryPort` | PropertyType 조회 (findById) | APP-PRT-001 |

```java
// PropertyCommandPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.property.Property;
import java.util.List;

public interface PropertyCommandPort {
    Long persist(Property property);
    void persistAll(List<Property> properties);
}

// PropertyQueryPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import java.util.Optional;

public interface PropertyQueryPort {
    Optional<Property> findById(PropertyId id);
}

// PropertyAmenityCommandPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import java.util.List;

public interface PropertyAmenityCommandPort {
    void persistAll(List<PropertyAmenity> amenities);
}

// PropertyPhotoCommandPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import java.util.List;

public interface PropertyPhotoCommandPort {
    void persistAll(List<PropertyPhoto> photos);
}

// PropertyAttributeValueCommandPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import java.util.List;

public interface PropertyAttributeValueCommandPort {
    void persistAll(List<PropertyAttributeValue> attributeValues);
}

// PartnerQueryPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import java.util.Optional;

public interface PartnerQueryPort {
    Optional<Partner> findById(PartnerId id);
}

// PropertyTypeQueryPort.java
package com.ryuqq.otatoy.application.port.out.persistence;

import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import java.util.Optional;

public interface PropertyTypeQueryPort {
    Optional<PropertyType> findById(PropertyTypeId id);
}
```

**주의사항**:
- Port 파라미터는 Domain 객체(Aggregate, VO)만 사용한다 (APP-PRT-002)
- CommandPort에 delete 메서드 금지 (APP-PRT-001)
- QueryPort에 findAll() 금지 (APP-PRT-001)
- 하위 엔티티(Amenity, Photo, AttributeValue) Port를 독립으로 분리하는 이유: PersistenceFacade에서 Property 저장 후 생성된 PropertyId를 하위 엔티티에 세팅해야 하므로, 별도 Port로 순차 호출한다

### 2단계: Command DTO

패키지: `application/src/main/java/com/ryuqq/otatoy/application/dto/command/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `RegisterPropertyCommand` | 숙소 등록 요청 데이터 | APP-DTO-001 |

```java
// RegisterPropertyCommand.java
package com.ryuqq.otatoy.application.dto.command;

import java.util.List;

public record RegisterPropertyCommand(
    Long partnerId,
    Long brandId,            // nullable
    Long propertyTypeId,
    String name,
    String description,      // nullable
    String address,
    double latitude,
    double longitude,
    String neighborhood,     // nullable
    String region,           // nullable
    String promotionText,    // nullable
    List<AmenityItem> amenities,
    List<PhotoItem> photos,
    List<AttributeValueItem> attributeValues
) {
    public static RegisterPropertyCommand of(Long partnerId, Long brandId, Long propertyTypeId,
                                              String name, String description,
                                              String address, double latitude, double longitude,
                                              String neighborhood, String region, String promotionText,
                                              List<AmenityItem> amenities, List<PhotoItem> photos,
                                              List<AttributeValueItem> attributeValues) {
        return new RegisterPropertyCommand(partnerId, brandId, propertyTypeId, name, description,
            address, latitude, longitude, neighborhood, region, promotionText,
            amenities != null ? amenities : List.of(),
            photos != null ? photos : List.of(),
            attributeValues != null ? attributeValues : List.of());
    }

    // 중첩 record — 편의시설 항목
    public record AmenityItem(
        String amenityType,
        String name,
        int additionalPrice,   // Money(int) 기반
        int sortOrder
    ) {}

    // 중첩 record — 사진 항목
    public record PhotoItem(
        String photoType,
        String originUrl,
        String cdnUrl,         // nullable
        int sortOrder
    ) {}

    // 중첩 record — 속성값 항목
    public record AttributeValueItem(
        Long propertyTypeAttributeId,
        String value
    ) {}
}
```

**주의사항**:
- record로 선언하여 불변성 보장 (APP-DTO-001)
- 인스턴스 메서드 금지, 정적 팩토리 of()만 허용 (APP-DTO-001)
- 편의시설/사진/속성값은 중첩 record로 포함

### 3단계: Factory

패키지: `application/src/main/java/com/ryuqq/otatoy/application/factory/`

| 클래스명 | 의존성 | 적용 컨벤션 |
|---------|-------|-----------|
| `PropertyFactory` | `TimeProvider` | APP-FAC-001 |

```java
// PropertyFactory.java
package com.ryuqq.otatoy.application.factory;

import com.ryuqq.otatoy.application.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.*;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
// TimeProvider import (core 모듈)

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
public class PropertyFactory {

    private final TimeProvider timeProvider;

    public PropertyFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public PropertyCreateResult create(RegisterPropertyCommand command) {
        Instant now = timeProvider.now();

        // 1. Property 생성 (PropertyId는 null — 저장 전)
        Property property = Property.forNew(
            PartnerId.of(command.partnerId()),
            command.brandId() != null ? BrandId.of(command.brandId()) : null,
            PropertyTypeId.of(command.propertyTypeId()),
            PropertyName.of(command.name()),
            command.description() != null ? PropertyDescription.of(command.description()) : null,
            Location.of(command.address(), command.latitude(), command.longitude(),
                        command.neighborhood(), command.region()),
            command.promotionText() != null ? PromotionText.of(command.promotionText()) : null,
            now
        );

        // 2. 하위 엔티티 생성 (PropertyId는 null — 저장 후 세팅)
        List<PropertyAmenity> amenities = command.amenities().stream()
            .map(a -> PropertyAmenity.forNew(
                null, // propertyId — 저장 후 세팅
                AmenityType.valueOf(a.amenityType()),
                AmenityName.of(a.name()),
                Money.of(a.additionalPrice()),
                a.sortOrder()))
            .toList();

        List<PropertyPhoto> photos = command.photos().stream()
            .map(p -> PropertyPhoto.forNew(
                null, // propertyId — 저장 후 세팅
                PhotoType.valueOf(p.photoType()),
                OriginUrl.of(p.originUrl()),
                p.cdnUrl() != null ? CdnUrl.of(p.cdnUrl()) : null,
                p.sortOrder(),
                now))
            .toList();

        List<PropertyAttributeValue> attributeValues = command.attributeValues().stream()
            .map(av -> PropertyAttributeValue.forNew(
                null, // propertyId — 저장 후 세팅
                PropertyTypeAttributeId.of(av.propertyTypeAttributeId()),
                av.value(),
                now))
            .toList();

        return new PropertyCreateResult(property, amenities, photos, attributeValues);
    }

    // Factory 내부 결과 객체 — 여러 도메인 객체를 한번에 전달
    public record PropertyCreateResult(
        Property property,
        List<PropertyAmenity> amenities,
        List<PropertyPhoto> photos,
        List<PropertyAttributeValue> attributeValues
    ) {}
}
```

**주의사항**:
- TimeProvider는 Factory에만 주입한다 (APP-FAC-001). Service/Manager에서 TimeProvider 직접 사용 금지
- PropertyAmenity.forNew()의 propertyId 파라미터가 null인 이유: Property가 아직 저장되지 않아 ID가 없다. PersistenceFacade에서 Property 저장 후 PropertyId를 세팅한다
- TimeProvider 인터페이스는 core 모듈(또는 application 내)에 정의, 구현체(SystemTimeProvider)는 infra 모듈에 둔다

**TimeProvider 정의 (core 또는 application):**
```java
public interface TimeProvider {
    Instant now();
    java.time.LocalDate today();
}
```

### 4단계: Manager (트랜잭션 경계)

패키지: `application/src/main/java/com/ryuqq/otatoy/application/manager/`

| 클래스명 | 유형 | 의존성 | @Transactional | 적용 컨벤션 |
|---------|------|-------|:--------------:|-----------|
| `PropertyReadManager` | ReadManager | `PropertyQueryPort` | readOnly=true | APP-MGR-001 |
| `PartnerReadManager` | ReadManager | `PartnerQueryPort` | readOnly=true | APP-MGR-001, APP-BC-001 |
| `PropertyTypeReadManager` | ReadManager | `PropertyTypeQueryPort` | readOnly=true | APP-MGR-001, APP-BC-001 |

```java
// PartnerReadManager.java
package com.ryuqq.otatoy.application.manager.read;

import com.ryuqq.otatoy.application.port.out.persistence.PartnerQueryPort;
import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class PartnerReadManager {

    private final PartnerQueryPort partnerQueryPort;

    public PartnerReadManager(PartnerQueryPort partnerQueryPort) {
        this.partnerQueryPort = partnerQueryPort;
    }

    public Partner getById(PartnerId id) {
        return partnerQueryPort.findById(id)
            .orElseThrow(PartnerNotFoundException::new);
    }
}

// PropertyTypeReadManager.java
package com.ryuqq.otatoy.application.manager.read;

import com.ryuqq.otatoy.application.port.out.persistence.PropertyTypeQueryPort;
import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
// PropertyTypeNotFoundException은 propertytype BC에 정의 필요
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class PropertyTypeReadManager {

    private final PropertyTypeQueryPort propertyTypeQueryPort;

    public PropertyTypeReadManager(PropertyTypeQueryPort propertyTypeQueryPort) {
        this.propertyTypeQueryPort = propertyTypeQueryPort;
    }

    public PropertyType getById(PropertyTypeId id) {
        return propertyTypeQueryPort.findById(id)
            .orElseThrow(() -> new PropertyTypeNotFoundException());
    }
}

// PropertyReadManager.java
package com.ryuqq.otatoy.application.manager.read;

import com.ryuqq.otatoy.application.port.out.persistence.PropertyQueryPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class PropertyReadManager {

    private final PropertyQueryPort propertyQueryPort;

    public PropertyReadManager(PropertyQueryPort propertyQueryPort) {
        this.propertyQueryPort = propertyQueryPort;
    }

    public Property getById(PropertyId id) {
        return propertyQueryPort.findById(id)
            .orElseThrow(PropertyNotFoundException::new);
    }
}
```

**주의사항**:
- ReadManager는 반드시 `@Transactional(readOnly = true)` (APP-MGR-001)
- 다른 BC의 ReadManager는 Service에서 자유롭게 호출 가능 (APP-BC-001)
- `PropertyTypeNotFoundException`이 현재 도메인에 없다. **선행 작업**: `domain/propertytype/` 패키지에 `PropertyTypeErrorCode`, `PropertyTypeNotFoundException` 추가 필요

### 5단계: PersistenceFacade

패키지: `application/src/main/java/com/ryuqq/otatoy/application/facade/`

| 클래스명 | 의존성 | @Transactional | 적용 컨벤션 |
|---------|-------|:--------------:|-----------|
| `PropertyPersistenceFacade` | PropertyCommandPort, PropertyAmenityCommandPort, PropertyPhotoCommandPort, PropertyAttributeValueCommandPort | 필수 | APP-FCD-001 |

```java
// PropertyPersistenceFacade.java
package com.ryuqq.otatoy.application.facade;

import com.ryuqq.otatoy.application.port.out.persistence.*;
import com.ryuqq.otatoy.domain.property.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class PropertyPersistenceFacade {

    private final PropertyCommandPort propertyCommandPort;
    private final PropertyAmenityCommandPort propertyAmenityCommandPort;
    private final PropertyPhotoCommandPort propertyPhotoCommandPort;
    private final PropertyAttributeValueCommandPort propertyAttributeValueCommandPort;

    public PropertyPersistenceFacade(PropertyCommandPort propertyCommandPort,
                                      PropertyAmenityCommandPort propertyAmenityCommandPort,
                                      PropertyPhotoCommandPort propertyPhotoCommandPort,
                                      PropertyAttributeValueCommandPort propertyAttributeValueCommandPort) {
        this.propertyCommandPort = propertyCommandPort;
        this.propertyAmenityCommandPort = propertyAmenityCommandPort;
        this.propertyPhotoCommandPort = propertyPhotoCommandPort;
        this.propertyAttributeValueCommandPort = propertyAttributeValueCommandPort;
    }

    /**
     * Property + 하위 엔티티를 하나의 트랜잭션에서 원자적으로 저장한다.
     * Property 저장 후 생성된 PropertyId를 하위 엔티티에 세팅한다.
     */
    public Long persist(Property property,
                        List<PropertyAmenity> amenities,
                        List<PropertyPhoto> photos,
                        List<PropertyAttributeValue> attributeValues) {

        // 1. Property 저장 -> PropertyId 획득
        Long propertyId = propertyCommandPort.persist(property);
        PropertyId pid = PropertyId.of(propertyId);

        // 2. 하위 엔티티에 PropertyId 세팅 후 저장
        // 주의: 도메인 객체가 불변이므로 새로 생성해야 한다
        // Factory에서 propertyId=null로 생성했으므로, 여기서 propertyId를 포함해 재생성
        if (!amenities.isEmpty()) {
            List<PropertyAmenity> withId = amenities.stream()
                .map(a -> PropertyAmenity.forNew(pid, a.amenityType(), a.name(),
                    a.additionalPrice(), a.sortOrder()))
                .toList();
            propertyAmenityCommandPort.persistAll(withId);
        }

        if (!photos.isEmpty()) {
            List<PropertyPhoto> withId = photos.stream()
                .map(p -> PropertyPhoto.forNew(pid, p.photoType(), p.originUrl(),
                    p.cdnUrl(), p.sortOrder(), p.createdAt()))
                .toList();
            propertyPhotoCommandPort.persistAll(withId);
        }

        if (!attributeValues.isEmpty()) {
            List<PropertyAttributeValue> withId = attributeValues.stream()
                .map(av -> PropertyAttributeValue.forNew(pid, av.propertyTypeAttributeId(),
                    av.value(), av.createdAt()))
                .toList();
            propertyAttributeValueCommandPort.persistAll(withId);
        }

        return propertyId;
    }
}
```

**주의사항**:
- 하나의 @Transactional에서 Property + 모든 하위 엔티티 저장 (원자성 보장)
- 하위 엔티티는 불변이므로 PropertyId를 포함해 재생성한다
- PersistenceFacade는 여러 CommandPort를 묶는 역할 (APP-FCD-001)

### 6단계: Port-In (UseCase 인터페이스)

패키지: `application/src/main/java/com/ryuqq/otatoy/application/port/in/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `RegisterPropertyUseCase` | 숙소 등록 유스케이스 | APP-UC-001 |

```java
// RegisterPropertyUseCase.java
package com.ryuqq.otatoy.application.port.in;

import com.ryuqq.otatoy.application.dto.command.RegisterPropertyCommand;

public interface RegisterPropertyUseCase {
    Long execute(RegisterPropertyCommand command);
}
```

### 7단계: Service (오케스트레이션)

패키지: `application/src/main/java/com/ryuqq/otatoy/application/service/`

| 클래스명 | 구현 | 의존성 | @Transactional | 적용 컨벤션 |
|---------|------|-------|:--------------:|-----------|
| `RegisterPropertyService` | `RegisterPropertyUseCase` | PartnerReadManager, PropertyTypeReadManager, PropertyFactory, PropertyPersistenceFacade | 금지 | APP-SVC-001, APP-BC-001 |

```java
// RegisterPropertyService.java
package com.ryuqq.otatoy.application.service;

import com.ryuqq.otatoy.application.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.facade.PropertyPersistenceFacade;
import com.ryuqq.otatoy.application.factory.PropertyFactory;
import com.ryuqq.otatoy.application.manager.read.PartnerReadManager;
import com.ryuqq.otatoy.application.manager.read.PropertyTypeReadManager;
import com.ryuqq.otatoy.application.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import org.springframework.stereotype.Service;

@Service
public class RegisterPropertyService implements RegisterPropertyUseCase {

    private final PartnerReadManager partnerReadManager;
    private final PropertyTypeReadManager propertyTypeReadManager;
    private final PropertyFactory propertyFactory;
    private final PropertyPersistenceFacade propertyPersistenceFacade;

    public RegisterPropertyService(PartnerReadManager partnerReadManager,
                                    PropertyTypeReadManager propertyTypeReadManager,
                                    PropertyFactory propertyFactory,
                                    PropertyPersistenceFacade propertyPersistenceFacade) {
        this.partnerReadManager = partnerReadManager;
        this.propertyTypeReadManager = propertyTypeReadManager;
        this.propertyFactory = propertyFactory;
        this.propertyPersistenceFacade = propertyPersistenceFacade;
    }

    @Override
    public Long execute(RegisterPropertyCommand command) {
        // 1. 파트너 존재 확인 (다른 BC ReadManager) — 없으면 PartnerNotFoundException
        partnerReadManager.getById(PartnerId.of(command.partnerId()));

        // 2. 숙소 유형 존재 확인 (다른 BC ReadManager) — 없으면 PropertyTypeNotFoundException
        propertyTypeReadManager.getById(PropertyTypeId.of(command.propertyTypeId()));

        // 3. 도메인 객체 생성 (Factory — TimeProvider 주입)
        PropertyFactory.PropertyCreateResult result = propertyFactory.create(command);

        // 4. 원자적 저장 (PersistenceFacade — @Transactional)
        return propertyPersistenceFacade.persist(
            result.property(),
            result.amenities(),
            result.photos(),
            result.attributeValues()
        );
    }
}
```

**검증 흐름 요약**:
1. 파트너 존재 여부 -> PartnerReadManager (readOnly 트랜잭션)
2. 숙소 유형 존재 여부 -> PropertyTypeReadManager (readOnly 트랜잭션)
3. 도메인 검증 -> Property.forNew() 내부 (단일 필드 검증)
4. 원자적 저장 -> PropertyPersistenceFacade (쓰기 트랜잭션)

### STORY-103 선행 작업 (도메인 보완)

현재 도메인에 없어서 **STORY-101 보완**으로 추가해야 하는 것:

| 항목 | 패키지 | 설명 |
|------|-------|------|
| `PropertyTypeErrorCode` | `domain/propertytype/` | PropertyType BC 에러 코드 (enum) |
| `PropertyTypeNotFoundException` | `domain/propertytype/` | DomainException 상속 |
| `AccommodationErrorCode` 보강 | `domain/accommodation/` | getHttpStatus() 메서드 추가 필요 (ErrorCode 인터페이스 확장 검토) |

> **결정 필요**: ErrorCode 인터페이스에 `int getHttpStatus()` 메서드를 추가할지, GlobalExceptionHandler에서 ErrorCode -> HTTP Status 매핑 테이블을 관리할지.
> **권장**: ErrorCode에 getHttpStatus() 추가. 각 ErrorCode enum에서 HTTP 상태 코드를 직접 선언하면 ErrorMapper가 단순해진다.

### STORY-103 적용 컨벤션 체크리스트

| 규칙 코드 | 규칙 | 확인 사항 |
|-----------|------|---------|
| APP-UC-001 | UseCase 인터페이스 1:1 Service | RegisterPropertyUseCase <-> RegisterPropertyService |
| APP-SVC-001 | Service에 @Transactional 금지 | RegisterPropertyService에 @Transactional 없음 |
| APP-MGR-001 | ReadManager @Transactional(readOnly=true) | PartnerReadManager, PropertyTypeReadManager, PropertyReadManager |
| APP-FCD-001 | PersistenceFacade에 @Transactional | PropertyPersistenceFacade |
| APP-FAC-001 | TimeProvider는 Factory에만 주입 | PropertyFactory만 TimeProvider 의존 |
| APP-PRT-001 | CommandPort는 persist/persistAll만 | delete 메서드 없음 |
| APP-PRT-002 | Port 파라미터는 Domain 객체 | Property, PropertyAmenity 등 도메인 객체 전달 |
| APP-DTO-001 | Command는 record | RegisterPropertyCommand + 중첩 record |
| APP-BC-001 | 다른 BC ReadManager 호출 허용 | PartnerReadManager, PropertyTypeReadManager 호출 |
| APP-BC-001 | Port 직접 호출 금지 | Service에서 Port 직접 의존 없음 |

### STORY-103 최종 파일 목록 (생성 순서)

```
application/src/main/java/com/ryuqq/otatoy/application/
├── port/
│   ├── in/
│   │   └── RegisterPropertyUseCase.java
│   └── out/
│       └── persistence/
│           ├── PropertyCommandPort.java
│           ├── PropertyQueryPort.java
│           ├── PropertyAmenityCommandPort.java
│           ├── PropertyPhotoCommandPort.java
│           ├── PropertyAttributeValueCommandPort.java
│           ├── PartnerQueryPort.java
│           └── PropertyTypeQueryPort.java
├── dto/
│   └── command/
│       └── RegisterPropertyCommand.java
├── factory/
│   ├── TimeProvider.java                     (또는 core 모듈)
│   └── PropertyFactory.java
├── manager/
│   └── read/
│       ├── PartnerReadManager.java
│       ├── PropertyTypeReadManager.java
│       └── PropertyReadManager.java
├── facade/
│   └── PropertyPersistenceFacade.java
└── service/
    └── RegisterPropertyService.java
```

총 14개 파일.

---

## STORY-104: 숙소 Persistence Adapter 구현

### 구현 순서

```
1. BaseAuditEntity / SoftDeletableEntity (이미 존재 — 확인)
2. JPA Entity 클래스 (create() 팩토리 패턴)
3. EntityMapper (toDomain/toEntity)
4. JpaRepository (save/saveAll만)
5. QueryDslRepository + ConditionBuilder
6. CommandAdapter (JpaRepository 의존)
7. QueryAdapter (QueryDslRepository 의존)
```

### 1단계: JPA Entity

패키지: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/otatoy/persistence/entity/`

> BaseAuditEntity, SoftDeletableEntity는 이미 구현 완료.

| 클래스명 | 상속 | DDL 매핑 | 적용 컨벤션 |
|---------|------|---------|-----------|
| `PropertyJpaEntity` | `SoftDeletableEntity` | property 테이블 | PER-ENT-001, 004, 005 |
| `PropertyAmenityJpaEntity` | `SoftDeletableEntity` | property_amenity 테이블 | PER-ENT-001, 004, 005 |
| `PropertyPhotoJpaEntity` | `SoftDeletableEntity` | property_photo 테이블 | PER-ENT-001, 004, 005 |
| `PropertyAttributeValueJpaEntity` | `SoftDeletableEntity` | property_attribute_value 테이블 | PER-ENT-001, 004, 005 |
| `PartnerJpaEntity` | `SoftDeletableEntity` | partner 테이블 | PER-ENT-001, 004, 005 |
| `PartnerMemberJpaEntity` | `SoftDeletableEntity` | partner_member 테이블 | PER-ENT-001, 004, 005 |
| `PropertyTypeJpaEntity` | `BaseAuditEntity` | (기존 DDL V202604041000 brand_and_property_type) | PER-ENT-001, 004, 005 |

**PropertyJpaEntity 구현 예시:**

```java
@Entity
@Table(name = "property")
public class PropertyJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long partnerId;
    private Long brandId;
    private Long propertyTypeId;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String neighborhood;
    private String region;
    private String status;
    private String promotionText;

    protected PropertyJpaEntity() {
        super();
    }

    private PropertyJpaEntity(Long id, Long partnerId, Long brandId, Long propertyTypeId,
                               String name, String description, String address,
                               double latitude, double longitude,
                               String neighborhood, String region,
                               String status, String promotionText,
                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.partnerId = partnerId;
        this.brandId = brandId;
        this.propertyTypeId = propertyTypeId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighborhood = neighborhood;
        this.region = region;
        this.status = status;
        this.promotionText = promotionText;
    }

    public static PropertyJpaEntity create(Long id, Long partnerId, Long brandId, Long propertyTypeId,
                                            String name, String description, String address,
                                            double latitude, double longitude,
                                            String neighborhood, String region,
                                            String status, String promotionText,
                                            Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyJpaEntity(id, partnerId, brandId, propertyTypeId,
            name, description, address, latitude, longitude,
            neighborhood, region, status, promotionText,
            createdAt, updatedAt, deletedAt);
    }

    // getter만 — setter 없음, 비즈니스 로직 없음
    public Long getId() { return id; }
    public Long getPartnerId() { return partnerId; }
    public Long getBrandId() { return brandId; }
    public Long getPropertyTypeId() { return propertyTypeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getNeighborhood() { return neighborhood; }
    public String getRegion() { return region; }
    public String getStatus() { return status; }
    public String getPromotionText() { return promotionText; }
}
```

**PropertyAmenityJpaEntity:**

```java
@Entity
@Table(name = "property_amenity")
public class PropertyAmenityJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long propertyId;
    private String amenityType;
    private String name;
    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal additionalPrice;
    private int sortOrder;

    protected PropertyAmenityJpaEntity() { super(); }

    private PropertyAmenityJpaEntity(Long id, Long propertyId, String amenityType,
                                      String name, java.math.BigDecimal additionalPrice,
                                      int sortOrder,
                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.propertyId = propertyId;
        this.amenityType = amenityType;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sortOrder = sortOrder;
    }

    public static PropertyAmenityJpaEntity create(Long id, Long propertyId, String amenityType,
                                                    String name, java.math.BigDecimal additionalPrice,
                                                    int sortOrder,
                                                    Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PropertyAmenityJpaEntity(id, propertyId, amenityType, name,
            additionalPrice, sortOrder, createdAt, updatedAt, deletedAt);
    }

    // getter ...
    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getAmenityType() { return amenityType; }
    public String getName() { return name; }
    public java.math.BigDecimal getAdditionalPrice() { return additionalPrice; }
    public int getSortOrder() { return sortOrder; }
}
```

**Entity DDL 매핑 확인 체크리스트:**

| Entity 필드 | DDL 컬럼 | 타입 매핑 | 비고 |
|-------------|---------|----------|------|
| PropertyJpaEntity.id | property.id | BIGINT -> Long | AUTO_INCREMENT |
| PropertyJpaEntity.partnerId | property.partner_id | BIGINT -> Long | FK (Long 전략) |
| PropertyJpaEntity.brandId | property.brand_id | BIGINT -> Long | nullable |
| PropertyJpaEntity.propertyTypeId | property.property_type_id | BIGINT -> Long | FK (Long 전략) |
| PropertyJpaEntity.status | property.status | VARCHAR(30) -> String | enum name() |
| PropertyAmenityJpaEntity.additionalPrice | property_amenity.additional_price | DECIMAL(12,2) -> BigDecimal | Money -> BigDecimal 변환 |
| PropertyPhotoJpaEntity.originUrl | property_photo.origin_url | VARCHAR(1000) -> String | OriginUrl -> String 변환 |

**금지 목록 (모든 Entity 공통):**
- @OneToMany, @ManyToOne, @OneToOne, @ManyToMany 금지 (PER-ENT-001)
- @Getter, @Setter, @NoArgsConstructor, @Builder 등 Lombok 금지 (PER-ENT-004)
- updateXxx(), changeXxx(), setXxx() 메서드 금지 (PER-ENT-005)
- public 생성자 금지 (protected 기본 + private 전체 필드) (PER-ENT-001)

### 2단계: EntityMapper

패키지: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/otatoy/persistence/mapper/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `PropertyEntityMapper` | Property <-> PropertyJpaEntity | PER-MAP-001 |
| `PropertyAmenityEntityMapper` | PropertyAmenity <-> PropertyAmenityJpaEntity | PER-MAP-001 |
| `PropertyPhotoEntityMapper` | PropertyPhoto <-> PropertyPhotoJpaEntity | PER-MAP-001 |
| `PropertyAttributeValueEntityMapper` | PropertyAttributeValue <-> PropertyAttributeValueJpaEntity | PER-MAP-001 |
| `PartnerEntityMapper` | Partner <-> PartnerJpaEntity | PER-MAP-001 |
| `PropertyTypeEntityMapper` | PropertyType <-> PropertyTypeJpaEntity | PER-MAP-001 |

```java
// PropertyEntityMapper.java
@Component
public class PropertyEntityMapper {

    public PropertyJpaEntity toEntity(Property domain) {
        return PropertyJpaEntity.create(
            domain.id() != null ? domain.id().value() : null,
            domain.partnerId().value(),
            domain.brandId() != null ? domain.brandId().value() : null,
            domain.propertyTypeId().value(),
            domain.name().value(),
            domain.description() != null ? domain.description().value() : null,
            domain.location().address(),
            domain.location().latitude(),
            domain.location().longitude(),
            domain.location().neighborhood(),
            domain.location().region(),
            domain.status().name(),
            domain.promotionText() != null ? domain.promotionText().value() : null,
            domain.createdAt(),
            domain.updatedAt(),
            null  // deletedAt
        );
    }

    public Property toDomain(PropertyJpaEntity entity) {
        return Property.reconstitute(
            PropertyId.of(entity.getId()),
            PartnerId.of(entity.getPartnerId()),
            entity.getBrandId() != null ? BrandId.of(entity.getBrandId()) : null,
            PropertyTypeId.of(entity.getPropertyTypeId()),
            PropertyName.of(entity.getName()),
            entity.getDescription() != null ? PropertyDescription.of(entity.getDescription()) : null,
            Location.of(entity.getAddress(), entity.getLatitude(), entity.getLongitude(),
                        entity.getNeighborhood(), entity.getRegion()),
            entity.getPromotionText() != null ? PromotionText.of(entity.getPromotionText()) : null,
            PropertyStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

**VO 변환 핵심 패턴:**

| Domain VO | Entity 필드 | toEntity 변환 | toDomain 변환 |
|-----------|------------|--------------|--------------|
| `PropertyName` | `String name` | `domain.name().value()` | `PropertyName.of(entity.getName())` |
| `Location` | `address, latitude, longitude, neighborhood, region` | 각 필드 분해 | `Location.of(...)` |
| `PropertyStatus` | `String status` | `domain.status().name()` | `PropertyStatus.valueOf(entity.getStatus())` |
| `Money` | `BigDecimal additionalPrice` | `domain.additionalPrice().toBigDecimal()` | `Money.of(entity.getAdditionalPrice())` |
| `AmenityType` | `String amenityType` | `.name()` (enum) | `AmenityType.valueOf(...)` |
| `OriginUrl` | `String originUrl` | `.value()` | `OriginUrl.of(...)` |

### 3단계: Repository

패키지: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/otatoy/persistence/repository/`

**JPA Repository (Command 전용):**

| 클래스명 | 대상 Entity | 적용 컨벤션 |
|---------|-----------|-----------|
| `PropertyJpaRepository` | `PropertyJpaEntity` | PER-REP-001 |
| `PropertyAmenityJpaRepository` | `PropertyAmenityJpaEntity` | PER-REP-001 |
| `PropertyPhotoJpaRepository` | `PropertyPhotoJpaEntity` | PER-REP-001 |
| `PropertyAttributeValueJpaRepository` | `PropertyAttributeValueJpaEntity` | PER-REP-001 |
| `PartnerJpaRepository` | `PartnerJpaEntity` | PER-REP-001 |

```java
// PropertyJpaRepository.java
package com.ryuqq.otatoy.persistence.repository.jpa;

import com.ryuqq.otatoy.persistence.entity.PropertyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, Long> {
    // 커스텀 메서드 추가 금지
    // @Query 금지
    // findBy* 금지
    // deleteBy* 금지
}
```

**QueryDsl Repository (Query 전용):**

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `PropertyQueryDslRepository` | Property 조회 (findById, findByCondition) | PER-REP-001 |
| `PartnerQueryDslRepository` | Partner 조회 (findById) | PER-REP-001 |
| `PropertyTypeQueryDslRepository` | PropertyType 조회 (findById) | PER-REP-001 |

```java
// PropertyQueryDslRepository.java
package com.ryuqq.otatoy.persistence.repository.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.entity.PropertyJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.ryuqq.otatoy.persistence.entity.QPropertyJpaEntity.propertyJpaEntity;

@Repository
public class PropertyQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public PropertyQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<PropertyJpaEntity> findById(Long id) {
        PropertyJpaEntity result = queryFactory
            .selectFrom(propertyJpaEntity)
            .where(
                propertyJpaEntity.id.eq(id),
                propertyJpaEntity.deleted.isFalse()  // soft delete 필터 필수
            )
            .fetchOne();
        return Optional.ofNullable(result);
    }
}
```

**주의사항**:
- 모든 QueryDslRepository 조회에 `deleted.isFalse()` 조건 포함 필수 (PER-REP-001)
- JPAQueryFactory는 별도 Bean 등록 필요 (JpaConfig에 추가)

**JpaConfig에 JPAQueryFactory Bean 추가:**

```java
@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "com.ryuqq.otatoy.persistence")
@EnableJpaRepositories(basePackages = "com.ryuqq.otatoy.persistence")
public class JpaConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
```

### 4단계: Adapter

패키지: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/otatoy/persistence/adapter/`

**CommandAdapter:**

| 클래스명 | 구현 Port | 의존성 | 적용 컨벤션 |
|---------|----------|-------|-----------|
| `PropertyCommandAdapter` | `PropertyCommandPort` | PropertyJpaRepository, PropertyEntityMapper | PER-ADP-001, PER-ADP-002 |
| `PropertyAmenityCommandAdapter` | `PropertyAmenityCommandPort` | PropertyAmenityJpaRepository, PropertyAmenityEntityMapper | PER-ADP-001 |
| `PropertyPhotoCommandAdapter` | `PropertyPhotoCommandPort` | PropertyPhotoJpaRepository, PropertyPhotoEntityMapper | PER-ADP-001 |
| `PropertyAttributeValueCommandAdapter` | `PropertyAttributeValueCommandPort` | PropertyAttributeValueJpaRepository, PropertyAttributeValueEntityMapper | PER-ADP-001 |

```java
// PropertyCommandAdapter.java
@Component
public class PropertyCommandAdapter implements PropertyCommandPort {

    private final PropertyJpaRepository jpaRepository;
    private final PropertyEntityMapper mapper;

    public PropertyCommandAdapter(PropertyJpaRepository jpaRepository,
                                   PropertyEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Long persist(Property property) {
        PropertyJpaEntity entity = mapper.toEntity(property);
        PropertyJpaEntity saved = jpaRepository.save(entity);
        return saved.getId();
    }

    @Override
    public void persistAll(List<Property> properties) {
        List<PropertyJpaEntity> entities = properties.stream()
            .map(mapper::toEntity)
            .toList();
        jpaRepository.saveAll(entities);
    }
}
```

**QueryAdapter:**

| 클래스명 | 구현 Port | 의존성 | 적용 컨벤션 |
|---------|----------|-------|-----------|
| `PropertyQueryAdapter` | `PropertyQueryPort` | PropertyQueryDslRepository, PropertyEntityMapper | PER-ADP-001 |
| `PartnerQueryAdapter` | `PartnerQueryPort` | PartnerQueryDslRepository, PartnerEntityMapper | PER-ADP-001 |
| `PropertyTypeQueryAdapter` | `PropertyTypeQueryPort` | PropertyTypeQueryDslRepository, PropertyTypeEntityMapper | PER-ADP-001 |

```java
// PropertyQueryAdapter.java
@Component
public class PropertyQueryAdapter implements PropertyQueryPort {

    private final PropertyQueryDslRepository queryDslRepository;
    private final PropertyEntityMapper mapper;

    public PropertyQueryAdapter(PropertyQueryDslRepository queryDslRepository,
                                 PropertyEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return queryDslRepository.findById(id.value())
            .map(mapper::toDomain);
    }
}
```

### STORY-104 Flyway DDL 확인

이미 존재하는 마이그레이션 파일:
- `V202604041001__create_property_tables.sql` -- property, property_attribute_value, property_amenity, property_photo
- `V202604041007__create_partner_tables.sql` -- partner, partner_member
- `V202604041000__create_brand_and_property_type_tables.sql` -- brand, property_type (확인 필요)

**추가 DDL 불필요** -- 이미 모든 테이블이 Flyway로 정의되어 있다. Entity 필드가 DDL과 일치하는지 반드시 검증해야 한다.

### STORY-104 적용 컨벤션 체크리스트

| 규칙 코드 | 규칙 | 확인 사항 |
|-----------|------|---------|
| PER-ENT-001 | static create() 유일 생성 진입점 | 모든 Entity |
| PER-ENT-001 | JPA 관계 어노테이션 금지 | @OneToMany 등 없음 |
| PER-ENT-002 | BaseAuditEntity/SoftDeletableEntity 상속 | 모든 Entity |
| PER-ENT-003 | hard delete 금지, persist만 | CommandPort에 delete 없음 |
| PER-ENT-004 | Lombok 전면 금지 | @Getter, @Setter 등 없음 |
| PER-ENT-005 | 상태 변경 메서드 금지 | create(), getter, isXxx()만 |
| PER-MAP-001 | Mapper toDomain/toEntity | reconstitute()로 복원, create()로 변환 |
| PER-ADP-001 | CQRS Adapter 분리 | Command는 JpaRepository만, Query는 QueryDslRepository만 |
| PER-ADP-002 | merge 방식 | save()로 persist/merge 자동 판단 |
| PER-REP-001 | JpaRepository 커스텀 메서드 금지 | save/saveAll만 |
| PER-REP-001 | QueryDsl soft delete 필터 | deleted.isFalse() 필수 |

### STORY-104 최종 파일 목록 (생성 순서)

```
adapter-out/persistence-mysql/src/main/java/com/ryuqq/otatoy/persistence/
├── config/
│   └── JpaConfig.java                              (수정 — JPAQueryFactory Bean 추가)
│
├── entity/
│   ├── BaseAuditEntity.java                         (기존)
│   ├── SoftDeletableEntity.java                     (기존)
│   ├── PropertyJpaEntity.java                       (신규)
│   ├── PropertyAmenityJpaEntity.java                (신규)
│   ├── PropertyPhotoJpaEntity.java                  (신규)
│   ├── PropertyAttributeValueJpaEntity.java         (신규)
│   ├── PartnerJpaEntity.java                        (신규)
│   ├── PartnerMemberJpaEntity.java                  (신규)
│   └── PropertyTypeJpaEntity.java                   (신규)
│
├── mapper/
│   ├── PropertyEntityMapper.java                    (신규)
│   ├── PropertyAmenityEntityMapper.java             (신규)
│   ├── PropertyPhotoEntityMapper.java               (신규)
│   ├── PropertyAttributeValueEntityMapper.java      (신규)
│   ├── PartnerEntityMapper.java                     (신규)
│   └── PropertyTypeEntityMapper.java                (신규)
│
├── repository/
│   ├── jpa/
│   │   ├── PropertyJpaRepository.java               (신규)
│   │   ├── PropertyAmenityJpaRepository.java        (신규)
│   │   ├── PropertyPhotoJpaRepository.java          (신규)
│   │   ├── PropertyAttributeValueJpaRepository.java (신규)
│   │   └── PartnerJpaRepository.java                (신규)
│   └── querydsl/
│       ├── PropertyQueryDslRepository.java          (신규)
│       ├── PartnerQueryDslRepository.java           (신규)
│       └── PropertyTypeQueryDslRepository.java      (신규)
│
└── adapter/
    ├── command/
    │   ├── PropertyCommandAdapter.java              (신규)
    │   ├── PropertyAmenityCommandAdapter.java       (신규)
    │   ├── PropertyPhotoCommandAdapter.java         (신규)
    │   └── PropertyAttributeValueCommandAdapter.java (신규)
    └── query/
        ├── PropertyQueryAdapter.java                (신규)
        ├── PartnerQueryAdapter.java                 (신규)
        └── PropertyTypeQueryAdapter.java            (신규)
```

총 26개 파일 (기존 3개 + 신규 23개).

---

## STORY-105: 숙소 등록 REST API

### 구현 순서

```
1. 공통 응답 (ApiResponse, SliceResponse, ErrorDetail)
2. ErrorMapper + GlobalExceptionHandler
3. Request/Response DTO (record)
4. ApiMapper
5. Controller
6. Swagger 설정
```

### 1단계: 공통 응답 포맷

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/common/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `ApiResponse` | 공통 응답 래퍼 (success/data/error) | API-CTR-002 |
| `SliceResponse` | 페이지네이션 래퍼 | API-CTR-003 |

```java
// ApiResponse.java
package com.ryuqq.otatoy.api.common;

public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorDetail error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> error(String code, String userMessage, String debugMessage) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, userMessage, debugMessage));
    }

    public record ErrorDetail(
        String code,
        String userMessage,
        String debugMessage
    ) {}
}

// SliceResponse.java
package com.ryuqq.otatoy.api.common;

import java.util.List;

public record SliceResponse<T>(
    List<T> content,
    boolean hasNext,
    Long nextCursor
) {
    public static <T> SliceResponse<T> of(List<T> content, boolean hasNext, Long nextCursor) {
        return new SliceResponse<>(content, hasNext, nextCursor);
    }
}
```

### 2단계: ErrorMapper + GlobalExceptionHandler

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/common/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `ErrorMapper` | DomainException -> ErrorDetail 변환 | API-ERR-001 |
| `GlobalExceptionHandler` | 전역 예외 처리 | API-ERR-001 |

```java
// ErrorMapper.java
@Component
public class ErrorMapper {

    public ApiResponse.ErrorDetail toErrorDetail(DomainException e) {
        ErrorCode errorCode = e.getErrorCode();
        String debugMessage = e.getArgs().isEmpty()
            ? errorCode.getMessage()
            : e.getArgs().toString();

        return new ApiResponse.ErrorDetail(
            errorCode.getCode(),
            errorCode.getMessage(),     // userMessage
            debugMessage                // debugMessage
        );
    }
}

// GlobalExceptionHandler.java
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorMapper errorMapper;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        ApiResponse.ErrorDetail detail = errorMapper.toErrorDetail(e);
        log.warn("도메인 예외 발생: {} - {}", detail.code(), detail.debugMessage());

        HttpStatus status = resolveHttpStatus(e);
        return ResponseEntity.status(status)
            .body(new ApiResponse<>(false, null, detail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        String userMessage = "입력값이 올바르지 않습니다";
        String debugMessage = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));

        log.warn("검증 실패: {}", debugMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", userMessage, debugMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다", e.getMessage()));
    }

    private HttpStatus resolveHttpStatus(DomainException e) {
        // ErrorCode에 getHttpStatus()가 있으면 사용, 아니면 기본 매핑
        String code = e.getErrorCode().getCode();
        if (code.contains("NOT_FOUND") || code.endsWith("-001")) {
            // PartnerErrorCode.PARTNER_NOT_FOUND("PTN-001"), PropertyNotFoundException 등
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
```

**HTTP 상태 매핑 전략 결정 필요:**
- **옵션 A (권장)**: ErrorCode 인터페이스에 `int getHttpStatus()` 추가. 각 ErrorCode enum에서 직접 선언.
- **옵션 B**: GlobalExceptionHandler에서 ErrorCode -> HttpStatus 매핑 테이블 관리.
- 현재 ErrorCode에 getHttpStatus()가 없으므로, 초기에는 옵션 B로 시작하고 이후 옵션 A로 리팩토링하는 것이 현실적이다.

### 3단계: Request/Response DTO

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/extranet/dto/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `RegisterPropertyApiRequest` | 숙소 등록 요청 | API-DTO-001, API-DTO-002 |
| `PropertyApiResponse` | 숙소 응답 | API-DTO-001 |

```java
// RegisterPropertyApiRequest.java
package com.ryuqq.otatoy.api.extranet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterPropertyApiRequest(

    @NotNull(message = "파트너 ID는 필수입니다")
    Long partnerId,

    Long brandId,                // nullable

    @NotNull(message = "숙소 유형 ID는 필수입니다")
    Long propertyTypeId,

    @NotBlank(message = "숙소 이름은 필수입니다")
    @Size(max = 200, message = "숙소 이름은 200자 이하입니다")
    String name,

    String description,          // nullable

    @NotBlank(message = "주소는 필수입니다")
    String address,

    double latitude,
    double longitude,

    String neighborhood,         // nullable
    String region,               // nullable
    String promotionText,        // nullable

    @Valid
    List<AmenityApiRequest> amenities,

    @Valid
    List<PhotoApiRequest> photos,

    @Valid
    List<AttributeValueApiRequest> attributeValues
) {
    public record AmenityApiRequest(
        @NotBlank(message = "편의시설 유형은 필수입니다")
        String amenityType,
        @NotBlank(message = "편의시설 이름은 필수입니다")
        String name,
        int additionalPrice,
        int sortOrder
    ) {}

    public record PhotoApiRequest(
        @NotBlank(message = "사진 유형은 필수입니다")
        String photoType,
        @NotBlank(message = "원본 URL은 필수입니다")
        String originUrl,
        String cdnUrl,
        int sortOrder
    ) {}

    public record AttributeValueApiRequest(
        @NotNull(message = "속성 ID는 필수입니다")
        Long propertyTypeAttributeId,
        @NotBlank(message = "속성값은 필수입니다")
        String value
    ) {}
}
```

### 4단계: ApiMapper

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/extranet/mapper/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `PropertyApiMapper` | ApiRequest -> Command 변환 | API-DTO-001 |

```java
// PropertyApiMapper.java
package com.ryuqq.otatoy.api.extranet.mapper;

import com.ryuqq.otatoy.api.extranet.dto.RegisterPropertyApiRequest;
import com.ryuqq.otatoy.application.dto.command.RegisterPropertyCommand;

import java.util.List;

public class PropertyApiMapper {

    private PropertyApiMapper() {} // 인스턴스 생성 방지

    public static RegisterPropertyCommand toCommand(RegisterPropertyApiRequest request) {
        List<RegisterPropertyCommand.AmenityItem> amenities = request.amenities() != null
            ? request.amenities().stream()
                .map(a -> new RegisterPropertyCommand.AmenityItem(
                    a.amenityType(), a.name(), a.additionalPrice(), a.sortOrder()))
                .toList()
            : List.of();

        List<RegisterPropertyCommand.PhotoItem> photos = request.photos() != null
            ? request.photos().stream()
                .map(p -> new RegisterPropertyCommand.PhotoItem(
                    p.photoType(), p.originUrl(), p.cdnUrl(), p.sortOrder()))
                .toList()
            : List.of();

        List<RegisterPropertyCommand.AttributeValueItem> attributeValues =
            request.attributeValues() != null
                ? request.attributeValues().stream()
                    .map(av -> new RegisterPropertyCommand.AttributeValueItem(
                        av.propertyTypeAttributeId(), av.value()))
                    .toList()
                : List.of();

        return RegisterPropertyCommand.of(
            request.partnerId(),
            request.brandId(),
            request.propertyTypeId(),
            request.name(),
            request.description(),
            request.address(),
            request.latitude(),
            request.longitude(),
            request.neighborhood(),
            request.region(),
            request.promotionText(),
            amenities,
            photos,
            attributeValues
        );
    }
}
```

### 5단계: Controller

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/extranet/`

| 클래스명 | 의존성 | 적용 컨벤션 |
|---------|-------|-----------|
| `ExtranetPropertyController` | `RegisterPropertyUseCase` | API-CTR-001, API-CTR-002, API-DOC-001 |

```java
// ExtranetPropertyController.java
package com.ryuqq.otatoy.api.extranet;

import com.ryuqq.otatoy.api.common.ApiResponse;
import com.ryuqq.otatoy.api.extranet.dto.RegisterPropertyApiRequest;
import com.ryuqq.otatoy.api.extranet.mapper.PropertyApiMapper;
import com.ryuqq.otatoy.application.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.port.in.RegisterPropertyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/extranet/properties")
@Tag(name = "Extranet - 숙소 관리", description = "파트너용 숙소 등록/관리 API")
public class ExtranetPropertyController {

    private final RegisterPropertyUseCase registerPropertyUseCase;

    public ExtranetPropertyController(RegisterPropertyUseCase registerPropertyUseCase) {
        this.registerPropertyUseCase = registerPropertyUseCase;
    }

    @Operation(summary = "숙소 등록", description = "파트너가 새로운 숙소를 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "파트너 또는 숙소 유형을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> registerProperty(
            @Valid @RequestBody RegisterPropertyApiRequest request) {

        // 1. Request -> Command 변환 (ApiMapper)
        RegisterPropertyCommand command = PropertyApiMapper.toCommand(request);

        // 2. UseCase 호출
        Long propertyId = registerPropertyUseCase.execute(command);

        // 3. 201 응답
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(propertyId));
    }
}
```

**금지 사항 확인**:
- @Transactional 없음 (API-CTR-001)
- 비즈니스 로직 없음 (API-CTR-001)
- UseCase 인터페이스만 의존 — 구체 Service 주입 금지 (API-CTR-001)
- 인라인 변환 로직 없음 — ApiMapper 사용 (API-DTO-001)
- @DeleteMapping 없음 (API-CTR-001)

### 6단계: Swagger/SpringDoc 설정

SpringDoc 의존성은 `adapter-in/rest-api/build.gradle.kts`에 추가 필요:
```kotlin
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
```

`application.yml` 추가:
```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### STORY-105 적용 컨벤션 체크리스트

| 규칙 코드 | 규칙 | 확인 사항 |
|-----------|------|---------|
| API-CTR-001 | UseCase만 의존 | RegisterPropertyUseCase만 주입 |
| API-CTR-001 | @Transactional 금지 | Controller에 없음 |
| API-CTR-001 | @DeleteMapping 금지 | 사용 안 함 |
| API-CTR-002 | 201 응답 + ApiResponse 래핑 | POST -> CREATED |
| API-CTR-003 | 페이지네이션은 SliceResponse | (Phase 4에서 적용) |
| API-DTO-001 | DTO는 record | RegisterPropertyApiRequest |
| API-DTO-001 | ApiMapper로 변환 | PropertyApiMapper.toCommand() |
| API-DTO-002 | Jakarta Validation | @NotNull, @NotBlank, @Size |
| API-ERR-001 | GlobalExceptionHandler | DomainException 일괄 처리 |
| API-ERR-001 | ErrorMapper | userMessage + debugMessage 분리 |
| API-DOC-001 | Swagger 어노테이션 | @Tag, @Operation, @ApiResponses |

### STORY-105 최종 파일 목록 (생성 순서)

```
adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/
├── common/
│   ├── ApiResponse.java                             (신규)
│   ├── SliceResponse.java                           (신규)
│   ├── ErrorMapper.java                             (신규)
│   └── GlobalExceptionHandler.java                  (신규)
│
└── extranet/
    ├── ExtranetPropertyController.java              (신규)
    ├── dto/
    │   └── RegisterPropertyApiRequest.java          (신규)
    └── mapper/
        └── PropertyApiMapper.java                   (신규)
```

총 7개 파일.

---

## 전체 Phase 2 파일 개수 요약

| Story | 신규 파일 수 | 수정 파일 수 | 레이어 |
|-------|:----------:|:----------:|-------|
| STORY-103 | 14 | 0 | Application |
| STORY-104 | 23 | 1 (JpaConfig) | Persistence |
| STORY-105 | 7 | 0 | API |
| **합계** | **44** | **1** | |

---

## 선행 작업 / 알려진 의존성

### STORY-103 시작 전 반드시 완료해야 하는 것

1. **TimeProvider 인터페이스 + 구현체**: core 또는 application 모듈에 `TimeProvider` 인터페이스, infra(또는 적절한 모듈)에 `SystemTimeProvider` 구현체. 테스트용 `FixedTimeProvider`도 함께 작성
2. **PropertyTypeNotFoundException**: `domain/propertytype/` 패키지에 `PropertyTypeErrorCode` enum + `PropertyTypeNotFoundException` 클래스 추가 (STORY-101 보완)
3. **ErrorCode.getHttpStatus() 결정**: ErrorCode 인터페이스 확장 여부 결정. 확장하면 GlobalExceptionHandler가 단순해진다

### STORY-104 시작 전 반드시 완료해야 하는 것

1. **QueryDSL 빌드 설정**: `adapter-out/persistence-mysql/build.gradle.kts`에 QueryDSL annotation processor 설정 (Q클래스 자동 생성)
2. **application.yml JPA 설정**: `spring.jpa.open-in-view=false`, `spring.jpa.hibernate.ddl-auto=validate`
3. **Flyway 설정**: `spring.flyway.enabled=true`, migration 경로 확인

### STORY-105 시작 전 반드시 완료해야 하는 것

1. **SpringDoc 의존성**: `adapter-in/rest-api/build.gradle.kts`에 springdoc-openapi 의존성 추가
2. **STORY-103, STORY-104 완료**: UseCase와 Adapter가 모두 존재해야 Controller가 동작

---

## 트레이드오프 기록

### 하위 엔티티 저장 전략: PersistenceFacade에서 재생성 vs ID 후세팅

**선택**: PersistenceFacade에서 PropertyId를 포함해 하위 엔티티를 재생성

**대안**: 하위 엔티티에 `setPropertyId(PropertyId)` mutable 메서드 추가
- 장점: 객체 재생성 비용 없음
- 단점: 도메인 객체의 불변성이 깨짐 (컨벤션 위반)

**결정 이유**: 도메인 객체의 불변성을 유지하는 것이 더 중요하다. 숙소 등록은 빈번한 작업이 아니므로 객체 재생성 비용은 무시할 수 있다.

### 하위 엔티티 Port 분리 vs 통합

**선택**: PropertyAmenityCommandPort, PropertyPhotoCommandPort, PropertyAttributeValueCommandPort 각각 독립

**대안**: PropertySubEntityCommandPort 하나로 통합
- 장점: Port 개수 감소
- 단점: 하나의 Port가 여러 도메인 개념을 섞음, CQRS Adapter 분리 시 Adapter도 비대해짐

**결정 이유**: 각 하위 엔티티가 독립 테이블이고, 향후 독립적으로 CRUD가 필요할 수 있다 (예: 사진만 추가/삭제). Port를 분리하면 각 Adapter가 단일 책임을 가진다.

### ErrorCode HTTP 상태 매핑

**현재**: GlobalExceptionHandler에서 ErrorCode 문자열 패턴으로 매핑 (임시)
**향후**: ErrorCode 인터페이스에 `int getHttpStatus()` 추가하여 각 ErrorCode가 자신의 HTTP 상태를 선언 (Phase 1 보완 시)
