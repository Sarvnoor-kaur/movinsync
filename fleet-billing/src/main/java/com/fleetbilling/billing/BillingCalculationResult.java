package com.fleetbilling.billing;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Internal value object carrying the full calculation breakdown for a single trip charge.
 * Used to populate InvoiceItem and provide billing audit trail.
 *
 * All monetary fields are in paisa (Long). Non-monetary decimals use BigDecimal.
 */
@Getter
@Builder
public class BillingCalculationResult {

    private final Long tripId;
    private final String externalTripId;
    private final Long contractVersionId;

    // Quantities used
    private final BigDecimal distanceKm;
    private final BigDecimal dutyHours;
    private final BigDecimal waitingHours;
    private final Long tollAmountPaisa;
    private final boolean nightTrip;

    // Charges in paisa
    private final long baseChargePaisa;
    private final long nightChargePaisa;
    private final long waitingChargePaisa;
    private final long tollPassThroughPaisa;
    private final long totalChargePaisa;

    // Description for the invoice item
    private final String description;

    // Machine-readable explanation for audit
    private final String explanation;
}
