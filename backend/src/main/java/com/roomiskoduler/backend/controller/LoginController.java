package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.service.LoginChecker;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        Map<String, Object> response = new HashMap<>();

        String[] loginResult = LoginChecker.loginWithRole(username, password);

        if (loginResult != null && loginResult.length == 2) {
            String role = loginResult[0];
            int userId;

            try {
                userId = Integer.parseInt(loginResult[1]);
                response.put("role", role);
                response.put("userId", userId);
            } catch (NumberFormatException e) {
                response.put("error", "Failed to parse user ID.");
            }

        } else {
            response.put("error", "Invalid credentials");
        }

        return response;
    }
}
