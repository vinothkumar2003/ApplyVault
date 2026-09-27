package com.example.backend.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vaults")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;  
    @Column(nullable = false)
    private String companyName;
    @Column(nullable = false)
    private String vaultTitle;
    private String vaultUrl;
    private String description;

    @Enumerated(EnumType.STRING)  // Store as string in DB
    @Column(nullable = false)
    @Builder.Default
    private VaultStatus status = VaultStatus.DRAFT;
    @Column(nullable = false)
    @Builder.Default
    private boolean isEnabled=true;
     @Column
    private LocalDateTime dateApply;
   @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
     @Transient
    public String getStatusDisplay() {
        return status.getDisplayName();
    }
    
    @Transient
    public Boolean isApproved() {
        return status == VaultStatus.APPROVED;
    }
    
    @Transient
    public Boolean isRejected() {
        return status == VaultStatus.REJECTED;
    }
    
    @Transient
    public Boolean isSubmitted() {
        return status == VaultStatus.SUBMITTED;
    }

}
