package com.together.server.domain.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String issue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String impact;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String solution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_tags", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "tags")
    private List<String> tags = new ArrayList<>();


    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Notification(String title, String summary, String issue, String impact, String solution, List<String> tags) {
        this.title = title;
        this.summary = summary;
        this.issue = issue;
        this.impact = impact;
        this.solution = solution;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
}
