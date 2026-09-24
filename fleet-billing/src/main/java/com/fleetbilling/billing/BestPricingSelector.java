package com.fleetbilling.billing;

import com.fleetbilling.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Selects the best (lowest-cost) valid pricing option from a set of contract-permitted options.
 *
 * <p>Selection Determinism & Tie-Breaking Rules:
 * <ol>
 *   <li>Primary: Lowest {@code totalChargePaisa}</li>
 *   <li>Secondary Tie-Breaker: Lowest {@code priority} (configured priority order)</li>
 *   <li>Tertiary Tie-Breaker: Alphabetical {@code optionName}</li>
 * </ol>
 *
 * <p>The same trip, contract version, and pricing configuration will ALWAYS produce the exact same selected option.
 */
@Component
public class BestPricingSelector {

    /**
     * Select the minimum-cost valid option from the provided list.
     *
     * @param options list of valid contract-permitted pricing options
     * @return the selected PricingOption with {@code selected = true}
     */
    public PricingOption selectBestOption(List<PricingOption> options) {
        if (options == null || options.isEmpty()) {
            throw new BusinessException("No valid pricing options available to compare");
        }

        PricingOption best = options.stream()
                .min(Comparator.comparingLong(PricingOption::getTotalChargePaisa)
                        .thenComparingInt(PricingOption::getPriority)
                        .thenComparing(PricingOption::getOptionName))
                .orElseThrow(() -> new BusinessException("Failed to select best pricing option"));

        return PricingOption.builder()
                .optionName(best.getOptionName())
                .pricingMethod(best.getPricingMethod())
                .priority(best.getPriority())
                .baseChargePaisa(best.getBaseChargePaisa())
                .nightChargePaisa(best.getNightChargePaisa())
                .waitingChargePaisa(best.getWaitingChargePaisa())
                .tollPassThroughPaisa(best.getTollPassThroughPaisa())
                .totalChargePaisa(best.getTotalChargePaisa())
                .description(best.getDescription())
                .explanation(best.getExplanation())
                .selected(true)
                .build();
    }
}
