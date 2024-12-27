package com.bindord.financemanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MailMessagesResponse {

    @JsonProperty("@odata.context")
    private String odataContext;
    private List<MessageDto> value;
    @JsonProperty("@odata.nextLink")
    private String odataNextLink;
}
