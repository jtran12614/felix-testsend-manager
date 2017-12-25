package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.datastore.entities.Info;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus;
import com.rakuten.felix.testsend.manager.webclients.dto.Content;
import com.rakuten.felix.testsend.manager.webclients.dto.MailJob;
import com.rakuten.felix.testsend.manager.webclients.dto.Pattern;
import com.rakuten.felix.testsend.manager.webclients.dto.Schedule;
import com.rakuten.felix.testsend.manager.webclients.dto.Subject;
import com.rakuten.felix.testsend.manager.webclients.dto.User;
import lombok.experimental.UtilityClass;
import lombok.val;

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
                .info(Info.builder()
                        .subjects(Collections.singletonList("Subject"))
                        .htmlContents(Collections.singletonList("Html content"))
                        .textContents(Collections.singletonList("Text content"))
                        .user(new User(1, "user-address@rakuten.com"))
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

    MailJob getEmptyMailJob() {
        return new MailJob(
                Collections.singletonList(new Schedule(Collections.emptyList(), Collections.emptyList())),
                Collections.singletonList(""),
                new User(0, ""),
                Collections.emptyList());
    }


    // Mail Content Builder Pattern:: SimpleSplit * Segment Split

    List<String> getParts() {
        return Arrays.asList("Part0", "Part1", "Part2", "Part3", "Part4", "Part5", "Part6", "Part7");
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
