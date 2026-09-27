package com.example.backend.service;

import com.example.backend.model.Vault;
import com.example.backend.model.VaultStatus;
import com.example.backend.repository.UserRespository;
import com.example.backend.repository.VaultRespository;
import com.example.backend.model.User;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VaultService {

    @Autowired
    private  VaultRespository vaultRepository;
    
    @Autowired
    private UserRespository userRepository;

    @Autowired
    private Environment env;
      public boolean isAdmin(String token){
            if(token==null || token.isEmpty()) return false;
            String AdminToken =env.getProperty("app.admin.token");
            return token.equals(AdminToken);
    }
    
    public ResponseEntity<?> getAllVaults(){
        try {
            List<Vault> vaults = vaultRepository.findAll();
            return ResponseEntity.ok(vaults);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred while fetching vaults"));
        }
    }
    // CREATE
    public ResponseEntity<?> createVault(Long userId, Vault vault) {
        try{
            if(!userRepository.existsById(userId)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            if(!userRepository.findById(userId).get().isEnabled()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user is disabled"); 
            vault.setUserId(userId);
            if(vault.getStatus() == null) vault.setStatus(VaultStatus.DRAFT);
            VaultStatus vaultStatus =VaultStatus.valueOf(vault.getStatus().toString().toUpperCase());
            vault.setStatus(vaultStatus);
            vault.setCreatedAt(LocalDateTime.now());
            
            Vault created = vaultRepository.save(vault);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Vault created successfully","vault",created));
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","An error occurred while creating the vault"));
        }
      
    }

    // READ
    public ResponseEntity<?> getUserVaults(Long userId) {
        try{
            if(userId ==null)  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","User Id is required"));
            List<Vault> vaults = vaultRepository.findByUserId(userId); 
            return ResponseEntity.status(HttpStatus.OK).body(vaults);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the vault");
        }
        
    }

    public ResponseEntity<?> getVaultsByStatus(String status) {
        try{
            
            List<Vault> vaults = vaultRepository.findByStatus(status);
            return ResponseEntity.status(HttpStatus.OK).body(vaults);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the vault");
        }
    }
    
    public ResponseEntity<?> getUserVaultsByStatus(Long userId, String status) {
        try{
            if(userId == null && status == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            List<Vault> vaults = vaultRepository.findByUserIdAndStatus(userId, status);
            return ResponseEntity.status(HttpStatus.OK).body(vaults);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the vault");
        }
    }

    public ResponseEntity<?> getVaultById(Long id) {
        try{
            if(id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vault Id is required");
            Optional<Vault> vault = vaultRepository.findById(id);
            if(vault.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(vault.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the vault");
        }
    }

    // UPDATE
    public ResponseEntity<?> updateVault(Long id, Long userId, Vault vaultDetails) {
        try {
            Optional<Vault> vault =  vaultRepository.findById(id);
            if(vault.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
            if (!vault.get().getUserId().equals(userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to update this vault");
            if (vaultDetails.getCompanyName() != null)   vault.get().setCompanyName(vaultDetails.getCompanyName());
            if (vaultDetails.getVaultTitle() != null)   vault.get().setVaultTitle(vaultDetails.getVaultTitle());
            if (vaultDetails.getVaultUrl() != null)  vault.get().setVaultUrl(vaultDetails.getVaultUrl());
            if (vaultDetails.getDescription() != null)  vault.get().setDescription(vaultDetails.getDescription());
            vault.get().setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.OK).body(vaultRepository.save(vault.get()));
        } catch(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the vault");
        }
    }

    // UPDATE STATUS
    public ResponseEntity<?> updateStatus(Long id, String status) {
        try {
            Optional<Vault> vaultOpt = vaultRepository.findById(id);
            if(vaultOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vault not found");
            Vault vault = vaultOpt.get();   
            VaultStatus vaultStatus =VaultStatus.valueOf(status.toUpperCase());
            vault.setStatus(vaultStatus);
            vault.setUpdatedAt(LocalDateTime.now());
            vaultRepository.save(vault);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","vault is updated successfully"));
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the vault status");
        }
    }

    // DELETE
    public ResponseEntity<?> deleteVault(Long id,String token) {
        try {
            if(!vaultRepository.existsById(id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error","Vault not found"));
            if(token==null){
                Optional<Vault> vaultotp =vaultRepository.findById(id);
                Vault vault = vaultotp.get();
                vault.isEnabled();
                vaultRepository.save(vault);
            }
            if(isAdmin(token)) vaultRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Vault deleted successfully"));
        } catch(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",e.getMessage()));
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","An error occurred while deleting the vault"));
        }
    }
}