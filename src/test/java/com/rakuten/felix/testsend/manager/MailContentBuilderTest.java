package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.processor.MailContentBuilder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MailContentBuilderTest {
    private MailContentBuilder mailContentBuilder;

    @BeforeEach
    void init() {
        mailContentBuilder = new MailContentBuilder();
    }

    private void assertResult(List<String> expected, List<String> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    void buildHtmlContent_contentIsMultipart_simpleSplit1_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit1Segment1();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Collections.singletonList("Part0Part1Part2");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsMultipart_simpleSplit1_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit1Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0Part1", "Part2Part3");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsMultipart_simpleSplit2_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit2Segment1();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0Part1", "Part2Part3");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsMultipart_simpleSplit2_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0", "Part1", "Part2", "Part3");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsText_simpleSplit1_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit1Segment1();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsText_simpleSplit1_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit1Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsText_simpleSplit2_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit2Segment1();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsText_simpleSplit2_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsHtml_simpleSplit1_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit1Segment1();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Collections.singletonList("Part0Part1Part2");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsHtml_simpleSplit1_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit1Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0Part1", "Part2Part3");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsHtml_simpleSplit2_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit2Segment1();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0Part1", "Part2Part3");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsHtml_simpleSplit2_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0", "Part1", "Part2", "Part3");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsMultipart_simpleSplit1_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit1Segment1();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Collections.singletonList("Part3Part4Part5");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsMultipart_simpleSplit1_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit1Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4Part5", "Part6Part7");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsMultipart_simpleSplit2_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit2Segment1();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4Part5", "Part6Part7");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsMultipart_simpleSplit2_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getMultipartContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4", "Part5", "Part6", "Part7");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsText_simpleSplit1_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit1Segment1();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Collections.singletonList("Part3Part4Part5");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsText_simpleSplit1_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit1Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4Part5", "Part6Part7");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsText_simpleSplit2_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit2Segment1();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4Part5", "Part6Part7");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsText_simpleSplit2_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4", "Part5", "Part6", "Part7");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsHtml_simpleSplit1_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit1Segment1();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsHtml_simpleSplit1_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit1Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsHtml_simpleSplit2_segmentSplit1() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit2Segment1();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsHtml_simpleSplit2_segmentSplit2() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = new ArrayList<String>();
        assertResult(expected, result);
    }

    @Test
    void buildSubjects_singleCondition() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val subjects = FakeData.getSubjectsWithSingleCondition();
        val result = mailContentBuilder.buildSubjectContents(subjects, parts, columns);
        val expected = Arrays.asList("Part0", "Part1", "Part2");
        assertResult(expected, result);
    }

    @Test
    void buildSubjects_multiConditions() {
        val parts = FakeData.getParts();
        val columns = FakeData.getColumns();
        val subjects = FakeData.getSubjectsWithMultiCondition();
        val result = mailContentBuilder.buildSubjectContents(subjects, parts, columns);
        val expected = Arrays.asList("Part0", "Part1", "Part2", "Part3", "Part4", "Part5");
        assertResult(expected, result);
    }

    @Test
    void buildHtmlContent_contentIsHtml_simpleSplit2_segmentSplit2_hasMuAttributes() {
        val parts = FakeData.getPartsWithMuAttributes();
        val columns = FakeData.getColumns();
        val contents = FakeData.getHtmlContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildHtmlContents(contents, parts, columns);
        val expected = Arrays.asList("Part0:###_ATTRIBUTE0_###", "Part1:%%email%%", "Part2:%%name%%", "Part3:%%easyId%%%%addA%%");
        assertResult(expected, result);
    }

    @Test
    void buildTextContent_contentIsText_simpleSplit2_segmentSplit2_haMuAttributes() {
        val parts = FakeData.getPartsWithMuAttributes();
        val columns = FakeData.getColumns();
        val contents = FakeData.getTextContentsWithSplit2Segment2();
        val result = mailContentBuilder.buildTextContents(contents, parts, columns);
        val expected = Arrays.asList("Part4:%%userId%%,%%addB%%", "Part5:%%addA%%", "Part6:%%cms:test-attrA%%", "Part7:###_ATTRI%%email%%BUTE22_###");
        assertResult(expected, result);
    }

    @Test
    void buildSubjects_multiConditionsAndHasMuAttributes() {
        val parts = FakeData.getPartsWithMuAttributes();
        val columns = FakeData.getColumns();
        val subjects = FakeData.getSubjectsWithMultiCondition();
        val result = mailContentBuilder.buildSubjectContents(subjects, parts, columns);
        val expected = Arrays.asList("Part0:###_ATTRIBUTE0_###", "Part1:%%email%%", "Part2:%%name%%", "Part3:%%easyId%%%%addA%%", "Part4:%%userId%%,%%addB%%", "Part5:%%addA%%");
        assertResult(expected, result);
    }
}