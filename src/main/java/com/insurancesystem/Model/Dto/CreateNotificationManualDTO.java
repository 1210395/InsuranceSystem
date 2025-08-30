package com.insurancesystem.Model.Dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateNotificationManualDTO {
    private UUID recipientId;   // لمين الإشعار
    private String message;     // نص الإشعار
}
