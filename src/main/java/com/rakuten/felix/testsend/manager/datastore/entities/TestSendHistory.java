package com.rakuten.felix.testsend.manager.datastore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rakuten.felix.testsend.manager.datastore.converters.InfoConverter;
import com.rakuten.felix.testsend.manager.datastore.converters.TestSendStatusConverter;
import com.rakuten.felix.testsend.manager.datastore.converters.ZonedDateTimeConverter;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampDeserializer;
import com.rakuten.felix.testsend.manager.serde.UnixTimeStampSerializer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Tolerate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Builder
@ToString
@Table(name = "test_send_histories")
public class TestSendHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Integer id;
    private Integer jobId;
    @Column(nullable = false)
    private Integer bundleType;
    @Column(nullable = false)
    private Integer bundleId;
    @Convert(converter = TestSendStatusConverter.class)
    private TestSendStatus status;
    @Column(nullable = false)
    @Convert(converter = InfoConverter.class)
    private Info info;
    @Column(nullable = false)
    @Convert(converter = ZonedDateTimeConverter.class)
    @JsonDeserialize(using = UnixTimeStampDeserializer.class)
    @JsonSerialize(using = UnixTimeStampSerializer.class)
    private ZonedDateTime started;
    @Convert(converter = ZonedDateTimeConverter.class)
    @JsonDeserialize(using = UnixTimeStampDeserializer.class)
    @JsonSerialize(using = UnixTimeStampSerializer.class)
    private ZonedDateTime finished;

    @Version
    @JsonIgnore
    private long version;

    @Tolerate
    public TestSendHistory() {
        // for default constructor
    }
}
