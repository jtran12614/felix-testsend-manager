package com.rakuten.felix.testsend.manager.web.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

@Value
@Builder
public class RecipientData {
    @Positive
    Integer recipientGroupId;

    @Size(min = 1, max = 100)
    List<@NotNull String> inlineAddresses;

    @NotBlank
    String recipientFilePath;

    @NotNull
    @Valid
    List<RecipientAttribute> recipientAttributes;
}
