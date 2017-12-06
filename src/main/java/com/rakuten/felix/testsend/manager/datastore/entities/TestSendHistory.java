package com.rakuten.felix.testsend.manager.datastore.entities;

import com.rakuten.felix.testsend.manager.datastore.converters.TestSendStatusConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_send_histories")
public class TestSendHistory {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer jobId;
    @Column(nullable = false)
    private Integer bundleType;
    @Column(nullable = false)
    private Integer bundleId;
    @Convert(converter = TestSendStatusConverter.class)
    private TestSendStatus status;
    @Column(nullable = false)
    private String info;
}
