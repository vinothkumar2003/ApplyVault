
package com.example.backend.service;

import com.example.backend.model.LoginUser;
import com.example.backend.model.User;
import com.example.backend.repository.UserRespository;
import org.springframework.core.env.Environment;
// import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRespository userRepository;
    @Autowired
    private Environment env;

    public ResponseEntity<?> LoginUser(LoginUser user){
        try{
            // System.out.println(user);
            if(user.getUsername()==null && user.getEmail()==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email or username is required");
            if(user.getPassword()==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("password is required");
            if(user.getUsername()==null && user.getPassword()==null && user.getEmail()==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email or username and password are required");
            Optional<User> existingUser = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail());
            if (existingUser.isPresent()) {
                User usr=existingUser.get(); 
                if(user.getPassword().equals(usr.getPassword())) return ResponseEntity.ok(Map.of("user",usr,"jwt",env.getProperty("app.admin.token")));
                else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid password");
            }
            else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username or password");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public String GetMessage(User user){
        String msg ="";
        if(userRepository.existsByUsername(user.getUsername()) && userRepository.existsByEmail(user.getEmail())) {
            msg = "Username and email already exist.";
            return msg;
        }
        if(userRepository.existsByUsername(user.getUsername())) {
            msg = "Username already exists. ";
        }
        if(userRepository.existsByEmail(user.getEmail())) {
            msg = "Email already exists. ";
        }
        return msg;
    }
    public boolean isAdmin(String token){
            if(token==null || token.isEmpty()) return false;
            String AdminToken =env.getProperty("app.admin.token");
            return token.equals(AdminToken);
    }
    
    public ResponseEntity<String> createUser(User user) {
        try{
        if(user.getEmail()==null || user.getUsername()==null || user.getPassword()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username, email and password are required");
        }
        if(userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GetMessage(user));
        }
        user.setCreatedAt(LocalDateTime.now());
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        if (user.getRole() == null) {
            user.setRole("user");
        }
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<?> getUserById(Long id) {
        try {
            if(id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User id is required");
            if(userRepository.existsById(id)){
            Optional<User> user = userRepository.findById(id);
            return ResponseEntity.ok(user.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<String> updateUser(Long id, User userDetails) {
      try{
        if(id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User id is required");
        if(userDetails==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User details are required");
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPassword(userDetails.getPassword());
            user.setEnabled(userDetails.getEnabled());
            user.setRole(userDetails.getRole());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    } catch(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    }

    public ResponseEntity<String> deleteUser(Long id,String token) {
        try {
            if(id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User id is required");
            if(!userRepository.existsById(id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            if(token==null){
               Optional<User> userotp =userRepository.findById(id);
               User user =userotp.get();
               user.setEnabled(false);
                userRepository.save(user);
            }
            if(isAdmin(token)) userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}