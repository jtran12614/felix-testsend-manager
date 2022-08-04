package com.rakuten.felix.testsend.manager.web.dto;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Value
public class RecipientAttribute {

    @PositiveOrZero
    Integer order;

    @NotBlank
    String replaceValue;
}
