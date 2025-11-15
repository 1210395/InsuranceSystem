package com.insurancesystem.Model.Dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class MessageDTO {
    private UUID id;
    private String content;
    private UUID senderId;
    private UUID receiverId;
    private LocalDateTime sentAt;
}
