package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity representing uploaded documents, media, or files.
 */
@Entity
@Table(name = "attachments")
@SQLDelete(sql = "UPDATE attachments SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"uploader", "decision", "comment", "community"})
public class Attachment extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotBlank
    @Size(max = 512)
    @Column(name = "url", nullable = false, length = 512)
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 100)
    private FileType fileType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id")
    private DecisionBoard decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    /**
     * Enforces the check constraint that exactly one target container is mapped.
     *
     * @return true if valid, false otherwise.
     */
    @AssertTrue(message = "Attachment must belong to exactly one container (decision, comment, or community)")
    public boolean isValidAttachmentTarget() {
        int count = 0;
        if (this.decision != null) count++;
        if (this.comment != null) count++;
        if (this.community != null) count++;
        return count == 1;
    }
}
