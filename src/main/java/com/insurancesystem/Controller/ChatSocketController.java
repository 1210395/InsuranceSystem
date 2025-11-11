package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.MessageDTO;
import com.insurancesystem.Services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendPrivateMessage")
    public void handlePrivateMessage(@Payload MessageDTO messageDTO) {
        MessageDTO saved = chatService.sendMessage(
                messageDTO.getSenderId(),
                messageDTO.getReceiverId(),
                messageDTO.getContent()
        );

        // إرسال الرسالة للطرفين
        messagingTemplate.convertAndSendToUser(
                saved.getReceiverId().toString(), "/queue/messages", saved);
        messagingTemplate.convertAndSendToUser(
                saved.getSenderId().toString(), "/queue/messages", saved);
    }
}
