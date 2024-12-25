package com.example.tracingdemo.tracing;

import datadog.trace.api.GlobalTracer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class TraceInterceptorRegistrar {

    @PostConstruct
    public void registerInterceptor() {
        GlobalTracer.get().addTraceInterceptor(new CustomTraceInterceptor());
    }
}

