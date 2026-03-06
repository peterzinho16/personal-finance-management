package com.bindord.financemanagement.model.record;

public record ProviderMailMessage(
    String id,
    String subject,
    String bodyHtml,
    String bodyPreview,
    String createdDateTime,
    String from,
    String webLink
) {}