package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.webclients.dto.Column;
import com.rakuten.felix.testsend.manager.webclients.dto.Columns;
import com.rakuten.felix.testsend.manager.webclients.dto.Content;
import com.rakuten.felix.testsend.manager.webclients.dto.Pattern;
import com.rakuten.felix.testsend.manager.webclients.dto.PersonalizerColumn;
import com.rakuten.felix.testsend.manager.webclients.dto.Subject;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MailContentBuilder {
    private static final java.util.regex.Pattern MU_ATTRIBUTE = java.util.regex.Pattern.compile("###_ATTRIBUTE(\\d+)_###");


    /**
     * Build subject contents from parts and subjects provided by job-data-keeper.
     *
     * @param subjects Subjects.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildSubjectContents(List<Subject> subjects, List<String> parts, Columns columns) {
        val replacements = buildReplacements(columns);
        return subjects.stream()
                       .map(Subject::getPartIds)
                       .flatMap(partIds -> partIds.stream().map(parts::get))
                       .map(revertMuAttribute(replacements))
                       .collect(Collectors.toList());
    }

    /**
     * Build html contents from parts and contents provided by job-data-keeper.
     *
     * @param contents Contents.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildHtmlContents(List<Content> contents, List<String> parts, Columns columns) {
        val replacements = buildReplacements(columns);
        return contents.stream()
                       .filter(Content::getHtml)
                       .findFirst()
                       .map(Content::getPatterns)
                       .map(mapToMergedContents(parts))
                       .map(revertMuAttributes(replacements))
                       .orElse(Collections.emptyList());
    }

    /**
     * Build text contents from parts and contents provided by job-data-keeper.
     *
     * @param contents Contents.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildTextContents(List<Content> contents, List<String> parts, Columns columns) {
        val replacements = buildReplacements(columns);
        return contents.stream()
                       .filter(it -> !it.getHtml())
                       .findFirst()
                       .map(Content::getPatterns)
                       .map(mapToMergedContents(parts))
                       .map(revertMuAttributes(replacements))
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

    private Map<Integer, String> buildReplacements(Columns columns) {
        val replacements = new HashMap<Integer, String>();
        Optional.ofNullable(columns.getEmail()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getEasyId()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getName()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getUserId()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getAdditional()).ifPresent(it -> it.forEach(putAttributes(replacements)));
        Optional.ofNullable(columns.getPersonalizer()).ifPresent(it -> it.forEach(putPersonalizeAttributes(replacements)));
        return replacements;
    }

    private Consumer<Column> putAttributes(Map<Integer, String> replacements) {
        return it -> replacements.putIfAbsent(it.getAttributeIndex(), "%%" + it.getKeyword() + "%%");
    }

    private Consumer<PersonalizerColumn> putPersonalizeAttributes(Map<Integer, String> replacements) {
        return it -> replacements.putIfAbsent(it.getAttributeIndex(), "%%" + it.getKeyword() + "%%");
    }

    private Function<List<String>, List<String>> revertMuAttributes(Map<Integer, String> replacements) {
        return values -> values.stream()
                               .map(revertMuAttribute(replacements))
                               .collect(Collectors.toList());
    }

    private Function<String, String> revertMuAttribute(Map<Integer, String> replacements) {
        return input -> {
            val output = new StringBuffer();
            val matcher = MU_ATTRIBUTE.matcher(input);
            while (matcher.find()) {
                val replacement = replacements.get(Integer.valueOf(matcher.group(1)));
                matcher.appendReplacement(output, replacement == null ? matcher.group() : replacement);
            }
            matcher.appendTail(output);
            return output.toString();
        };
    }
}
