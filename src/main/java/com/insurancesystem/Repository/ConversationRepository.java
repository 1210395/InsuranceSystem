package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId ORDER BY c.lastUpdated DESC")
    List<Conversation> findAllByUserId(UUID userId);

    @Query("SELECT c FROM Conversation c WHERE (c.user1.id = :senderId AND c.user2.id = :receiverId) OR (c.user1.id = :receiverId AND c.user2.id = :senderId)")
    Optional<Conversation> findConversationBetween(UUID senderId, UUID receiverId);
}
