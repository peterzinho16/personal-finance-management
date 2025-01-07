package com.bindord.financemanagement.model.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    private String message;
    private String code;

    public ErrorResponse(){}

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }

}
