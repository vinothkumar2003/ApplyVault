package com.example.backend.model;

public enum VaultStatus {
    DRAFT("Draft - Not Submitted"),
    SUBMITTED("Submitted for Review"),
    APPROVED("Approved"),
    REJECTED("Rejected");
    
    private final String displayName;
    
    VaultStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}