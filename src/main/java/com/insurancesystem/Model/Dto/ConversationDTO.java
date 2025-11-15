package com.insurancesystem.Model.Dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ConversationDTO {
    private UUID id;
    private List<MessageDTO> messages;
}
