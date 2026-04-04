# ERD — OTA 숙박 플랫폼 (v2)

> v1 피드백 25개 반영. 정규화 + 구조 개선.

```mermaid
erDiagram
    Brand {
        long id PK
        string name
        string name_kr
        string logo_url
    }

    PropertyType {
        long id PK
        string code
        string name
        string description
    }

    PropertyTypeAttribute {
        long id PK
        long property_type_id FK
        string attribute_key
        string attribute_name
        string value_type
        int is_required
        int sort_order
    }

    Property {
        long id PK
        long partner_id FK
        long brand_id FK
        long property_type_id FK
        string name
        string description
        string address
        float latitude
        float longitude
        string neighborhood
        string region
        string status
        string promotion_text
    }

    PropertyAttributeValue {
        long id PK
        long property_id FK
        long property_type_attribute_id FK
        string value
    }

    Landmark {
        long id PK
        string name
        string landmark_type
        float latitude
        float longitude
    }

    PropertyLandmark {
        long id PK
        long property_id FK
        long landmark_id FK
        float distance_km
        int walking_minutes
    }

    RoomType {
        long id PK
        long property_id FK
        string name
        string description
        float area_sqm
        string area_pyeong
        int base_occupancy
        int max_occupancy
        int base_inventory
        string check_in_time
        string check_out_time
        string status
    }

    RoomTypeAttribute {
        long id PK
        long room_type_id FK
        string attribute_key
        string attribute_value
    }

    BedType {
        long id PK
        string code
        string name
    }

    ViewType {
        long id PK
        string code
        string name
    }

    RoomTypeBed {
        long id PK
        long room_type_id FK
        long bed_type_id FK
        int quantity
    }

    RoomTypeView {
        long id PK
        long room_type_id FK
        long view_type_id FK
    }

    PropertyAmenity {
        long id PK
        long property_id FK
        string amenity_type
        string name
        float additional_price
        int sort_order
    }

    RoomAmenity {
        long id PK
        long room_type_id FK
        string amenity_type
        string name
        float additional_price
        int sort_order
    }

    PropertyPhoto {
        long id PK
        long property_id FK
        string photo_type
        string origin_url
        string cdn_url
        int sort_order
    }

    RoomPhoto {
        long id PK
        long room_type_id FK
        string photo_type
        string origin_url
        string cdn_url
        int sort_order
    }

    RatePlan {
        long id PK
        long room_type_id FK
        string name
        string source_type
        long supplier_id FK
        int is_free_cancellation
        int is_non_refundable
        string cancellation_policy_text
        string payment_policy
    }

    RatePlanAddOn {
        long id PK
        long rate_plan_id FK
        string add_on_type
        string name
        float price
        int is_included
    }

    RateRule {
        long id PK
        long rate_plan_id FK
        string start_date
        string end_date
        float weekday_price
        float friday_price
        float saturday_price
        float sunday_price
        float base_price
    }

    RateOverride {
        long id PK
        long rate_rule_id FK
        string override_date
        float price
        string reason
    }

    Rate {
        long id PK
        long rate_plan_id FK
        string rate_date
        float base_price
        string calculated_from
    }

    Inventory {
        long id PK
        long room_type_id FK
        string inventory_date
        int available_count
        int is_stop_sell
        int version
    }

    Reservation {
        long id PK
        long rate_plan_id FK
        string reservation_no
        string guest_name
        string guest_phone
        string guest_email
        string check_in_date
        string check_out_date
        int guest_count
        float total_amount
        string status
        string cancel_reason
        string booking_snapshot
        string created_at
        string cancelled_at
    }

    ReservationItem {
        long id PK
        long reservation_id FK
        long inventory_id FK
        string stay_date
    }

    Partner {
        long id PK
        string name
        string status
    }

    PartnerMember {
        long id PK
        long partner_id FK
        string name
        string email
        string phone
        string role
        string status
    }

    Supplier {
        long id PK
        string name
        string name_kr
        string company_title
        string owner_name
        string business_no
        string address
        string phone
        string email
        string terms_url
        string status
    }

    SupplierApiConfig {
        long id PK
        long supplier_id FK
        string api_base_url
        string api_key
        string auth_type
        int sync_interval_minutes
        string status
    }

    SupplierProperty {
        long id PK
        long supplier_id FK
        long property_id FK
        string supplier_property_id
        string last_synced_at
        string status
    }

    SupplierRoomType {
        long id PK
        long supplier_property_id FK
        long room_type_id FK
        string supplier_room_id
        string last_synced_at
        string status
    }

    SupplierSyncLog {
        long id PK
        long supplier_id FK
        string sync_type
        string synced_at
        string status
        int total_count
        int created_count
        int updated_count
        int deleted_count
        string error_message
    }

    ReservationOutbox {
        long id PK
        long reservation_id FK
        string event_type
        string payload
        string status
        int retry_count
        string created_at
        string updated_at
        string processed_at
    }

    SupplierOutbox {
        long id PK
        long supplier_id FK
        string event_type
        string payload
        string status
        int retry_count
        string created_at
        string updated_at
        string processed_at
    }

    Brand ||--o{ Property : has
    PropertyType ||--o{ Property : categorizes
    PropertyType ||--o{ PropertyTypeAttribute : defines
    Property ||--o{ PropertyAttributeValue : has
    PropertyTypeAttribute ||--o{ PropertyAttributeValue : fills
    Property ||--o{ PropertyLandmark : has
    Landmark ||--o{ PropertyLandmark : referenced
    Partner ||--o{ Property : owns
    Partner ||--o{ PartnerMember : has
    Property ||--o{ RoomType : has
    Property ||--o{ PropertyAmenity : has
    Property ||--o{ PropertyPhoto : has
    RoomType ||--o{ RoomAmenity : has
    RoomType ||--o{ RoomPhoto : has
    RoomType ||--o{ RoomTypeBed : has
    RoomType ||--o{ RoomTypeView : has
    BedType ||--o{ RoomTypeBed : referenced
    ViewType ||--o{ RoomTypeView : referenced
    RoomType ||--o{ RoomTypeAttribute : has
    RoomType ||--o{ RatePlan : has
    RoomType ||--o{ Inventory : has
    RatePlan ||--o{ RatePlanAddOn : has
    RatePlan ||--o{ RateRule : has
    RateRule ||--o{ RateOverride : has
    RatePlan ||--o{ Rate : calculated
    RatePlan ||--o{ Reservation : booked
    Reservation ||--o{ ReservationItem : contains
    ReservationItem }o--|| Inventory : reserves
    Supplier ||--o{ SupplierApiConfig : has
    Supplier ||--o{ SupplierProperty : provides
    Supplier ||--o{ SupplierSyncLog : logs
    Supplier ||--o{ RatePlan : supplies
    SupplierProperty ||--o{ SupplierRoomType : has
    SupplierProperty }o--|| Property : maps
    SupplierRoomType }o--|| RoomType : maps
    Reservation ||--o{ ReservationOutbox : publishes
    Supplier ||--o{ SupplierOutbox : publishes
```
