package com.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorityTestController {

    @GetMapping("/facebook")
    public String facebook() {
        return "facebook 인증 성공";
    }

    @GetMapping("/google")
    public String google() {
        return "google 인증 성공";
    }

    @GetMapping("/kakao")
    public String kakao() {
        return "kakao 인증 성공";
    }
}