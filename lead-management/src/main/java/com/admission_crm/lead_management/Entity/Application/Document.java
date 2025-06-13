package com.admission_crm.lead_management.Entity.Application;

import com.admission_crm.lead_management.Entity.CoreEntities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String applicationId;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // TRANSCRIPT, CERTIFICATE, ID_PROOF, etc.

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    private String uploadedBy;

    private String verifiedBy;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @CreationTimestamp
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DocumentStatus {
        UPLOADED, VERIFIED, REJECTED, PENDING_VERIFICATION
    }
}
