package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.repository.PricingSlabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Core pricing engine that:
 * 1. Generates valid contract-defined pricing options via PricingOptionGenerator
 * 2. Selects the best (minimum-cost) valid option via BestPricingSelector
 * 3. Builds the BillingCalculationResult
 *
 * All monetary values remain as Long (paisa) throughout.
 * BigDecimal is used only for intermediate non-monetary calculations.
 */
@Component
@RequiredArgsConstructor
public class PricingEngine {

    private final PricingSlabRepository pricingSlabRepository;
    private final PricingOptionGenerator optionGenerator;
    private final BestPricingSelector bestPricingSelector;

    /**
     * Calculate the full billing result for a single trip under the given contract version.
     *
     * @param trip            the trip to bill
     * @param contractVersion the version active on trip.tripDate
     * @return a complete BillingCalculationResult with all charge components in paisa
     */
    public BillingCalculationResult calculate(Trip trip, ContractVersion contractVersion) {
        List<PricingSlab> slabs = pricingSlabRepository
                .findByContractVersionIdOrderByFromValueAsc(contractVersion.getId());

        List<PricingOption> validOptions = optionGenerator.generateValidOptions(trip, contractVersion, slabs);
        PricingOption selectedOption = bestPricingSelector.selectBestOption(validOptions);

        StringBuilder explanationBuilder = new StringBuilder();
        explanationBuilder.append("SELECTED_OPTION: ").append(selectedOption.getOptionName())
                .append(" (").append(selectedOption.getTotalChargePaisa()).append(" paisa)");

        if (validOptions.size() > 1) {
            explanationBuilder.append(" | COMPARED_OPTIONS: [");
            for (int i = 0; i < validOptions.size(); i++) {
                PricingOption opt = validOptions.get(i);
                if (i > 0) explanationBuilder.append(", ");
                explanationBuilder.append(opt.getOptionName()).append("=").append(opt.getTotalChargePaisa()).append("paisa");
            }
            explanationBuilder.append("]");
        }
        explanationBuilder.append(" | ").append(selectedOption.getExplanation());

        return BillingCalculationResult.builder()
                .tripId(trip.getId())
                .externalTripId(trip.getExternalTripId())
                .contractVersionId(contractVersion.getId())
                .distanceKm(trip.getDistanceKm())
                .dutyHours(trip.getDutyHours())
                .waitingHours(trip.getWaitingHours())
                .tollAmountPaisa(trip.getTollAmountPaisa())
                .nightTrip(Boolean.TRUE.equals(trip.getNight()))
                .baseChargePaisa(selectedOption.getBaseChargePaisa())
                .nightChargePaisa(selectedOption.getNightChargePaisa())
                .waitingChargePaisa(selectedOption.getWaitingChargePaisa())
                .tollPassThroughPaisa(selectedOption.getTollPassThroughPaisa())
                .totalChargePaisa(selectedOption.getTotalChargePaisa())
                .description(selectedOption.getDescription())
                .explanation(explanationBuilder.toString())
                .build();
    }
}
