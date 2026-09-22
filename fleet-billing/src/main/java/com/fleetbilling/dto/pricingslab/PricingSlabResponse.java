package com.fleetbilling.dto.pricingslab;

import com.fleetbilling.enums.PricingUnitType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSlabResponse {

    private Long id;
    private Long contractVersionId;
    private Integer slabOrder;
    private Integer minValue;
    private Integer maxValue;
    private Long ratePaisa;
    private PricingUnitType unitType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
