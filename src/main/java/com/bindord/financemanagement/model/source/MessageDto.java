package com.bindord.financemanagement.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class MessageDto {

    @JsonProperty("@odata.etag")
    private String odataEtag;
    private String id;
    private String createdDateTime;
    private String lastModifiedDateTime;
    private String changeKey;
    private List<String> categories;
    private String receivedDateTime;
    private String sentDateTime;
    private Boolean hasAttachments;
    private String internetMessageId;
    private String subject;
    private String bodyPreview;
    private String importance;
    private String parentFolderId;
    private String conversationId;
    private String conversationIndex;
    private Boolean isDeliveryReceiptRequested;
    private Boolean isReadReceiptRequested;
    private Boolean isRead;
    private Boolean isDraft;
    private String webLink;
    private String inferenceClassification;
    private Body body;
    private EmailAddressWrapper sender;
    private EmailAddressWrapper from;
    private List<Recipient> toRecipients;
    private List<Recipient> ccRecipients;
    private List<Recipient> bccRecipients;
    private List<Recipient> replyTo;
    private Flag flag;

    @Setter
    @Getter
    @ToString
    public static class Body {
        private String contentType;
        private String content;
    }

    @Setter
    @Getter
    @ToString
    public static class EmailAddressWrapper {
        //In revision...
        private String name;
        private String address;
        //End
        private EmailAddress emailAddress;

        @Setter
        @Getter
        @ToString
        public static class EmailAddress {
            private String name;
            private String address;

        }
    }

    @Setter
    @Getter
    @ToString
    public static class Recipient {
        private EmailAddressWrapper emailAddress;
    }

    @Setter
    @Getter
    @ToString
    public static class Flag {
        private String flagStatus;

    }
}
