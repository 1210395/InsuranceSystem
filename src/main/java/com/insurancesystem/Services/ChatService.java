package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.Conversation;
import com.insurancesystem.Model.Entity.Message;
import com.insurancesystem.Repository.ConversationRepository;
import com.insurancesystem.Repository.MessageRepository;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Model.Dto.ConversationDTO;
import com.insurancesystem.Model.Dto.MessageDTO;
import com.insurancesystem.Model.MapStruct.ChatMapper;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private ChatMapper chatMapper;

    // ✅ إرسال رسالة جديدة
    @Transactional
    public MessageDTO sendMessage(UUID senderId, UUID receiverId, String content) {
        var sender = clientRepo.findById(senderId)
                .orElseThrow(() -> new ApiException("Sender not found"));
        var receiver = clientRepo.findById(receiverId)
                .orElseThrow(() -> new ApiException("Receiver not found"));

        // 🚫 منع العميل من المشاركة في المحادثة
        if (sender.hasRole(RoleName.INSURANCE_CLIENT) || receiver.hasRole(RoleName.INSURANCE_CLIENT)) {
            throw new ApiException("Clients are not allowed to use the chat system");
        }

        // ✅ إيجاد أو إنشاء محادثة
        var conversation = conversationRepo.findConversationBetween(senderId, receiverId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser1(sender);
                    c.setUser2(receiver);
                    c.setConversationType(detectConversationType(sender, receiver));
                    return conversationRepo.save(c);
                });

        // ✅ إنشاء الرسالة
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setSentAt(Instant.now());

        messageRepo.save(message);
        conversation.setLastUpdated(Instant.now());
        conversationRepo.save(conversation);

        return chatMapper.toMessageDTO(message);
    }

    private String detectConversationType(Object s, Object r) {
        return "Insurance-Medical";
    }

    // ✅ جلب محادثات المستخدم (بين التأمين والجهات الطبية فقط)
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(UUID userId) {
        return conversationRepo.findAllByUserId(userId)
                .stream()
                .filter(c ->
                        (c.getUser1().hasRole(RoleName.INSURANCE_MANAGER) ||
                                c.getUser2().hasRole(RoleName.INSURANCE_MANAGER))
                )
                .map(chatMapper::toConversationDTO)
                .collect(Collectors.toList());
    }

    // ✅ جلب الرسائل لمحادثة معينة
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(UUID conversationId) {
        return messageRepo.findAllByConversationId(conversationId)
                .stream()
                .map(chatMapper::toMessageDTO)
                .collect(Collectors.toList());
    }
}
