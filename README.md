# Spring Boot Tracing Demo

## Project Overview

This Spring Boot application demonstrates:
1. Pre-routing failure handling using a filter (`PreRoutingFilter`).
2. Custom tracing metadata enhancement using Datadog's `TraceInterceptor`.
3. Registration of the custom interceptor via the `TraceInterceptorRegistrar`.

---

## Project Structure

```plaintext
spring-boot-tracing-demo
├── build.gradle.kts                # Gradle build script
├── settings.gradle.kts             # Gradle settings
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── tracingdemo
│   │   │               ├── TracingDemoApplication.java
│   │   │               ├── controller
│   │   │               │   └── LineArrivalsController.java
│   │   │               ├── filter
│   │   │               │   └── PreRoutingFilter.java
│   │   │               └── tracing
│   │   │                   ├── CustomTraceInterceptor.java
│   │   │                   ├── TraceInterceptorRegistrar.java
│   │   └── resources
│   │       └── application.yml     # Configuration file
└── README.md                       # Project description
```

---

## Project Files

### 1. LineArrivalsController

Handles HTTP POST requests to `/services/V4/LineArrivals`. This controller processes line arrival data or logs invocation during testing.

```java
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
```

---

### 2. PreRoutingFilter

Simulates a pre-routing failure for testing purposes by returning an HTTP 412 response before reaching the routing layer.

```java
package com.example.tracingdemo.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensures the filter runs before Spring’s internal filters
public class PreRoutingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Simulate a pre-routing failure if the query parameter is set
        System.out.println("Pre-routing failure triggered. Returning HTTP 412.");
        String simulatePreRouting = httpRequest.getParameter("simulatePreRouting");
        if ("true".equals(simulatePreRouting)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.PRECONDITION_FAILED.value());
            httpResponse.getWriter().write("Precondition failed before reaching routing layer.");
            return; // Stop further processing
        }

        // Proceed normally if no simulation
        chain.doFilter(request, response);
    }
}
```

---

### 3. CustomTraceInterceptor

Enhances Datadog spans by setting a custom resource name for 4xx responses when `http.route` is unavailable.

```java
package com.example.tracingdemo.tracing;

import datadog.trace.api.DDTags;
import datadog.trace.api.interceptor.MutableSpan;
import datadog.trace.api.interceptor.TraceInterceptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CustomTraceInterceptor implements TraceInterceptor {

    @Override
    public Collection<? extends MutableSpan> onTraceComplete(
            Collection<? extends MutableSpan> trace) {

        List<MutableSpan> modifiedTrace = new ArrayList<>();
        for (MutableSpan span : trace) {
            String method = (String) span.getTag("http.method");
            Integer statusCode = (Integer) span.getTags().get("http.status_code");
            String servicePath = (String) span.getTag("moovit.service_path");

            // Handle 4xx responses
            if (statusCode != null && statusCode >= 400 && statusCode < 500) {
                if (method != null && servicePath != null) {
                    span.setTag(DDTags.RESOURCE_NAME, method + " /" + servicePath);
                }
            }

            // Add the span to the modified trace collection
            modifiedTrace.add(span);
        }

        return modifiedTrace;
    }

    @Override
    public int priority() {
        return 100; // High priority to ensure this runs last
    }
}
```

---

### 4. TraceInterceptorRegistrar

Registers the custom `CustomTraceInterceptor` with Datadog's global tracer.

```java
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
```

---

### 5. TracingDemoApplication

Spring Boot application entry point.

```java
package com.example.tracingdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TracingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracingDemoApplication.class, args);
    }
}
```

---

### 6. `application.yml`

Defines the application’s context path.

```yaml
spring:
  application:
    name: spring-boot-tracing-demo

server:
  port: 8080
  servlet:
    context-path: /services-app
```

---

### 7. `build.gradle.kts`

Configures dependencies and Java settings for the project.

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.datadoghq.pej"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.datadoghq:dd-trace-ot:1.44.1")
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

---

## Manual Testing Using `curl`

### 1. Test Success Scenario (HTTP 200)

```bash
curl -X POST http://localhost:8080/services-app/services/V4/LineArrivals
```

- **Expected Output (Console):**
  ```
  Controller method invoked.
  ```

- **Expected HTTP Response:**
  ```
  Line arrivals processed successfully.
  ```

---

### 2. Test Failure Scenario (HTTP 412)

```bash
curl -X POST http://localhost:8080/services-app/services/V4/LineArrivals?simulatePreRouting=true
```

- **Expected Output (Console):**
  ```
  Pre-routing failure triggered. Returning HTTP 412.
  ```

- **Expected HTTP Response:**
  ```
  Precondition failed before reaching routing layer.
  ```

---

## Goals of the Key Components

### `PreRoutingFilter`
- Simulates a scenario where the routing layer is skipped (e.g., validation or preconditions fail).
- Helps test how Datadog traces handle requests without `http.route`.

### `CustomTraceInterceptor`
- Ensures consistent span metadata in Datadog for HTTP 4xx responses.
- Provides meaningful resource names (`Method + "/" + moovit.service_path`) when `http.route` is unavailable.
