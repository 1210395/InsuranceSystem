package com.insurancesystem.Model.Entity.Enums;

public enum ClaimStatus {
    PENDING, // بانتظار المراجعة الطبية
    APPROVED_BY_MEDICAL, // تمت الموافقة الطبية
    REJECTED_BY_MEDICAL, // تم الرفض الطبي
    AWAITING_ADMIN_REVIEW, // بانتظار المراجعة الإدارية
    APPROVED, // تمت الموافقة النهائية
    REJECTED // الرفض الإداري النهائي
}
