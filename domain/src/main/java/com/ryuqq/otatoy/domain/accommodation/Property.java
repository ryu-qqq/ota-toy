package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.location.Location;
import com.ryuqq.otatoy.domain.partner.PartnerId;

import java.time.Instant;
import java.util.Objects;

public class Property {

    private final PropertyId id;
    private final PartnerId partnerId;
    private BrandId brandId;
    private final PropertyTypeId propertyTypeId;
    private PropertyName name;
    private PropertyDescription description;
    private Location location;
    private PromotionText promotionText;
    private PropertyStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Property(PropertyId id, PartnerId partnerId, BrandId brandId, PropertyTypeId propertyTypeId,
                     PropertyName name, PropertyDescription description, Location location,
                     PromotionText promotionText, PropertyStatus status,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.brandId = brandId;
        this.propertyTypeId = propertyTypeId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.promotionText = promotionText;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Property forNew(PartnerId partnerId, BrandId brandId, PropertyTypeId propertyTypeId,
                                   PropertyName name, PropertyDescription description,
                                   Location location, PromotionText promotionText, Instant now) {
        return new Property(null, partnerId, brandId, propertyTypeId, name, description,
                location, promotionText, PropertyStatus.ACTIVE, now, now);
    }

    public static Property reconstitute(PropertyId id, PartnerId partnerId, BrandId brandId,
                                         PropertyTypeId propertyTypeId, PropertyName name,
                                         PropertyDescription description, Location location,
                                         PromotionText promotionText, PropertyStatus status,
                                         Instant createdAt, Instant updatedAt) {
        return new Property(id, partnerId, brandId, propertyTypeId, name, description,
                location, promotionText, status, createdAt, updatedAt);
    }

    public void rename(PropertyName newName, Instant now) {
        this.name = newName;
        this.updatedAt = now;
    }

    public void updateDescription(PropertyDescription newDescription, Instant now) {
        this.description = newDescription;
        this.updatedAt = now;
    }

    public void updateLocation(Location newLocation, Instant now) {
        this.location = newLocation;
        this.updatedAt = now;
    }

    public void updatePromotionText(PromotionText newPromotionText, Instant now) {
        this.promotionText = newPromotionText;
        this.updatedAt = now;
    }

    public void deactivate(Instant now) {
        this.status = PropertyStatus.INACTIVE;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = PropertyStatus.ACTIVE;
        this.updatedAt = now;
    }

    public boolean isActive() {
        return this.status == PropertyStatus.ACTIVE;
    }

    public PropertyId id() { return id; }
    public PartnerId partnerId() { return partnerId; }
    public BrandId brandId() { return brandId; }
    public PropertyTypeId propertyTypeId() { return propertyTypeId; }
    public PropertyName name() { return name; }
    public PropertyDescription description() { return description; }
    public Location location() { return location; }
    public PromotionText promotionText() { return promotionText; }
    public PropertyStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
