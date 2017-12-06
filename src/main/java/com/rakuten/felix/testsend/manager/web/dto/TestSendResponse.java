package com.rakuten.felix.testsend.manager.web.dto;

import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import lombok.Value;

@Value
public class TestSendResponse {
    Integer id;
    Integer bundleId;
    Integer bundleType;

    /**
     * Get instance from entity.
     *
     * @param history Test send history.
     * @return Test send response.
     */
    public static TestSendResponse fromEntity(TestSendHistory history) {
        return new TestSendResponse(
                history.getId(),
                history.getBundleId(),
                history.getBundleType()
        );
    }
}
