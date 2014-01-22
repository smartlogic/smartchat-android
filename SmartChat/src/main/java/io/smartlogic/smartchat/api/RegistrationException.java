package io.smartlogic.smartchat.api;

import java.util.List;
import java.util.Map;

public class RegistrationException extends Exception {
    public Map<String, List<String>> errors;

    public RegistrationException(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}
