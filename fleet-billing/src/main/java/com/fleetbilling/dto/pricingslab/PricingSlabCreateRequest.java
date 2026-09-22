package com.fleetbilling.dto.pricingslab;

import com.fleetbilling.enums.PricingUnitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSlabCreateRequest {

    @NotNull(message = "Slab order is required")
    @Positive(message = "Slab order must be positive (>= 1)")
    private Integer slabOrder;

    @NotNull(message = "Minimum value is required")
    @PositiveOrZero(message = "Minimum value must be zero or positive")
    private Integer minValue;

    private Integer maxValue;

    @NotNull(message = "Rate in paisa is required")
    @PositiveOrZero(message = "Rate in paisa must be zero or positive")
    private Long ratePaisa;

    @NotNull(message = "Pricing unit type is required")
    private PricingUnitType unitType;

    // Getter helpers for fromValue / toValue compatibility
    public Integer getFromValue() {
        return minValue;
    }

    public Integer getToValue() {
        return maxValue;
    }
}
