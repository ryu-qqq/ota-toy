# Phase 2 구현 가이드 — 숙소 등록 E2E

> 작성일: 2026-04-05
> 작성자: project-lead
> 대상: STORY-103, STORY-104, STORY-105
> 전제: STORY-101(Partner 도메인), STORY-102(공통 VO) 완료 상태
> 이전 버전 대비 변경 사유: OTA 리서치(`docs/research/ota-extranet-registration-flow.md`)에 근거하여 UseCase를 단일 책임으로 분리

---

## 이전 버전의 문제점과 수정 근거

### 문제: "기본정보 + 편의시설 + 사진 + 속성값을 한번에 저장" 구조

이전 구현 가이드는 `RegisterPropertyCommand`에 편의시설/사진/속성값 리스트를 모두 포함하고, `PropertyPersistenceFacade`에서 원자적으로 저장하는 구조였다. 이 설계에는 세 가지 문제가 있다.

**1. 실제 OTA 등록 플로우와 불일치**

Booking.com, 야놀자 모두 숙소 등록은 **단계별 별도 저장**이다. 기본정보, 사진, 편의시설, 객실이 각각 독립 API로 관리되며, 등록 후에도 독립적으로 수정된다. 사진 업로드가 숙소 기본정보 트랜잭션에 묶여야 할 이유가 없다.

| 항목 | 생명주기 | 변경 빈도 | 행위자 |
|------|---------|----------|-------|
| 기본정보 | 거의 안 바뀜 | 낮음 | 계약 담당 |
| 사진 | 수시 업로드/삭제/재정렬 | 높음 | 마케팅 |
| 편의시설 | 토글 on/off | 중간 | 운영 |
| 속성값 | 간헐적 변경 | 낮음 | 운영 |

**2. PersistenceFacade 오용**

Application 컨벤션(APP-FCD-001)에 따르면, PersistenceFacade는 "여러 Aggregate를 하나의 트랜잭션에서 원자적으로 저장해야 할 때" 사용한다. Property 기본정보 저장은 단일 Aggregate이므로 CommandManager로 충분하다. PersistenceFacade가 필요한 예시는 "예약 생성 + Outbox 저장"처럼 서로 다른 Aggregate가 반드시 같은 트랜잭션에 있어야 하는 경우다.

**3. Command에 불필요한 결합**

편의시설/사진/속성값을 RegisterPropertyCommand에 중첩하면, "숙소 기본정보 등록"이라는 단일 유스케이스에 관계없는 데이터가 끼어든다. 숙소 기본정보만 등록하고 싶어도 빈 리스트를 전달해야 한다.

---

## 수정된 UseCase 설계

### 원칙: 독립 생명주기를 가진 데이터는 독립 UseCase로 분리

```
RegisterPropertyUseCase       — Property 기본정보만 저장
AddPropertyPhotosUseCase      — 사진 독립 업로드
SetPropertyAmenitiesUseCase   — 편의시설 독립 설정
SetPropertyAttributesUseCase  — 속성값 독립 설정
RegisterRoomTypeUseCase       — 객실 독립 등록 (STORY-106)
```

### PersistenceFacade vs CommandManager 사용 기준

| 상황 | 사용 컴포넌트 | 예시 |
|------|-------------|------|
| 단일 Aggregate 저장 | CommandManager | Property 기본정보 저장, 사진 업로드 |
| 여러 Aggregate 원자적 저장 | PersistenceFacade | 예약 생성 + Outbox 저장, 예약 생성 + 재고 기록 |

---

## 개요

Phase 2는 Domain -> Application -> Persistence -> REST API 전 레이어를 관통하는 **첫 번째 E2E 흐름**이다.
파트너가 숙소를 단계별로 등록하는 플로우를 완성한다:

1. **기본정보 등록** (POST /api/v1/extranet/properties) -> PropertyId 반환
2. **사진 업로드** (POST /api/v1/extranet/properties/{id}/photos) -> 독립 저장
3. **편의시설 설정** (POST /api/v1/extranet/properties/{id}/amenities) -> 독립 저장
4. **속성값 설정** (POST /api/v1/extranet/properties/{id}/attributes) -> 독립 저장

구현 순서는 반드시 **STORY-103 -> STORY-104 -> STORY-105** 순서를 따른다.

---

## STORY-103: 숙소 등록 UseCase 구현

### 수용기준 재정의

기존 수용기준 AC-8(PersistenceFacade가 Property + 하위 엔티티를 원자적 저장), AC-9(Command에 편의시설/사진/속성값 리스트 포함), AC-10(Factory가 하위 엔티티를 함께 생성)은 **폐기**한다. 대신 아래의 수용기준으로 대체한다.

**변경된 수용기준**:
- AC-1: RegisterPropertyUseCase 호출 시 Property **기본정보만** 저장되고 PropertyId(Long)가 반환됨
- AC-2: partnerId가 존재하지 않으면 PartnerNotFoundException 발생 (PropertyRegistrationValidator — PartnerReadManager.verifyExists 경유, APP-VAL-002)
- AC-3: propertyTypeId가 존재하지 않으면 PropertyTypeNotFoundException 발생 (PropertyRegistrationValidator — PropertyTypeReadManager.verifyExists 경유, APP-VAL-002)
- AC-4: PropertyCommandPort, PropertyQueryPort가 Application 레이어에 정의됨 (APP-PRT-001)
- AC-5: PropertyFactory가 TimeProvider를 주입받아 Property.forNew() 호출로 도메인 객체 생성 (APP-FAC-001)
- AC-6: RegisterPropertyService는 @Transactional 없이 Manager/Factory 조합으로 동작 (APP-SVC-001)
- AC-7: PropertyRegistrationValidator를 통해 파트너/숙소유형 존재 확인 (APP-VAL-002)
- AC-8: Property 저장은 PropertyCommandManager를 사용 (단일 Aggregate이므로 PersistenceFacade 불필요)
- AC-9: RegisterPropertyCommand는 record로 선언. **기본정보 필드만 포함**, 필드 타입은 Domain VO 사용 (APP-DTO-001)
- AC-10: 사진/편의시설/속성값은 각각 독립 UseCase로 분리하여 별도 구현

**추가 UseCase 수용기준**:
- AC-11: AddPropertyPhotosUseCase 호출 시 해당 Property에 사진이 독립 저장됨
- AC-12: SetPropertyAmenitiesUseCase 호출 시 해당 Property의 편의시설이 독립 설정됨
- AC-13: SetPropertyAttributesUseCase 호출 시 해당 Property의 속성값이 독립 설정됨
- AC-14: AC-11~13 모두 PropertyExistenceValidator(PropertyReadManager.verifyExists)로 Property 존재 확인 후 실행 (APP-VAL-002)

### 구현 순서

```
1. Port-Out 인터페이스 (Persistence와의 계약)
2. Command DTO (record)
3. Factory (도메인 객체 생성)
4. Manager (트랜잭션 경계)
5. Port-In 인터페이스 (UseCase)
6. Service (오케스트레이션)
```

### 1단계: Port-Out 인터페이스 정의

패키지:
- Property 관련 Port: `application/src/main/java/com/ryuqq/otatoy/application/property/port/out/`
- Partner 관련 Port: `application/src/main/java/com/ryuqq/otatoy/application/partner/port/out/`
- PropertyType 관련 Port: `application/src/main/java/com/ryuqq/otatoy/application/propertytype/port/out/`

| 클래스명 | 패키지 (BC) | 역할 | 적용 컨벤션 |
|---------|-----------|------|-----------|
| `PropertyCommandPort` | `property/port/out/` | Property 저장 (persist/persistAll) | APP-PRT-001 |
| `PropertyQueryPort` | `property/port/out/` | Property 조회 (findById/findByCondition) | APP-PRT-001 |
| `PropertyAmenityCommandPort` | `property/port/out/` | PropertyAmenity 벌크 저장 | APP-PRT-001 |
| `PropertyPhotoCommandPort` | `property/port/out/` | PropertyPhoto 벌크 저장 | APP-PRT-001 |
| `PropertyAttributeValueCommandPort` | `property/port/out/` | PropertyAttributeValue 벌크 저장 | APP-PRT-001 |
| `PartnerQueryPort` | `partner/port/out/` | Partner 조회 (findById) | APP-PRT-001 |
| `PropertyTypeQueryPort` | `propertytype/port/out/` | PropertyType 조회 (findById) | APP-PRT-001 |

```java
// PropertyCommandPort.java
package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.Property;
import java.util.List;

public interface PropertyCommandPort {
    Long persist(Property property);
    void persistAll(List<Property> properties);
}

// PropertyQueryPort.java
package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import java.util.Optional;

public interface PropertyQueryPort {
    Optional<Property> findById(PropertyId id);
    boolean existsById(PropertyId id);
}

// PropertyAmenityCommandPort.java
package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import java.util.List;

public interface PropertyAmenityCommandPort {
    void persistAll(List<PropertyAmenity> amenities);
}

// PropertyPhotoCommandPort.java
package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import java.util.List;

public interface PropertyPhotoCommandPort {
    void persistAll(List<PropertyPhoto> photos);
}

// PropertyAttributeValueCommandPort.java
package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import java.util.List;

public interface PropertyAttributeValueCommandPort {
    void persistAll(List<PropertyAttributeValue> attributeValues);
}

// PartnerQueryPort.java
package com.ryuqq.otatoy.application.partner.port.out;

import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import java.util.Optional;

public interface PartnerQueryPort {
    Optional<Partner> findById(PartnerId id);
    boolean existsById(PartnerId id);
}

// PropertyTypeQueryPort.java
package com.ryuqq.otatoy.application.propertytype.port.out;

import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import java.util.Optional;

public interface PropertyTypeQueryPort {
    Optional<PropertyType> findById(PropertyTypeId id);
    boolean existsById(PropertyTypeId id);
}
```

**주의사항**:
- Port 파라미터는 Domain 객체(Aggregate, VO)만 사용한다 (APP-PRT-002)
- CommandPort에 delete 메서드 금지 (APP-PRT-001)
- QueryPort에 findAll() 금지 (APP-PRT-001)
- 하위 엔티티(Amenity, Photo, AttributeValue) Port를 독립으로 분리하는 이유: 각각 독립 UseCase에서 호출되고, 생명주기가 다르다

### 2단계: Command DTO

패키지: `application/src/main/java/com/ryuqq/otatoy/application/property/dto/command/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `RegisterPropertyCommand` | 숙소 기본정보 등록 요청 | APP-DTO-001 |
| `AddPropertyPhotosCommand` | 사진 업로드 요청 | APP-DTO-001 |
| `SetPropertyAmenitiesCommand` | 편의시설 설정 요청 | APP-DTO-001 |
| `SetPropertyAttributesCommand` | 속성값 설정 요청 | APP-DTO-001 |

```java
// RegisterPropertyCommand.java — 기본정보만 포함 (필드에 Domain VO 사용, APP-DTO-001)
package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.domain.property.*;

public record RegisterPropertyCommand(
    PartnerId partnerId,
    BrandId brandId,              // nullable
    PropertyTypeId propertyTypeId,
    PropertyName name,
    PropertyDescription description, // nullable
    Location location,
    PromotionText promotionText   // nullable
) {
    public static RegisterPropertyCommand of(PartnerId partnerId, BrandId brandId,
                                              PropertyTypeId propertyTypeId,
                                              PropertyName name, PropertyDescription description,
                                              Location location, PromotionText promotionText) {
        return new RegisterPropertyCommand(partnerId, brandId, propertyTypeId, name, description,
            location, promotionText);
    }
}

// AddPropertyPhotosCommand.java — 사진 독립 업로드 (propertyId는 Domain VO)
package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import java.util.List;

public record AddPropertyPhotosCommand(
    PropertyId propertyId,
    List<PhotoItem> photos
) {
    public static AddPropertyPhotosCommand of(PropertyId propertyId, List<PhotoItem> photos) {
        return new AddPropertyPhotosCommand(propertyId, photos != null ? photos : List.of());
    }

    public record PhotoItem(
        String photoType,
        String originUrl,
        String cdnUrl,         // nullable
        int sortOrder
    ) {}
}

// SetPropertyAmenitiesCommand.java — 편의시설 독립 설정 (propertyId는 Domain VO)
package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import java.util.List;

public record SetPropertyAmenitiesCommand(
    PropertyId propertyId,
    List<AmenityItem> amenities
) {
    public static SetPropertyAmenitiesCommand of(PropertyId propertyId, List<AmenityItem> amenities) {
        return new SetPropertyAmenitiesCommand(propertyId, amenities != null ? amenities : List.of());
    }

    public record AmenityItem(
        String amenityType,
        String name,
        int additionalPrice,   // Money(int) 기반
        int sortOrder
    ) {}
}

// SetPropertyAttributesCommand.java — 속성값 독립 설정 (propertyId는 Domain VO)
package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import java.util.List;

public record SetPropertyAttributesCommand(
    PropertyId propertyId,
    List<AttributeValueItem> attributeValues
) {
    public static SetPropertyAttributesCommand of(PropertyId propertyId, List<AttributeValueItem> attributeValues) {
        return new SetPropertyAttributesCommand(propertyId,
            attributeValues != null ? attributeValues : List.of());
    }

    public record AttributeValueItem(
        Long propertyTypeAttributeId,
        String value
    ) {}
}
```

**변경 포인트**:
1. 이전에는 `RegisterPropertyCommand` 하나에 편의시설/사진/속성값이 중첩 record로 포함되었다. 이제 각각 독립 Command로 분리하여 단일 책임 원칙을 준수한다.
2. Command 필드가 `Long`, `String` 같은 원시 타입에서 Domain VO(`PartnerId`, `PropertyTypeId`, `PropertyName`, `Location` 등)로 변경되었다 (APP-DTO-001). VO 변환은 Adapter-in의 ApiMapper에서 한 번만 수행한다.

### 3단계: Factory

패키지: `application/src/main/java/com/ryuqq/otatoy/application/property/factory/`

| 클래스명 | 의존성 | 적용 컨벤션 |
|---------|-------|-----------|
| `PropertyFactory` | `TimeProvider` | APP-FAC-001 |

```java
// PropertyFactory.java — Property 기본정보 생성만 담당
package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.property.dto.command.AddPropertyPhotosCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
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

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
public class PropertyFactory {

    private final TimeProvider timeProvider;

    public PropertyFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Property 기본정보 도메인 객체를 생성한다.
     * 편의시설/사진/속성값은 별도 UseCase에서 처리하므로 여기서 생성하지 않는다.
     * Command 필드가 이미 Domain VO이므로 변환 없이 직접 전달한다 (APP-DTO-001).
     */
    public Property createProperty(RegisterPropertyCommand command) {
        Instant now = timeProvider.now();

        return Property.forNew(
            command.partnerId(),
            command.brandId(),
            command.propertyTypeId(),
            command.name(),
            command.description(),
            command.location(),
            command.promotionText(),
            now
        );
    }

    /**
     * 사진 도메인 객체 리스트를 생성한다.
     * command.propertyId()가 이미 PropertyId VO이므로 변환 불필요.
     */
    public List<PropertyPhoto> createPhotos(AddPropertyPhotosCommand command) {
        PropertyId propertyId = command.propertyId();
        Instant now = timeProvider.now();

        return command.photos().stream()
            .map(p -> PropertyPhoto.forNew(
                propertyId,
                PhotoType.valueOf(p.photoType()),
                OriginUrl.of(p.originUrl()),
                p.cdnUrl() != null ? CdnUrl.of(p.cdnUrl()) : null,
                p.sortOrder(),
                now))
            .toList();
    }

    /**
     * 편의시설 도메인 객체 리스트를 생성한다.
     * command.propertyId()가 이미 PropertyId VO이므로 변환 불필요.
     */
    public List<PropertyAmenity> createAmenities(SetPropertyAmenitiesCommand command) {
        PropertyId propertyId = command.propertyId();
        return command.amenities().stream()
            .map(a -> PropertyAmenity.forNew(
                propertyId,
                AmenityType.valueOf(a.amenityType()),
                AmenityName.of(a.name()),
                Money.of(a.additionalPrice()),
                a.sortOrder()))
            .toList();
    }

    /**
     * 속성값 도메인 객체 리스트를 생성한다.
     * command.propertyId()가 이미 PropertyId VO이므로 변환 불필요.
     */
    public List<PropertyAttributeValue> createAttributeValues(SetPropertyAttributesCommand command) {
        PropertyId propertyId = command.propertyId();
        Instant now = timeProvider.now();

        return command.attributeValues().stream()
            .map(av -> PropertyAttributeValue.forNew(
                propertyId,
                PropertyTypeAttributeId.of(av.propertyTypeAttributeId()),
                av.value(),
                now))
            .toList();
    }
}
```

**변경 포인트**: 이전에는 Factory가 `PropertyCreateResult(Property, List<Amenity>, List<Photo>, List<AttributeValue>)`를 한번에 반환했다. 이제 각 도메인 객체 생성 메서드가 독립적이다. PropertyId가 null인 채로 하위 엔티티를 생성할 필요가 없어졌다 -- 사진/편의시설/속성값은 Property가 이미 저장된 후에 호출되므로 PropertyId가 항상 존재한다.

**TimeProvider 정의 (core 또는 application):**
```java
public interface TimeProvider {
    Instant now();
    java.time.LocalDate today();
}
```

### 4단계: Manager (트랜잭션 경계)

패키지:
- Property 관련 Manager: `application/src/main/java/com/ryuqq/otatoy/application/property/manager/`
- Partner 관련 Manager: `application/src/main/java/com/ryuqq/otatoy/application/partner/manager/`
- PropertyType 관련 Manager: `application/src/main/java/com/ryuqq/otatoy/application/propertytype/manager/`

> manager 하위에 `command/`, `read/` 서브패키지를 두지 않는다. 네이밍 컨벤션으로 역할이 구분된다.

| 클래스명 | 패키지 (BC) | 유형 | 의존성 | @Transactional | 적용 컨벤션 |
|---------|-----------|------|-------|:--------------:|-----------|
| `PropertyCommandManager` | `property/manager/` | CommandManager | `PropertyCommandPort` | 메서드 단위 필수 | APP-MGR-001 |
| `PropertyAmenityCommandManager` | `property/manager/` | CommandManager | `PropertyAmenityCommandPort` | 메서드 단위 필수 | APP-MGR-001 |
| `PropertyPhotoCommandManager` | `property/manager/` | CommandManager | `PropertyPhotoCommandPort` | 메서드 단위 필수 | APP-MGR-001 |
| `PropertyAttributeValueCommandManager` | `property/manager/` | CommandManager | `PropertyAttributeValueCommandPort` | 메서드 단위 필수 | APP-MGR-001 |
| `PropertyReadManager` | `property/manager/` | ReadManager | `PropertyQueryPort` | 메서드 단위 readOnly=true | APP-MGR-001 |
| `PartnerReadManager` | `partner/manager/` | ReadManager | `PartnerQueryPort` | 메서드 단위 readOnly=true | APP-MGR-001, APP-BC-001 |
| `PropertyTypeReadManager` | `propertytype/manager/` | ReadManager | `PropertyTypeQueryPort` | 메서드 단위 readOnly=true | APP-MGR-001, APP-BC-001 |

```java
// PropertyCommandManager.java — 단일 Aggregate 저장, @Transactional 메서드 단위 (APP-MGR-001)
package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyCommandPort;
import com.ryuqq.otatoy.domain.property.Property;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PropertyCommandManager {

    private final PropertyCommandPort propertyCommandPort;

    public PropertyCommandManager(PropertyCommandPort propertyCommandPort) {
        this.propertyCommandPort = propertyCommandPort;
    }

    @Transactional
    public Long persist(Property property) {
        return propertyCommandPort.persist(property);
    }

    @Transactional
    public void persistAll(List<Property> properties) {
        propertyCommandPort.persistAll(properties);
    }
}

// PropertyPhotoCommandManager.java
package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyPhotoCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PropertyPhotoCommandManager {

    private final PropertyPhotoCommandPort propertyPhotoCommandPort;

    public PropertyPhotoCommandManager(PropertyPhotoCommandPort propertyPhotoCommandPort) {
        this.propertyPhotoCommandPort = propertyPhotoCommandPort;
    }

    @Transactional
    public void persistAll(List<PropertyPhoto> photos) {
        propertyPhotoCommandPort.persistAll(photos);
    }
}

// PropertyAmenityCommandManager.java
package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyAmenityCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PropertyAmenityCommandManager {

    private final PropertyAmenityCommandPort propertyAmenityCommandPort;

    public PropertyAmenityCommandManager(PropertyAmenityCommandPort propertyAmenityCommandPort) {
        this.propertyAmenityCommandPort = propertyAmenityCommandPort;
    }

    @Transactional
    public void persistAll(List<PropertyAmenity> amenities) {
        propertyAmenityCommandPort.persistAll(amenities);
    }
}

// PropertyAttributeValueCommandManager.java
package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyAttributeValueCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PropertyAttributeValueCommandManager {

    private final PropertyAttributeValueCommandPort propertyAttributeValueCommandPort;

    public PropertyAttributeValueCommandManager(
            PropertyAttributeValueCommandPort propertyAttributeValueCommandPort) {
        this.propertyAttributeValueCommandPort = propertyAttributeValueCommandPort;
    }

    @Transactional
    public void persistAll(List<PropertyAttributeValue> attributeValues) {
        propertyAttributeValueCommandPort.persistAll(attributeValues);
    }
}

// PartnerReadManager.java
package com.ryuqq.otatoy.application.partner.manager;

import com.ryuqq.otatoy.application.partner.port.out.PartnerQueryPort;
import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PartnerReadManager {

    private final PartnerQueryPort partnerQueryPort;

    public PartnerReadManager(PartnerQueryPort partnerQueryPort) {
        this.partnerQueryPort = partnerQueryPort;
    }

    @Transactional(readOnly = true)
    public Partner getById(PartnerId id) {
        return partnerQueryPort.findById(id)
            .orElseThrow(PartnerNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public void verifyExists(PartnerId id) {
        if (!partnerQueryPort.existsById(id)) {
            throw new PartnerNotFoundException();
        }
    }
}

// PropertyTypeReadManager.java
package com.ryuqq.otatoy.application.propertytype.manager;

import com.ryuqq.otatoy.application.propertytype.port.out.PropertyTypeQueryPort;
import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PropertyTypeReadManager {

    private final PropertyTypeQueryPort propertyTypeQueryPort;

    public PropertyTypeReadManager(PropertyTypeQueryPort propertyTypeQueryPort) {
        this.propertyTypeQueryPort = propertyTypeQueryPort;
    }

    @Transactional(readOnly = true)
    public PropertyType getById(PropertyTypeId id) {
        return propertyTypeQueryPort.findById(id)
            .orElseThrow(() -> new PropertyTypeNotFoundException());
    }

    @Transactional(readOnly = true)
    public void verifyExists(PropertyTypeId id) {
        if (!propertyTypeQueryPort.existsById(id)) {
            throw new PropertyTypeNotFoundException();
        }
    }
}

// PropertyReadManager.java
package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyQueryPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PropertyReadManager {

    private final PropertyQueryPort propertyQueryPort;

    public PropertyReadManager(PropertyQueryPort propertyQueryPort) {
        this.propertyQueryPort = propertyQueryPort;
    }

    @Transactional(readOnly = true)
    public Property getById(PropertyId id) {
        return propertyQueryPort.findById(id)
            .orElseThrow(PropertyNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public void verifyExists(PropertyId id) {
        if (!propertyQueryPort.existsById(id)) {
            throw new PropertyNotFoundException();
        }
    }
}
```

**변경 포인트**:
1. `PropertyCommandManager`가 신규 추가되었다. 이전에는 PropertyPersistenceFacade가 모든 저장을 담당했지만, 단일 Aggregate 저장은 CommandManager로 충분하다. 사진/편의시설/속성값도 각각 독립 CommandManager를 가진다.
2. 모든 Manager의 `@Transactional`이 클래스 레벨에서 **메서드 레벨**로 변경되었다 (APP-MGR-001). 향후 트랜잭션이 필요 없는 메서드 추가나 전파 속성 분리에 유연하게 대응한다.
3. `PartnerReadManager`, `PropertyTypeReadManager`에 `verifyExists()` 메서드가 추가되었다. `PropertyRegistrationValidator`는 이 ReadManager들을 주입받아 검증을 담당한다. ReadManager의 `getById()`는 조회된 엔티티를 이후 로직에서 사용해야 하는 경우에, `verifyExists()`는 존재 확인만 필요한 경우에 사용한다.

### 5단계: Port-In (UseCase 인터페이스)

패키지: `application/src/main/java/com/ryuqq/otatoy/application/property/port/in/`

| 클래스명 | 역할 | 적용 컨벤션 |
|---------|------|-----------|
| `RegisterPropertyUseCase` | 숙소 기본정보 등록 | APP-UC-001 |
| `AddPropertyPhotosUseCase` | 사진 독립 업로드 | APP-UC-001 |
| `SetPropertyAmenitiesUseCase` | 편의시설 독립 설정 | APP-UC-001 |
| `SetPropertyAttributesUseCase` | 속성값 독립 설정 | APP-UC-001 |

```java
// RegisterPropertyUseCase.java
package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;

public interface RegisterPropertyUseCase {
    Long execute(RegisterPropertyCommand command);
}

// AddPropertyPhotosUseCase.java
package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.AddPropertyPhotosCommand;

public interface AddPropertyPhotosUseCase {
    void execute(AddPropertyPhotosCommand command);
}

// SetPropertyAmenitiesUseCase.java
package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;

public interface SetPropertyAmenitiesUseCase {
    void execute(SetPropertyAmenitiesCommand command);
}

// SetPropertyAttributesUseCase.java
package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;

public interface SetPropertyAttributesUseCase {
    void execute(SetPropertyAttributesCommand command);
}
```

### 6단계: Service (오케스트레이션)

패키지: `application/src/main/java/com/ryuqq/otatoy/application/property/service/`

```java
// PropertyRegistrationValidator.java — 숙소 등록 검증 전용 (APP-VAL-002)
package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.partner.manager.PartnerReadManager;
import com.ryuqq.otatoy.application.propertytype.manager.PropertyTypeReadManager;
import org.springframework.stereotype.Component;

@Component
public class PropertyRegistrationValidator {

    private final PartnerReadManager partnerReadManager;
    private final PropertyTypeReadManager propertyTypeReadManager;

    public PropertyRegistrationValidator(PartnerReadManager partnerReadManager,
                                          PropertyTypeReadManager propertyTypeReadManager) {
        this.partnerReadManager = partnerReadManager;
        this.propertyTypeReadManager = propertyTypeReadManager;
    }

    public void validate(RegisterPropertyCommand command) {
        partnerReadManager.verifyExists(command.partnerId());
        propertyTypeReadManager.verifyExists(command.propertyTypeId());
    }
}

// RegisterPropertyService.java — 기본정보만 저장 (Validator 사용)
package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyCommandManager;
import com.ryuqq.otatoy.application.property.validator.PropertyRegistrationValidator;
import com.ryuqq.otatoy.application.property.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.domain.property.Property;
import org.springframework.stereotype.Service;

@Service
public class RegisterPropertyService implements RegisterPropertyUseCase {

    private final PropertyRegistrationValidator validator;
    private final PropertyFactory propertyFactory;
    private final PropertyCommandManager propertyCommandManager;

    public RegisterPropertyService(PropertyRegistrationValidator validator,
                                    PropertyFactory propertyFactory,
                                    PropertyCommandManager propertyCommandManager) {
        this.validator = validator;
        this.propertyFactory = propertyFactory;
        this.propertyCommandManager = propertyCommandManager;
    }

    @Override
    public Long execute(RegisterPropertyCommand command) {
        // 1. 검증 (Validator — ReadManager.verifyExists 경유)
        validator.validate(command);

        // 2. 도메인 객체 생성 (Factory — TimeProvider)
        Property property = propertyFactory.createProperty(command);

        // 3. 저장 (CommandManager — @Transactional 메서드 단위)
        return propertyCommandManager.persist(property);
    }
}

// PropertyExistenceValidator.java — Property 존재 검증 전용 (APP-VAL-002)
package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.domain.property.PropertyId;
import org.springframework.stereotype.Component;

@Component
public class PropertyExistenceValidator {

    private final PropertyReadManager propertyReadManager;

    public PropertyExistenceValidator(PropertyReadManager propertyReadManager) {
        this.propertyReadManager = propertyReadManager;
    }

    public void validate(PropertyId propertyId) {
        propertyReadManager.verifyExists(propertyId);
    }
}

// AddPropertyPhotosService.java — 사진 독립 업로드 (Validator 사용)
package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.AddPropertyPhotosCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyPhotoCommandManager;
import com.ryuqq.otatoy.application.property.validator.PropertyExistenceValidator;
import com.ryuqq.otatoy.application.property.port.in.AddPropertyPhotosUseCase;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddPropertyPhotosService implements AddPropertyPhotosUseCase {

    private final PropertyExistenceValidator validator;
    private final PropertyFactory propertyFactory;
    private final PropertyPhotoCommandManager propertyPhotoCommandManager;

    public AddPropertyPhotosService(PropertyExistenceValidator validator,
                                     PropertyFactory propertyFactory,
                                     PropertyPhotoCommandManager propertyPhotoCommandManager) {
        this.validator = validator;
        this.propertyFactory = propertyFactory;
        this.propertyPhotoCommandManager = propertyPhotoCommandManager;
    }

    @Override
    public void execute(AddPropertyPhotosCommand command) {
        // 1. Property 존재 확인 (Validator — ReadManager.verifyExists)
        validator.validate(command.propertyId());

        // 2. 도메인 객체 생성 (Factory — TimeProvider)
        List<PropertyPhoto> photos = propertyFactory.createPhotos(command);

        // 3. 저장 (CommandManager — @Transactional 메서드 단위)
        propertyPhotoCommandManager.persistAll(photos);
    }
}

// SetPropertyAmenitiesService.java — 편의시설 독립 설정 (PropertyExistenceValidator 재사용)
package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyAmenityCommandManager;
import com.ryuqq.otatoy.application.property.validator.PropertyExistenceValidator;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAmenitiesUseCase;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetPropertyAmenitiesService implements SetPropertyAmenitiesUseCase {

    private final PropertyExistenceValidator validator;
    private final PropertyFactory propertyFactory;
    private final PropertyAmenityCommandManager propertyAmenityCommandManager;

    public SetPropertyAmenitiesService(PropertyExistenceValidator validator,
                                        PropertyFactory propertyFactory,
                                        PropertyAmenityCommandManager propertyAmenityCommandManager) {
        this.validator = validator;
        this.propertyFactory = propertyFactory;
        this.propertyAmenityCommandManager = propertyAmenityCommandManager;
    }

    @Override
    public void execute(SetPropertyAmenitiesCommand command) {
        // 1. Property 존재 확인 (Validator — ReadManager.verifyExists)
        validator.validate(command.propertyId());

        // 2. 도메인 객체 생성 (Factory)
        List<PropertyAmenity> amenities = propertyFactory.createAmenities(command);

        // 3. 저장 (CommandManager — @Transactional 메서드 단위)
        propertyAmenityCommandManager.persistAll(amenities);
    }
}

// SetPropertyAttributesService.java — 속성값 독립 설정 (PropertyExistenceValidator 재사용)
package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyAttributeValueCommandManager;
import com.ryuqq.otatoy.application.property.validator.PropertyExistenceValidator;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAttributesUseCase;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetPropertyAttributesService implements SetPropertyAttributesUseCase {

    private final PropertyExistenceValidator validator;
    private final PropertyFactory propertyFactory;
    private final PropertyAttributeValueCommandManager propertyAttributeValueCommandManager;

    public SetPropertyAttributesService(PropertyExistenceValidator validator,
                                         PropertyFactory propertyFactory,
                                         PropertyAttributeValueCommandManager propertyAttributeValueCommandManager) {
        this.validator = validator;
        this.propertyFactory = propertyFactory;
        this.propertyAttributeValueCommandManager = propertyAttributeValueCommandManager;
    }

    @Override
    public void execute(SetPropertyAttributesCommand command) {
        // 1. Property 존재 확인 (Validator — ReadManager.verifyExists)
        validator.validate(command.propertyId());

        // 2. 도메인 객체 생성 (Factory — TimeProvider)
        List<PropertyAttributeValue> attributeValues =
            propertyFactory.createAttributeValues(command);

        // 3. 저장 (CommandManager — @Transactional 메서드 단위)
        propertyAttributeValueCommandManager.persistAll(attributeValues);
    }
}
```

**변경 포인트**:
1. 이전에는 `RegisterPropertyService` 하나가 모든 것을 처리했다. 이제 4개의 독립 Service가 각각의 UseCase를 구현한다.
2. 존재 확인은 Validator가 ReadManager의 `verifyExists()` 메서드를 호출하는 패턴으로 변경되었다 (APP-VAL-002). RegisterPropertyService는 `PropertyRegistrationValidator`로 파트너/숙소유형 존재를 확인하고, 나머지 Service는 `PropertyExistenceValidator`로 Property 존재를 확인한다. Validator는 @Transactional 없이 ReadManager에 트랜잭션 관리를 위임한다.
3. 각 Service의 흐름이 단순해졌다: "검증(Validator) -> 도메인 생성(Factory) -> 저장(CommandManager)".

`PropertyPersistenceFacade`는 이 Phase에서 불필요해졌다. 향후 예약 생성(STORY-302)처럼 여러 Aggregate를 원자적으로 저장해야 하는 시점에 `ReservationPersistenceFacade`를 도입한다.

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
| APP-UC-001 | UseCase 인터페이스 1:1 Service | RegisterPropertyUseCase <-> RegisterPropertyService 등 4쌍 |
| APP-SVC-001 | Service에 @Transactional 금지 | 4개 Service 모두 @Transactional 없음 |
| APP-MGR-001 | CommandManager @Transactional 메서드 단위 필수 | PropertyCommandManager 등 4개, 클래스 레벨 금지 |
| APP-MGR-001 | ReadManager @Transactional(readOnly=true) 메서드 단위 | PartnerReadManager, PropertyTypeReadManager, PropertyReadManager |
| APP-VAL-002 | Validator — ReadManager 주입, verifyExists 경유 | PropertyRegistrationValidator, PropertyExistenceValidator |
| APP-VAL-002 | Validator @Transactional 없음 | ReadManager가 트랜잭션 관리 |
| APP-FAC-001 | TimeProvider는 Factory에만 주입 | PropertyFactory만 TimeProvider 의존 |
| APP-PRT-001 | CommandPort는 persist/persistAll만 | delete 메서드 없음 |
| APP-PRT-001 | QueryPort에 existsById 메서드 | PartnerQueryPort, PropertyTypeQueryPort, PropertyQueryPort |
| APP-PRT-002 | Port 파라미터는 Domain 객체 | Property, PropertyAmenity 등 도메인 객체 전달 |
| APP-DTO-001 | Command는 record, 필드에 Domain VO 사용 | 4개 Command 모두 record, PartnerId/PropertyTypeId/PropertyName 등 VO |
| APP-BC-001 | Port 직접 호출 금지 | Service/Validator 모두 Port 직접 의존 없음, Manager 경유 |

### STORY-103 최종 파일 목록 (생성 순서)

```
application/src/main/java/com/ryuqq/otatoy/application/
├── property/                              ← Property BC
│   ├── port/
│   │   ├── in/
│   │   │   ├── RegisterPropertyUseCase.java
│   │   │   ├── AddPropertyPhotosUseCase.java
│   │   │   ├── SetPropertyAmenitiesUseCase.java
│   │   │   └── SetPropertyAttributesUseCase.java
│   │   └── out/
│   │       ├── PropertyCommandPort.java
│   │       ├── PropertyQueryPort.java
│   │       ├── PropertyAmenityCommandPort.java
│   │       ├── PropertyPhotoCommandPort.java
│   │       └── PropertyAttributeValueCommandPort.java
│   ├── dto/
│   │   └── command/
│   │       ├── RegisterPropertyCommand.java
│   │       ├── AddPropertyPhotosCommand.java
│   │       ├── SetPropertyAmenitiesCommand.java
│   │       └── SetPropertyAttributesCommand.java
│   ├── factory/
│   │   └── PropertyFactory.java
│   ├── validator/
│   │   ├── PropertyRegistrationValidator.java    (신규 — APP-VAL-002)
│   │   └── PropertyExistenceValidator.java       (신규 — APP-VAL-002)
│   ├── manager/                           ← 플랫 (command/read 나누지 않음)
│   │   ├── PropertyCommandManager.java
│   │   ├── PropertyReadManager.java
│   │   ├── PropertyPhotoCommandManager.java
│   │   ├── PropertyAmenityCommandManager.java
│   │   └── PropertyAttributeValueCommandManager.java
│   └── service/
│       ├── RegisterPropertyService.java
│       ├── AddPropertyPhotosService.java
│       ├── SetPropertyAmenitiesService.java
│       └── SetPropertyAttributesService.java
├── partner/                               ← Partner BC
│   ├── port/out/
│   │   └── PartnerQueryPort.java
│   └── manager/
│       └── PartnerReadManager.java
├── propertytype/                          ← PropertyType BC
│   ├── port/out/
│   │   └── PropertyTypeQueryPort.java
│   └── manager/
│       └── PropertyTypeReadManager.java
└── common/                                ← 공통
    └── factory/
        └── TimeProvider.java              (또는 core 모듈)
```

총 25개 파일 (Validator 2개 추가).

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
| `PropertyTypeJpaEntity` | `BaseAuditEntity` | property_type 테이블 | PER-ENT-001, 004, 005 |

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

    /**
     * 존재 여부만 확인한다. 엔티티 전체를 로딩하지 않고 count 쿼리로 처리.
     * ReadManager의 verifyExists() 내부에서 사용한다 (APP-VAL-002).
     */
    public boolean existsById(Long id) {
        Integer result = queryFactory
            .selectOne()
            .from(propertyJpaEntity)
            .where(
                propertyJpaEntity.id.eq(id),
                propertyJpaEntity.deleted.isFalse()
            )
            .fetchFirst();
        return result != null;
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

    @Override
    public boolean existsById(PropertyId id) {
        return queryDslRepository.existsById(id.value());
    }
}
```

### STORY-104 Flyway DDL 확인

이미 존재하는 마이그레이션 파일:
- `V202604041001__create_property_tables.sql` -- property, property_attribute_value, property_amenity, property_photo
- `V202604041007__create_partner_tables.sql` -- partner, partner_member
- `V202604041000__create_brand_and_property_type_tables.sql` -- brand, property_type

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

### 수용기준 재정의

기존 수용기준 AC-4(RegisterPropertyApiRequest에 편의시설/사진/속성값 리스트 포함)는 폐기한다. API도 단계별 독립 엔드포인트로 분리한다.

**변경된 수용기준**:
- AC-1: POST /api/v1/extranet/properties 요청 시 201 응답 + ApiResponse 래핑 + data에 propertyId 반환
- AC-2: 필수 필드(name, propertyTypeId, partnerId, address) 누락 시 400 응답
- AC-3: ExtranetPropertyController는 UseCase 인터페이스만 의존
- AC-4: RegisterPropertyApiRequest는 record로 선언. **기본정보 필드만 포함**
- AC-5: 사진/편의시설/속성값은 각각 독립 엔드포인트로 분리
- AC-6: POST /api/v1/extranet/properties/{id}/photos (사진 업로드)
- AC-7: POST /api/v1/extranet/properties/{id}/amenities (편의시설 설정)
- AC-8: POST /api/v1/extranet/properties/{id}/attributes (속성값 설정)
- AC-9: PropertyApiMapper.toCommand()로 Request -> Command 변환
- AC-10: DomainException 발생 시 GlobalExceptionHandler가 적절한 HTTP 상태 코드 반환

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
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorMapper errorMapper;

    public GlobalExceptionHandler(ErrorMapper errorMapper) {
        this.errorMapper = errorMapper;
    }

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
        String code = e.getErrorCode().getCode();
        if (code.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
```

### 3단계: Request/Response DTO

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/extranet/dto/`

```java
// RegisterPropertyApiRequest.java — 기본정보만 포함
package com.ryuqq.otatoy.api.extranet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
    String promotionText         // nullable
) {}

// AddPropertyPhotosApiRequest.java — 사진 독립 업로드
package com.ryuqq.otatoy.api.extranet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddPropertyPhotosApiRequest(

    @NotEmpty(message = "사진은 최소 1개 이상 필요합니다")
    @Valid
    List<PhotoApiRequest> photos
) {
    public record PhotoApiRequest(
        @NotBlank(message = "사진 유형은 필수입니다")
        String photoType,
        @NotBlank(message = "원본 URL은 필수입니다")
        String originUrl,
        String cdnUrl,
        int sortOrder
    ) {}
}

// SetPropertyAmenitiesApiRequest.java — 편의시설 독립 설정
package com.ryuqq.otatoy.api.extranet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SetPropertyAmenitiesApiRequest(

    @NotEmpty(message = "편의시설은 최소 1개 이상 필요합니다")
    @Valid
    List<AmenityApiRequest> amenities
) {
    public record AmenityApiRequest(
        @NotBlank(message = "편의시설 유형은 필수입니다")
        String amenityType,
        @NotBlank(message = "편의시설 이름은 필수입니다")
        String name,
        int additionalPrice,
        int sortOrder
    ) {}
}

// SetPropertyAttributesApiRequest.java — 속성값 독립 설정
package com.ryuqq.otatoy.api.extranet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetPropertyAttributesApiRequest(

    @NotEmpty(message = "속성값은 최소 1개 이상 필요합니다")
    @Valid
    List<AttributeValueApiRequest> attributeValues
) {
    public record AttributeValueApiRequest(
        @NotNull(message = "속성 ID는 필수입니다")
        Long propertyTypeAttributeId,
        @NotBlank(message = "속성값은 필수입니다")
        String value
    ) {}
}
```

**변경 포인트**: 이전에는 `RegisterPropertyApiRequest` 하나에 편의시설/사진/속성값이 중첩되어 있었다. 이제 각각 독립 Request로 분리하고, propertyId는 URL path variable로 전달받는다.

### 4단계: ApiMapper

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/extranet/mapper/`

```java
// PropertyApiMapper.java — 원시 타입 -> Domain VO 변환 (APP-DTO-001)
package com.ryuqq.otatoy.api.extranet.mapper;

import com.ryuqq.otatoy.api.extranet.dto.*;
import com.ryuqq.otatoy.application.property.dto.command.*;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.*;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import java.util.List;

public class PropertyApiMapper {

    private PropertyApiMapper() {} // 인스턴스 생성 방지

    public static RegisterPropertyCommand toCommand(RegisterPropertyApiRequest request) {
        return RegisterPropertyCommand.of(
            PartnerId.of(request.partnerId()),
            request.brandId() != null ? BrandId.of(request.brandId()) : null,
            PropertyTypeId.of(request.propertyTypeId()),
            PropertyName.of(request.name()),
            request.description() != null ? PropertyDescription.of(request.description()) : null,
            Location.of(request.address(), request.latitude(), request.longitude(),
                        request.neighborhood(), request.region()),
            request.promotionText() != null ? PromotionText.of(request.promotionText()) : null
        );
    }

    public static AddPropertyPhotosCommand toCommand(Long propertyId,
                                                       AddPropertyPhotosApiRequest request) {
        List<AddPropertyPhotosCommand.PhotoItem> photos = request.photos().stream()
            .map(p -> new AddPropertyPhotosCommand.PhotoItem(
                p.photoType(), p.originUrl(), p.cdnUrl(), p.sortOrder()))
            .toList();
        return AddPropertyPhotosCommand.of(PropertyId.of(propertyId), photos);
    }

    public static SetPropertyAmenitiesCommand toCommand(Long propertyId,
                                                          SetPropertyAmenitiesApiRequest request) {
        List<SetPropertyAmenitiesCommand.AmenityItem> amenities = request.amenities().stream()
            .map(a -> new SetPropertyAmenitiesCommand.AmenityItem(
                a.amenityType(), a.name(), a.additionalPrice(), a.sortOrder()))
            .toList();
        return SetPropertyAmenitiesCommand.of(PropertyId.of(propertyId), amenities);
    }

    public static SetPropertyAttributesCommand toCommand(Long propertyId,
                                                            SetPropertyAttributesApiRequest request) {
        List<SetPropertyAttributesCommand.AttributeValueItem> attributeValues =
            request.attributeValues().stream()
                .map(av -> new SetPropertyAttributesCommand.AttributeValueItem(
                    av.propertyTypeAttributeId(), av.value()))
                .toList();
        return SetPropertyAttributesCommand.of(PropertyId.of(propertyId), attributeValues);
    }
}
```

### 5단계: Controller

패키지: `adapter-in/rest-api/src/main/java/com/ryuqq/otatoy/api/extranet/`

```java
// ExtranetPropertyController.java
package com.ryuqq.otatoy.api.extranet;

import com.ryuqq.otatoy.api.common.ApiResponse;
import com.ryuqq.otatoy.api.extranet.dto.*;
import com.ryuqq.otatoy.api.extranet.mapper.PropertyApiMapper;
import com.ryuqq.otatoy.application.property.port.in.*;
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
    private final AddPropertyPhotosUseCase addPropertyPhotosUseCase;
    private final SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase;
    private final SetPropertyAttributesUseCase setPropertyAttributesUseCase;

    public ExtranetPropertyController(RegisterPropertyUseCase registerPropertyUseCase,
                                       AddPropertyPhotosUseCase addPropertyPhotosUseCase,
                                       SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase,
                                       SetPropertyAttributesUseCase setPropertyAttributesUseCase) {
        this.registerPropertyUseCase = registerPropertyUseCase;
        this.addPropertyPhotosUseCase = addPropertyPhotosUseCase;
        this.setPropertyAmenitiesUseCase = setPropertyAmenitiesUseCase;
        this.setPropertyAttributesUseCase = setPropertyAttributesUseCase;
    }

    @Operation(summary = "숙소 기본정보 등록", description = "파트너가 숙소 기본정보를 등록합니다")
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

        Long propertyId = registerPropertyUseCase.execute(
            PropertyApiMapper.toCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(propertyId));
    }

    @Operation(summary = "숙소 사진 업로드", description = "등록된 숙소에 사진을 추가합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "업로드 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    @PostMapping("/{propertyId}/photos")
    public ResponseEntity<ApiResponse<Void>> addPhotos(
            @PathVariable Long propertyId,
            @Valid @RequestBody AddPropertyPhotosApiRequest request) {

        addPropertyPhotosUseCase.execute(
            PropertyApiMapper.toCommand(propertyId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "숙소 편의시설 설정", description = "등록된 숙소의 편의시설을 설정합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    @PostMapping("/{propertyId}/amenities")
    public ResponseEntity<ApiResponse<Void>> setAmenities(
            @PathVariable Long propertyId,
            @Valid @RequestBody SetPropertyAmenitiesApiRequest request) {

        setPropertyAmenitiesUseCase.execute(
            PropertyApiMapper.toCommand(propertyId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "숙소 속성값 설정", description = "등록된 숙소의 속성값을 설정합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    @PostMapping("/{propertyId}/attributes")
    public ResponseEntity<ApiResponse<Void>> setAttributes(
            @PathVariable Long propertyId,
            @Valid @RequestBody SetPropertyAttributesApiRequest request) {

        setPropertyAttributesUseCase.execute(
            PropertyApiMapper.toCommand(propertyId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

**금지 사항 확인**:
- @Transactional 없음 (API-CTR-001)
- 비즈니스 로직 없음 (API-CTR-001)
- UseCase 인터페이스만 의존 (API-CTR-001)
- 인라인 변환 로직 없음 -- ApiMapper 사용 (API-DTO-001)

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
| API-CTR-001 | UseCase만 의존 | 4개 UseCase 인터페이스만 주입 |
| API-CTR-001 | @Transactional 금지 | Controller에 없음 |
| API-CTR-001 | @DeleteMapping 금지 | 사용 안 함 |
| API-CTR-002 | 201 응답 + ApiResponse 래핑 | POST (기본정보 등록) -> CREATED |
| API-DTO-001 | DTO는 record | 4개 ApiRequest 모두 record |
| API-DTO-001 | ApiMapper로 변환 | PropertyApiMapper.toCommand() 4개 오버로드 |
| API-DTO-002 | Jakarta Validation | @NotNull, @NotBlank, @Size, @NotEmpty |
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
    │   ├── RegisterPropertyApiRequest.java          (신규)
    │   ├── AddPropertyPhotosApiRequest.java         (신규)
    │   ├── SetPropertyAmenitiesApiRequest.java      (신규)
    │   └── SetPropertyAttributesApiRequest.java     (신규)
    └── mapper/
        └── PropertyApiMapper.java                   (신규)
```

총 10개 파일.

---

## 전체 Phase 2 파일 개수 요약

| Story | 신규 파일 수 | 수정 파일 수 | 레이어 |
|-------|:----------:|:----------:|-------|
| STORY-103 | 25 | 0 | Application (Validator 2개 추가) |
| STORY-104 | 23 | 1 (JpaConfig) | Persistence |
| STORY-105 | 10 | 0 | API |
| **합계** | **58** | **1** | |

---

## API 엔드포인트 요약

| 메서드 | 엔드포인트 | UseCase | 설명 |
|--------|----------|---------|------|
| POST | /api/v1/extranet/properties | RegisterPropertyUseCase | 숙소 기본정보 등록 |
| POST | /api/v1/extranet/properties/{id}/photos | AddPropertyPhotosUseCase | 사진 업로드 |
| POST | /api/v1/extranet/properties/{id}/amenities | SetPropertyAmenitiesUseCase | 편의시설 설정 |
| POST | /api/v1/extranet/properties/{id}/attributes | SetPropertyAttributesUseCase | 속성값 설정 |
| POST | /api/v1/extranet/properties/{id}/rooms | RegisterRoomTypeUseCase | 객실 등록 (STORY-106) |

실제 OTA 플랫폼과 동일하게, 파트너는 기본정보를 먼저 등록하여 PropertyId를 받은 뒤, 사진/편의시설/속성값/객실을 순서 상관없이 독립적으로 추가할 수 있다. 중간 저장이 자연스럽게 지원되며, 각 단계를 개별적으로 수정할 수 있다.

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

### UseCase 분리 vs 통합 (변경됨)

**선택**: UseCase를 독립 생명주기 단위로 분리 (RegisterProperty, AddPhotos, SetAmenities, SetAttributes)

**대안 (이전 방식)**: RegisterPropertyUseCase 하나에 기본정보 + 편의시설 + 사진 + 속성값을 모두 포함
- 장점: API 호출 1회로 모든 데이터 저장
- 단점: 실제 OTA 등록 플로우와 불일치, 사진 실패가 기본정보 저장을 롤백, Command가 비대해짐, PersistenceFacade를 단일 Aggregate 저장에도 사용하게 됨

**결정 이유**: OTA 리서치(`docs/research/ota-extranet-registration-flow.md`)에 따르면 Booking.com과 야놀자 모두 단계별 별도 저장이다. 사진/편의시설/속성값은 숙소 기본정보와 생명주기가 독립적이다. 각각의 UseCase가 단순해지고, 트랜잭션 범위가 최소화된다.

### PersistenceFacade 제거 (변경됨)

**선택**: Phase 2에서 PropertyPersistenceFacade를 사용하지 않음

**대안 (이전 방식)**: PropertyPersistenceFacade에서 Property + 하위 엔티티를 원자적 저장
- 장점: 하나의 트랜잭션에서 모든 데이터 일관성 보장
- 단점: 단일 Aggregate 저장에 PersistenceFacade를 사용하는 것은 APP-FCD-001의 취지와 다름

**결정 이유**: Application 컨벤션(APP-FCD-001)에 따르면 PersistenceFacade는 "여러 Aggregate를 하나의 트랜잭션에서 원자적으로 저장해야 할 때" 사용한다. Property 기본정보는 단일 Aggregate이므로 CommandManager로 충분하다. PersistenceFacade는 STORY-302(예약 생성 + Outbox)처럼 진정한 크로스 Aggregate 원자성이 필요한 시점에 도입한다.

### 하위 엔티티 PropertyId 문제 해소 (변경됨)

**이전 문제**: Factory에서 PropertyId가 null인 채로 하위 엔티티를 생성하고, PersistenceFacade에서 재생성해야 했다.

**현재**: 사진/편의시설/속성값은 Property가 이미 저장된 후에 별도 API로 호출되므로, PropertyId가 항상 존재한다. Factory에서 PropertyId를 파라미터로 받아 정상적으로 도메인 객체를 생성한다. 불변 객체 재생성 비용이 사라졌다.

### ErrorCode HTTP 상태 매핑

**현재**: GlobalExceptionHandler에서 ErrorCode 문자열 패턴으로 매핑 (임시)
**향후**: ErrorCode 인터페이스에 `int getHttpStatus()` 추가하여 각 ErrorCode가 자신의 HTTP 상태를 선언 (Phase 1 보완 시)

---

## 백로그 수용기준 갱신 필요 사항

이 가이드의 변경에 따라 `docs/backlog.md`의 다음 수용기준을 갱신해야 한다:

### STORY-103 갱신 대상
- **AC-8 폐기**: "PropertyPersistenceFacade가 Property + 하위 엔티티를 원자적으로 저장" -> "PropertyCommandManager가 Property 기본정보를 단독 저장"
- **AC-9 수정**: "편의시설/사진/속성값 리스트를 포함하는 중첩 record" -> "기본정보 필드만 포함하는 record"
- **AC-10 폐기**: "PropertyFactory가 Property + 일급 컬렉션을 함께 생성" -> "PropertyFactory.createProperty()가 Property만 생성"
- **AC-11~14 추가**: AddPropertyPhotosUseCase, SetPropertyAmenitiesUseCase, SetPropertyAttributesUseCase, Property 존재 확인 후 실행

### STORY-105 갱신 대상
- **AC-4 수정**: "RegisterPropertyApiRequest에 편의시설/사진/속성값 포함" -> "기본정보 필드만 포함"
- **AC-5 수정**: "PropertyApiMapper.toCommand()" -> "PropertyApiMapper.toCommand() 4개 오버로드"
- **AC-6~8 추가**: 사진/편의시설/속성값 독립 엔드포인트
