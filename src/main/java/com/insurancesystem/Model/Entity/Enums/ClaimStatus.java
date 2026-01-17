package com.insurancesystem.Model.Entity.Enums;

public enum ClaimStatus {
    PENDING, // بانتظار المراجعة الطبية
    PENDING_MEDICAL, // New: Pending medical review
    APPROVED_BY_MEDICAL, // تمت الموافقة الطبية
    APPROVED_MEDICAL, // New: Approved by medical (alias for APPROVED_BY_MEDICAL)
    REJECTED_BY_MEDICAL, // تم الرفض الطبي
    REJECTED_MEDICAL, // New: Rejected by medical (alias for REJECTED_BY_MEDICAL)
    AWAITING_ADMIN_REVIEW, // بانتظار المراجعة الإدارية
    AWAITING_COORDINATION_REVIEW, // New: Awaiting coordination review
    PENDING_COORDINATION, // New: Pending coordination review
    APPROVED, // تمت الموافقة النهائية
    APPROVED_FINAL, // New: Final approval (alias for APPROVED)
    REJECTED, // الرفض الإداري النهائي
    REJECTED_FINAL, // New: Final rejection (alias for REJECTED)
    RETURNED_FOR_REVIEW, // New: Returned from coordinator for medical re-review
    RETURNED_TO_PROVIDER, // New: Returned to provider
    PAYMENT_PENDING, // New: Payment pending
    PAID // New: Paid
}
