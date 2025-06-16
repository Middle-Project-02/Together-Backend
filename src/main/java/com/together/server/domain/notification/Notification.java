package com.together.server.domain.notification;

import com.together.server.infra.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "issue", nullable = false, columnDefinition = "TEXT")
    private String issue;

    @Column(name = "solution", nullable = false, columnDefinition = "TEXT")
    private String solution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_tags", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "tags")
    private List<String> tags = new ArrayList<>();

    public Notification(String title, String issue, String solution, List<String> tags) {
        this.title = title;
        this.issue = issue;
        this.solution = solution;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
}
