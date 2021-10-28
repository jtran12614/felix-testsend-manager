package com.rakuten.felix.testsend.manager.webclients.dto;

import com.rakuten.felix.testsend.manager.serde.UnixTimeStampDeserializer;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Value
public class RegisterCampaignRequest {
    @JsonDeserialize(contentUsing = UnixTimeStampDeserializer.class)
    @JsonSerialize(contentUsing = UnixTimeStampSerializer.class)
    List<ZonedDateTime> scheduleDates;

    Map<String, Object> mailJob;
}
