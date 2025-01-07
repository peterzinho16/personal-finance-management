package com.bindord.financemanagement.advice;


import com.bindord.financemanagement.utils.Constants;

public class NotFoundValidationException extends Exception {

    private String internalCode;

    public NotFoundValidationException(String internalCode) {
        super(Constants.RESOURCE_NOT_FOUND);
        this.internalCode = internalCode;
    }

    public String getInternalCode() {
        return internalCode;
    }
}
