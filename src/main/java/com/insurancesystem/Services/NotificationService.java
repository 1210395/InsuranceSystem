package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Exception.UnauthorizedException;
import com.insurancesystem.Model.Dto.NotificationDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Notification;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.NotificationType;
import com.insurancesystem.Model.MapStruct.NotificationMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final ClientRepository clientRepo;
    private final NotificationMapper notificationMapper;

    /**
     * Get system sender for system notifications.
     * Uses the first INSURANCE_MANAGER as the system sender.
     * Returns null if no INSURANCE_MANAGER exists (e.g., fresh database).
     */
    private Client getSystemSender() {
        List<Client> managers = clientRepo.findByRoleOrRequestedRole(RoleName.INSURANCE_MANAGER);
        return managers.isEmpty() ? null : managers.get(0);
    }

    // ➕ إرسال إشعار يدوي (استفسار أو رد)
    @Transactional
    public void createNotification(UUID senderId, UUID recipientId, String message, UUID repliedNotificationId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));
        Client sender = clientRepo.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender not found"));

        if (repliedNotificationId != null) {
            Notification original = notificationRepo.findById(repliedNotificationId)
                    .orElseThrow(() -> new NotFoundException("Original notification not found"));
            original.setRead(true);
            original.setReplied(true);
            notificationRepo.save(original);
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .message(sender.getFullName() + ": " + message)
                .read(false)
                .type(NotificationType.MANUAL_MESSAGE)
                .build();

        notificationRepo.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient);
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Transactional
    public void sendToUser(UUID recipientId, String message) {
        sendToUser(recipientId, message, null);
    }

    @Transactional
    public void sendToUser(UUID recipientId, String arabicMessage, String englishMessage) {
        Client systemSender = getSystemSender();
        if (systemSender == null) {
            log.warn("No Insurance Manager found - skipping system notification to user {}: {}", recipientId, arabicMessage);
            return;
        }

        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(systemSender)
                .message(arabicMessage)
                .englishMessage(englishMessage)
                .read(false)
                .type(NotificationType.SYSTEM)
                .build();

        notificationRepo.save(notification);
    }

    @Transactional
    public void sendToRole(RoleName roleName, String message) {
        sendToRole(roleName, message, null);
    }

    @Transactional
    public void sendToRole(RoleName roleName, String arabicMessage, String englishMessage) {
        Client systemSender = getSystemSender();
        if (systemSender == null) {
            log.warn("No Insurance Manager found - skipping system notification to role {}: {}", roleName, arabicMessage);
            return;
        }

        List<Client> clients = clientRepo.findByRoleOrRequestedRole(roleName);

        if (clients.isEmpty()) {
            log.warn("No users with role {} found - skipping notification", roleName);
            return;
        }

        List<Notification> notifications = clients.stream()
                .map(client -> Notification.builder()
                        .recipient(client)
                        .sender(systemSender)
                        .message(arabicMessage)
                        .englishMessage(englishMessage)
                        .read(false)
                        .type(NotificationType.SYSTEM)
                        .build())
                .toList();

        notificationRepo.saveAll(notifications);
    }

    @Transactional
    public void markAllAsRead(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(recipient);
        notifications.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(notifications);
    }

    @Transactional
    public void markNotificationAsReadByMessage(RoleName roleName, String message) {
        List<Client> clients = clientRepo.findByRoleOrRequestedRole(roleName);

        for (Client client : clients) {
            List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(client);
            notifications.stream()
                    .filter(n -> n.getMessage().equals(message))
                    .forEach(n -> n.setRead(true));
            notificationRepo.saveAll(notifications);
        }
    }

    @Transactional
    public void markAsRead(UUID notificationId, Client currentUser) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + notificationId));

        if (notification.getRecipient() == null ||
                !notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("❌ This notification is not yours");
        }

        notification.setRead(true);
        notificationRepo.save(notification);
    }



    @Transactional(readOnly = true)
    public long countUnreadNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return notificationRepo.countByRecipientAndReadFalse(recipient);
    }

    @Transactional(readOnly = true)
    public long countUnreadEmergencyNotifications(UUID recipientId) {
        Client recipient = clientRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return notificationRepo.countByRecipientAndTypeAndReadFalse(recipient, NotificationType.EMERGENCY);
    }

    @Transactional(readOnly = true)
    public UUID getNotificationSenderId(UUID notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + notificationId));
        return notification.getSender().getId();
    }

    @Transactional
    public void deleteNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this notification");
        }

        notificationRepo.delete(notification);
    }

    @Transactional
    public void createNotificationByEmail(
            String senderEmail,
            String recipientEmail,
            String message,
            NotificationType type,
            UUID repliedNotificationId
    ) {
        Client sender = clientRepo.findByEmail(senderEmail.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Sender not found"));

        Client recipient = clientRepo.findByEmail(recipientEmail.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        if (repliedNotificationId != null) {
            Notification original = notificationRepo.findById(repliedNotificationId)
                    .orElseThrow(() -> new NotFoundException("Original notification not found"));
            original.setRead(true);
            notificationRepo.save(original);
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .message(sender.getFullName() + ": " + message)
                .read(false)
                .type(type != null ? type : NotificationType.MANUAL_MESSAGE)
                .build();

        notificationRepo.save(notification);
    }

    @Transactional
    public void clientDeleteNotification(UUID clientId, UUID notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(clientId)) {
            throw new UnauthorizedException("Unauthorized: This notification is not yours");
        }

        notificationRepo.delete(notification);
    }

    @Transactional
    public void createNotificationByFullName(String senderFullName, String recipientFullName,
                                             String message, UUID parentId) {
        Client sender = clientRepo.findByFullName(senderFullName)
                .orElseThrow(() -> new NotFoundException("Sender not found with name: " + senderFullName));

        Client recipient = clientRepo.findByFullName(recipientFullName)
                .orElseThrow(() -> new NotFoundException("Recipient not found with name: " + recipientFullName));

        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .message(message)
                .type(NotificationType.MANUAL_MESSAGE)
                .read(false)
                .replied(false)
                .createdAt(Instant.now())
                .build();

        notificationRepo.save(notification);
    }
}
