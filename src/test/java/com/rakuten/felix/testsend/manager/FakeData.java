package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.webclients.dto.Column;
import com.rakuten.felix.testsend.manager.webclients.dto.Columns;
import com.rakuten.felix.testsend.manager.webclients.dto.Content;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJob;
import com.rakuten.felix.testsend.manager.webclients.dto.Pattern;
import com.rakuten.felix.testsend.manager.webclients.dto.PersonalizerColumn;
import com.rakuten.felix.testsend.manager.webclients.dto.Schedule;
import com.rakuten.felix.testsend.manager.webclients.dto.Subject;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@UtilityClass
class FakeData {
    TestSendHistory getHistory() {
        return TestSendHistory.builder()
                              .id(1)
                              .status(TestSendStatus.NEW)
                              .bundleId(1)
                              .bundleType(1)
                              .info(Info.builder()
                                        .subjects(Collections.singletonList("Subject"))
                                        .htmlContents(Collections.singletonList("Html content"))
                                        .textContents(Collections.singletonList("Text content"))
                                        .user(new User(1, "user-name", "user-address@rakuten.com"))
                                        .recipients(Arrays.asList("recipient1", "recipient2"))
                                        .build())
                              .build();
    }

    List<TestSendHistory> getHistories() {
        return Arrays.asList(
                TestSendHistory.builder()
                               .id(1)
                               .status(TestSendStatus.NEW)
                               .bundleId(11)
                               .bundleType(111)
                               .info(Info.builder().build())
                               .build(),
                TestSendHistory.builder()
                               .id(2)
                               .status(TestSendStatus.FINISHED)
                               .bundleId(22)
                               .bundleType(222)
                               .info(Info.builder().build())
                               .build()
        );
    }

    MailJob getValidMailJob() {
        return new MailJob(
                Collections.singletonList(
                        new Schedule(
                                1,
                                Collections.emptyList(),
                                ZonedDateTime.of(2015, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
                                getSubjectsWithSingleCondition(),
                                getHtmlContentsWithSplit1Segment1()
                        )
                ),
                getParts(),
                Arrays.asList("test-address1@rakuten.com", "test-address2@rakuten.com", "test-address1@rakuten.com", "test-address2@rakuten.com"),
                Columns.builder().build(),
                null,
                null
        );
    }


    // Mail Content Builder Pattern:: SimpleSplit * Segment Split

    List<String> getParts() {
        return Arrays.asList("Part0", "Part1", "Part2", "Part3", "Part4", "Part5", "Part6", "Part7");
    }

    List<String> getPartsWithMuAttributes() {
        return Arrays.asList(
                "Part0:###_ATTRIBUTE0_###",                      // No Match Replacement
                "Part1:###_ATTRIBUTE2_###",                      // Email Column Attributes
                "Part2:###_ATTRIBUTE3_######_ATTRIBUTE1_###",    // Name Column Attributes
                "Part3:###_ATTRIBUTE4_###,'https://emagazine.rakuten.co.jp/nq?k=###_ATTRIBUTE6_###','https://emagazine.rakuten.co.jp/q?u=rakuetn24&k=###_ATTRIBUTE6_###&scid=rm_1234567'",   // EasyId/Unsubscribe Column Attributes
                "Part4:###_ATTRIBUTE8_###,###_ATTRIBUTE7_###",   // UserId/Identifier Column Attributes
                "Part5:###_ATTRIBUTE11_###,###_ATTRIBUTE12_###", // Additional11/12 Column Attributes
                "Part6:###_ATTRIBUTE21_###,###_ATTRIBUTE22_###", // Personalizer21/22 Column Attributes
                "Part7:###_ATTRI###_ATTRIBUTE2_###BUTE22_###"    // Email Column Attributes
        );
    }

    Columns getColumns() {
        return Columns.builder()
                      .email(Column.builder().attributeIndex(2).keyword("email").build())
                      .name(Column.builder().attributeIndex(3).keyword("name").build())
                      .easyId(Column.builder().attributeIndex(4).keyword("easyId").build())
                      .userId(Column.builder().attributeIndex(8).keyword("userId").build())
                      .additional(
                              Arrays.asList(
                                      Column.builder().attributeIndex(11).keyword("addA").build(),
                                      Column.builder().attributeIndex(12).keyword("addB").build()
                              )
                      )
                      .personalizer(
                              Arrays.asList(
                                      PersonalizerColumn.builder().attributeIndex(21).keyword("cms:test-attrA").build(),
                                      PersonalizerColumn.builder().attributeIndex(22).keyword("cms:test-attrB").build()
                              )
                      )
                      .build();
    }

    List<Subject> getSubjectsWithSingleCondition() {
        return Collections.singletonList(new Subject(Arrays.asList(0, 1, 2)));
    }

    List<Subject> getSubjectsWithMultiCondition() {
        return Arrays.asList(new Subject(Arrays.asList(0, 1, 2)), new Subject(Arrays.asList(3, 4, 5)));
    }

    List<Content> getMultipartContentsWithSplit1Segment1() {
        val htmlPatterns = Collections.singletonList(new Pattern(Collections.singletonList(Arrays.asList(0, 1, 2))));
        val htmlContent = new Content(true, htmlPatterns);
        val textPatterns = Collections.singletonList(new Pattern(Collections.singletonList(Arrays.asList(3, 4, 5))));
        val textContent = new Content(false, textPatterns);
        return Arrays.asList(htmlContent, textContent);
    }

    List<Content> getTextContentsWithSplit1Segment1() {
        val textPatterns = Collections.singletonList(new Pattern(Collections.singletonList(Arrays.asList(3, 4, 5))));
        val textContent = new Content(false, textPatterns);
        return Collections.singletonList(textContent);
    }

    List<Content> getHtmlContentsWithSplit1Segment1() {
        val htmlPatterns = Collections.singletonList(new Pattern(Collections.singletonList(Arrays.asList(0, 1, 2))));
        val htmlContent = new Content(true, htmlPatterns);
        return Collections.singletonList(htmlContent);
    }

    List<Content> getMultipartContentsWithSplit1Segment2() {
        val htmlPatterns = Arrays.asList(new Pattern(Collections.singletonList(Arrays.asList(0, 1))), new Pattern(Collections.singletonList(Arrays.asList(2, 3))));
        val htmlContent = new Content(true, htmlPatterns);
        val textPatterns = Arrays.asList(new Pattern(Collections.singletonList(Arrays.asList(4, 5))), new Pattern(Collections.singletonList(Arrays.asList(6, 7))));
        val textContent = new Content(false, textPatterns);
        return Arrays.asList(htmlContent, textContent);
    }

    List<Content> getTextContentsWithSplit1Segment2() {
        val textPatterns = Arrays.asList(new Pattern(Collections.singletonList(Arrays.asList(4, 5))), new Pattern(Collections.singletonList(Arrays.asList(6, 7))));
        val textContent = new Content(false, textPatterns);
        return Collections.singletonList(textContent);
    }

    List<Content> getHtmlContentsWithSplit1Segment2() {
        val htmlPatterns = Arrays.asList(new Pattern(Collections.singletonList(Arrays.asList(0, 1))), new Pattern(Collections.singletonList(Arrays.asList(2, 3))));
        val htmlContent = new Content(true, htmlPatterns);
        return Collections.singletonList(htmlContent);
    }

    List<Content> getMultipartContentsWithSplit2Segment1() {
        val htmlPatterns = Collections.singletonList(new Pattern(Arrays.asList(Arrays.asList(0, 1), Arrays.asList(2, 3))));
        val htmlContent = new Content(true, htmlPatterns);
        val textPatterns = Collections.singletonList(new Pattern(Arrays.asList(Arrays.asList(4, 5), Arrays.asList(6, 7))));
        val textContent = new Content(false, textPatterns);
        return Arrays.asList(htmlContent, textContent);
    }

    List<Content> getTextContentsWithSplit2Segment1() {
        val textPatterns = Collections.singletonList(new Pattern(Arrays.asList(Arrays.asList(4, 5), Arrays.asList(6, 7))));
        val textContent = new Content(false, textPatterns);
        return Collections.singletonList(textContent);
    }

    List<Content> getHtmlContentsWithSplit2Segment1() {
        val htmlPatterns = Collections.singletonList(new Pattern(Arrays.asList(Arrays.asList(0, 1), Arrays.asList(2, 3))));
        val htmlContent = new Content(true, htmlPatterns);
        return Collections.singletonList(htmlContent);
    }

    List<Content> getMultipartContentsWithSplit2Segment2() {
        val htmlPatterns = Arrays.asList(
                new Pattern(Arrays.asList(Collections.singletonList(0), Collections.singletonList(1))),
                new Pattern(Arrays.asList(Collections.singletonList(2), Collections.singletonList(3)))
        );
        val htmlContent = new Content(true, htmlPatterns);
        val textPatterns = Arrays.asList(
                new Pattern(Arrays.asList(Collections.singletonList(4), Collections.singletonList(5))),
                new Pattern(Arrays.asList(Collections.singletonList(6), Collections.singletonList(7)))
        );
        val textContent = new Content(false, textPatterns);
        return Arrays.asList(htmlContent, textContent);
    }

    List<Content> getTextContentsWithSplit2Segment2() {
        val textPatterns = Arrays.asList(
                new Pattern(Arrays.asList(Collections.singletonList(4), Collections.singletonList(5))),
                new Pattern(Arrays.asList(Collections.singletonList(6), Collections.singletonList(7)))
        );
        val textContent = new Content(false, textPatterns);
        return Collections.singletonList(textContent);
    }

    List<Content> getHtmlContentsWithSplit2Segment2() {
        val htmlPatterns = Arrays.asList(
                new Pattern(Arrays.asList(Collections.singletonList(0), Collections.singletonList(1))),
                new Pattern(Arrays.asList(Collections.singletonList(2), Collections.singletonList(3)))
        );
        val htmlContent = new Content(true, htmlPatterns);
        return Collections.singletonList(htmlContent);
    }
}
