package com.odoo.quick_desk_backend.utility;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder().toFactory();
    public String sanitize(String input) {
        if (input == null) return null;
        return POLICY.sanitize(input.trim());
    }
}
