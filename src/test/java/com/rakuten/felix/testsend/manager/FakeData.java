package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJobWithContents;
import com.rakuten.felix.testsend.manager.webclients.dto.Schedule;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class FakeData {
    TestSendHistory getHistory() {
        return TestSendHistory.builder()
                .id(1)
                .status(TestSendStatus.NEW)
                .bundleId(1)
                .bundleType(1)
                .info("{json}")
                .build();
    }

    List<TestSendHistory> getHistories() {
        return Arrays.asList(
                TestSendHistory.builder()
                        .id(1)
                        .status(TestSendStatus.NEW)
                        .bundleId(11)
                        .bundleType(111)
                        .info("{json}")
                        .build(),
                TestSendHistory.builder()
                        .id(2)
                        .status(TestSendStatus.FINISHED)
                        .bundleId(22)
                        .bundleType(222)
                        .info("{json}")
                        .build()
        );
    }

    MailJobWithContents getEmptyMailJob() {
        return new MailJobWithContents(
                Collections.singletonList(new Schedule(Collections.emptyList(), Collections.emptyList())),
                Collections.singletonList(""),
                new User(0, ""));
    }
}
