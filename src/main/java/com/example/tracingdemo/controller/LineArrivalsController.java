package com.example.tracingdemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services")
public class LineArrivalsController {

    @PostMapping("/V4/LineArrivals")
    public ResponseEntity<String> handleRequest() {
        System.out.println("Controller method invoked.");
        return ResponseEntity.ok("Line arrivals processed successfully.");
    }
}
