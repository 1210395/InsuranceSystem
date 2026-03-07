package com.insurancesystem.Controller;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Exception.UnauthorizedException;
import com.insurancesystem.Model.Dto.ConversationDTO;
import com.insurancesystem.Model.Dto.MessageDTO;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Services.ChatService;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Model.Entity.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;
    private final ClientRepository clientRepo;

    // ✅ إرسال رسالة جديدة (HTTP) - يستخدمها React لأول رسالة
    @PostMapping("/send")
    public MessageDTO sendMessage(
            @RequestParam UUID senderId,
            @RequestParam UUID receiverId,
            @RequestParam String content,
            Authentication auth) {
        // Fix #41: Validate senderId matches authenticated user
        Client authenticatedUser = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        if (!authenticatedUser.getId().equals(senderId)) {
            throw new UnauthorizedException("You cannot send messages on behalf of another user");
        }
        return chatService.sendMessage(senderId, receiverId, content);
    }

    // ✅ جلب كل المحادثات للمستخدم الحالي
    @GetMapping("/conversations/{userId}")
    public List<ConversationDTO> getUserConversations(@PathVariable UUID userId, Authentication auth) {
        // Fix #41: Validate userId matches authenticated user
        Client authenticatedUser = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        if (!authenticatedUser.getId().equals(userId)) {
            throw new UnauthorizedException("You cannot view another user's conversations");
        }
        return chatService.getUserConversations(userId);
    }

    // ✅ جلب جميع الرسائل لمحادثة معينة
    @GetMapping("/messages/{conversationId}")
    public List<MessageDTO> getMessages(@PathVariable UUID conversationId) {
        return chatService.getMessages(conversationId);
    }

    // ✅ جلب قائمة المستخدمين الآخرين لبدء دردشة جديدة
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsersExceptCurrent(@RequestParam UUID currentUserId, Authentication auth) {
        // Fix #41: Validate currentUserId matches authenticated user
        Client authenticatedUser = clientRepo.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        if (!authenticatedUser.getId().equals(currentUserId)) {
            throw new UnauthorizedException("You cannot impersonate another user");
        }
        // Use targeted role queries instead of loading all clients
        List<RoleName> chatRoles = List.of(
                RoleName.DOCTOR, RoleName.PHARMACIST, RoleName.LAB_TECH,
                RoleName.RADIOLOGIST, RoleName.MEDICAL_ADMIN,
                RoleName.COORDINATION_ADMIN, RoleName.INSURANCE_MANAGER
        );
        // Fix #44: Return lightweight map instead of raw Client entities (which expose passwordHash, nationalId, etc.)
        return clientRepo.findAllHealthcareProviders(chatRoles)
                .stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> {
                    Map<String, Object> dto = new java.util.LinkedHashMap<>();
                    dto.put("id", u.getId());
                    dto.put("fullName", u.getFullName());
                    dto.put("email", u.getEmail());
                    dto.put("phone", u.getPhone());
                    dto.put("department", u.getDepartment());
                    dto.put("specialization", u.getSpecialization());
                    dto.put("status", u.getStatus());
                    return dto;
                })
                .toList();
    }

}
