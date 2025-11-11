package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private Client user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private Client user2;

    @Column(name = "conversation_type", length = 50)
    private String conversationType; // e.g. "Insurance-Doctor"

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        this.lastUpdated = Instant.now();
    }
}
