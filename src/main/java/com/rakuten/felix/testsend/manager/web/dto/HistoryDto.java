package com.rakuten.felix.testsend.manager.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampDeserializer;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampSerializer;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class HistoryDto {
    Integer id;
    Integer jobId;
    Integer bundleType;
    Integer bundleId;
    TestSendStatus status;
    @JsonDeserialize(using = UnixTimeStampDeserializer.class)
    @JsonSerialize(using = UnixTimeStampSerializer.class)
    ZonedDateTime started;
    @JsonDeserialize(using = UnixTimeStampDeserializer.class)
    @JsonSerialize(using = UnixTimeStampSerializer.class)
    ZonedDateTime finished;

    public static HistoryDto buildFromEntity(TestSendHistory entity){
        return HistoryDto.builder()
                         .id(entity.getId())
                         .jobId(entity.getJobId())
                         .bundleType(entity.getBundleType())
                         .bundleId(entity.getBundleId())
                         .status(entity.getStatus())
                         .started(entity.getStarted())
                         .finished(entity.getFinished())
                         .build();
    }
}
