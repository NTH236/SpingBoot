package com.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // <-- Thêm
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // <-- Thêm

import com.api.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestParam String userName, @RequestParam String password) {
        String dummyUser = "hoang";
        String dummyPass = "123456";
        if (userName.equals(dummyUser) && password.equals(dummyPass)) {
            String token = jwtUtil.generateToken(userName);
            return token;
        }
        return "Truy cập bị từ chối.";
    }
}