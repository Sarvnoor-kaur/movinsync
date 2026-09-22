package com.fleetbilling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestRoleController {

    @GetMapping("/admin/test")
    public ResponseEntity<Map<String, String>> adminTest() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Admin endpoint accessed successfully!",
                "role", "ADMIN"
        ));
    }

    @GetMapping("/hr/test")
    public ResponseEntity<Map<String, String>> hrTest() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "HR endpoint accessed successfully!",
                "role", "HR"
        ));
    }

    @GetMapping("/employee/test")
    public ResponseEntity<Map<String, String>> employeeTest() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Employee endpoint accessed successfully!",
                "role", "EMPLOYEE"
        ));
    }
}
