package com.bank.customer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    private String type; // PERSONAL o ENTERPRISE
    private String status; // ACTIVE, INACTIVE
    private PersonalInfo personalInfo;
    private EnterpriseInfo enterpriseInfo;
    private List<Signer> signers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters y Setters explícitos de Java 27
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }
    
    public EnterpriseInfo getEnterpriseInfo() { return enterpriseInfo; }
    public void setEnterpriseInfo(EnterpriseInfo enterpriseInfo) { this.enterpriseInfo = enterpriseInfo; }
    
    public List<Signer> getSigners() { return signers; }
    public void setSigners(List<Signer> signers) { this.signers = signers; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
