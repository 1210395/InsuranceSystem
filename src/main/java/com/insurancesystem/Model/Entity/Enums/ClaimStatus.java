package com.insurancesystem.Model.Entity.Enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum ClaimStatus {
    // Legacy statuses (for backward compatibility with existing data)
    PENDING,                  // Legacy: Initial submission
    APPROVED,                 // Legacy: Final approval
    REJECTED,                 // Legacy: Final rejection
    APPROVED_BY_MEDICAL,      // Legacy: Approved by medical admin

    // New workflow statuses
    PENDING_MEDICAL,          // New claims awaiting medical review
    PENDING_COORDINATION,     // Alias for awaiting coordination (frontend compatibility)
    AWAITING_COORDINATION_REVIEW,  // Medical approved - awaiting coordination review
    APPROVED_MEDICAL,         // Approved by medical admin (new workflow)
    REJECTED_MEDICAL,         // Rejected by medical admin (new workflow)
    APPROVED_FINAL,           // Final approval (after coordination approval)
    REJECTED_FINAL,           // Final rejection
    RETURNED_FOR_REVIEW,      // Returned from coordinator to medical admin
    RETURNED_TO_PROVIDER,     // Returned to provider for corrections
    PAYMENT_PENDING,          // Approved - awaiting payment
    PAID;                     // Payment completed

    private static final Map<ClaimStatus, Set<ClaimStatus>> VALID_TRANSITIONS = Map.ofEntries(
        Map.entry(PENDING, EnumSet.of(PENDING_MEDICAL, APPROVED_FINAL, REJECTED_FINAL, REJECTED_MEDICAL, AWAITING_COORDINATION_REVIEW)),
        Map.entry(PENDING_MEDICAL, EnumSet.of(AWAITING_COORDINATION_REVIEW, REJECTED_MEDICAL, RETURNED_TO_PROVIDER)),
        Map.entry(RETURNED_FOR_REVIEW, EnumSet.of(AWAITING_COORDINATION_REVIEW, REJECTED_MEDICAL, RETURNED_TO_PROVIDER)),
        Map.entry(AWAITING_COORDINATION_REVIEW, EnumSet.of(APPROVED_FINAL, REJECTED_FINAL, RETURNED_FOR_REVIEW)),
        Map.entry(APPROVED_FINAL, EnumSet.of(PAYMENT_PENDING, RETURNED_FOR_REVIEW)),
        Map.entry(PAYMENT_PENDING, EnumSet.of(PAID)),
        Map.entry(RETURNED_TO_PROVIDER, EnumSet.of(PENDING_MEDICAL))
    );

    public boolean canTransitionTo(ClaimStatus target) {
        Set<ClaimStatus> allowed = VALID_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }
}
