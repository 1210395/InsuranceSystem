package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ConversationDTO;
import com.insurancesystem.Model.Dto.MessageDTO;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Services.ChatService;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Model.Entity.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ClientRepository clientRepo;

    // ✅ إرسال رسالة جديدة (HTTP) - يستخدمها React لأول رسالة
    @PostMapping("/send")
    public MessageDTO sendMessage(
            @RequestParam UUID senderId,
            @RequestParam UUID receiverId,
            @RequestParam String content) {
        return chatService.sendMessage(senderId, receiverId, content);
    }

    // ✅ جلب كل المحادثات للمستخدم الحالي
    @GetMapping("/conversations/{userId}")
    public List<ConversationDTO> getUserConversations(@PathVariable UUID userId) {
        return chatService.getUserConversations(userId);
    }

    // ✅ جلب جميع الرسائل لمحادثة معينة
    @GetMapping("/messages/{conversationId}")
    public List<MessageDTO> getMessages(@PathVariable UUID conversationId) {
        return chatService.getMessages(conversationId);
    }

    // ✅ جلب قائمة المستخدمين الآخرين لبدء دردشة جديدة
    @GetMapping("/users")
    public List<Client> getAllUsersExceptCurrent(@RequestParam UUID currentUserId) {
        return clientRepo.findAll()
                .stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> !u.hasRole(RoleName.INSURANCE_CLIENT)) // 🚫 استبعاد العملاء
                .toList();
    }

}
