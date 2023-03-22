package com.company.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "notification")
public class Notification implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDateTime sendDate;

    @Setter
    private boolean seen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    public Notification() {}

    public Notification(String title,
                        String description,
                        User sender,
                        User receiver)
    {
        this.title = title;
        this.description = description;
        this.sendDate = LocalDateTime.now();
        this.seen = false;
        this.sender = sender;
        this.receiver = receiver;
    }
}
