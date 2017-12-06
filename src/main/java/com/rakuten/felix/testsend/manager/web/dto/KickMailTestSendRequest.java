package com.rakuten.felix.testsend.manager.web.dto;

import lombok.Value;
import org.json.simple.JSONArray;

@Value
public class KickMailTestSendRequest {
    Integer bundleId;
    Integer bundleType;
    JSONArray mailJob;
}
