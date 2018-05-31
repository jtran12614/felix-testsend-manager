package com.rakuten.felix.testsend.manager.webclients.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampDeserializer;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampSerializer;
import lombok.Value;
import org.json.simple.JSONObject;

import java.time.ZonedDateTime;
import java.util.List;

@Value
public class RegisterCampaignRequest {
    @JsonDeserialize(contentUsing = UnixTimeStampDeserializer.class)
    @JsonSerialize(contentUsing = UnixTimeStampSerializer.class)
    List<ZonedDateTime> scheduleDates;

    JSONObject mailJob;
}
