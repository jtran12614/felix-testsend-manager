package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.webclients.dto.Content;
import com.rakuten.felix.testsend.manager.webclients.dto.Pattern;
import com.rakuten.felix.testsend.manager.webclients.dto.Subject;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MailContentBuilder {

    /**
     * Build subject contents from parts and subjects provided by job-data-keeper.
     *
     * @param subjects Subjects.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildSubjectContents(List<Subject> subjects, List<String> parts) {
        return subjects.stream()
                       .map(Subject::getPartIds)
                       .map(buildContentFromParts(parts))
                       .collect(Collectors.toList());
    }

    /**
     * Build html contents from parts and contents provided by job-data-keeper.
     *
     * @param contents Contents.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildHtmlContents(List<Content> contents, List<String> parts) {
        return contents.stream()
                       .filter(Content::getHtml)
                       .findFirst()
                       .map(Content::getPatterns)
                       .map(mapToMergedContents(parts))
                       .orElse(Collections.emptyList());
    }

    /**
     * Build text contents from parts and contents provided by job-data-keeper.
     *
     * @param contents Contents.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildTextContents(List<Content> contents, List<String> parts) {
        return contents.stream()
                       .filter(it -> !it.getHtml())
                       .findFirst()
                       .map(Content::getPatterns)
                       .map(mapToMergedContents(parts))
                       .orElse(Collections.emptyList());
    }

    private Function<List<Pattern>, List<String>> mapToMergedContents(List<String> parts) {
        return patterns -> patterns.stream()
                                   .map(Pattern::getPartIds)
                                   .flatMap(partsIds -> partsIds.stream().map(buildContentFromParts(parts)))
                                   .collect(Collectors.toList());
    }

    private Function<List<Integer>, String> buildContentFromParts(List<String> parts) {
        return partIds -> partIds.stream()
                                 .map(parts::get)
                                 .collect(Collectors.joining(""));
    }
}
