package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.MessageDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClientRepository clientRepo;

    @MessageMapping("/chat.sendPrivateMessage")
    public void handlePrivateMessage(@Payload MessageDTO messageDTO, Principal principal) {
        // Validate that the authenticated user matches the senderId
        if (principal == null) {
            log.warn("Unauthenticated WebSocket message attempt");
            return;
        }

        Client sender = clientRepo.findByEmail(principal.getName().toLowerCase()).orElse(null);
        if (sender == null || !sender.getId().equals(messageDTO.getSenderId())) {
            log.warn("WebSocket sender mismatch: authenticated={}, claimed={}",
                    principal.getName(), messageDTO.getSenderId());
            return;
        }

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
