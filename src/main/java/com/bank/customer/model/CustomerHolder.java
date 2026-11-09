package com.bank.customer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "customer_holders")
public class CustomerHolder {
    @Id
    private String id;
    private String accountId;
    private String customerId; 
    private List<HolderDetail> holders;
    private List<SignerDetail> authorizedSigners;
    private LocalDateTime assignedAt;

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public List<HolderDetail> getHolders() { return holders; }
    public void setHolders(List<HolderDetail> holders) { this.holders = holders; }
    
    public List<SignerDetail> getAuthorizedSigners() { return authorizedSigners; }
    public void setAuthorizedSigners(List<SignerDetail> authorizedSigners) { this.authorizedSigners = authorizedSigners; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
