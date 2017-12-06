package com.rakuten.felix.testsend.manager.web.dto;

import lombok.Value;
import org.json.simple.JSONObject;

@Value
public class KickMailTestSendRequest {
    Integer bundleId;
    Integer bundleType;
    JSONObject mailJob;
}
