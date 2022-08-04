package com.rakuten.felix.testsend.manager.web.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Value
@Builder
public class RecipientData {

    @NotNull
    @Positive
    Integer recipientGroupId;

    @NotBlank
    String recipientFilePath;

    @NotNull
    @Valid
    List<RecipientAttribute> recipientAttributes;
}
