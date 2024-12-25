package com.example.tracingdemo.tracing;

import datadog.trace.api.DDTags;
import datadog.trace.api.interceptor.MutableSpan;
import datadog.trace.api.interceptor.TraceInterceptor;
import ddtrot.dd.trace.core.DDSpan;
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
