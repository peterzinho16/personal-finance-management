package com.bindord.financemanagement.advice;

public class CustomForbiddenException extends Exception {

    private String internalCode;

    public CustomForbiddenException(String message) {
        super(message);
    }

    public CustomForbiddenException(String message, String internalCode) {
        super(message);
        this.internalCode = internalCode;
    }

    public String getInternalCode() {
        return internalCode;
    }
}
