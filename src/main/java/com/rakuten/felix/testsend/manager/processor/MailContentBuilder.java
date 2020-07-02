package com.rakuten.felix.testsend.manager.processor;

import com.rakuten.felix.testsend.manager.webclients.dto.Column;
import com.rakuten.felix.testsend.manager.webclients.dto.Columns;
import com.rakuten.felix.testsend.manager.webclients.dto.Content;
import com.rakuten.felix.testsend.manager.webclients.dto.Pattern;
import com.rakuten.felix.testsend.manager.webclients.dto.PermissionType;
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
// Will be removed when migrating JP -> JM. Job builder will provide these content.
public class MailContentBuilder {
    private static final Integer MU_ATTRIBUTE_INDEX_LINE_NUMBER = 1;
    private static final Integer MU_ATTRIBUTE_INDEX_IDENTIFIER  = 7;
    private static final String MU_ATTRIBUTE_LINE_NUMBER_TAG = "\\$\\{line_number}";
    private static final String EMAGAZINE_UNSUBSCRIBE_TAG = "%%emagazine_unsubscribe_url%%";
    private static final String RMAIL_UNSUBSCRIBE_TAG = "%%rmail_unsubscribe_url%%";
    private static final String EMAGAZINE_UNSUBSCRIBE_URL = "https://emagazine\\.rakuten\\.co\\.jp/nq\\?k=###_ATTRIBUTE6_###";
    private static final String RMAIL_UNSUBSCRIBE_URL = "https://emagazine\\.rakuten\\.co\\.jp/q\\?u=(\\d|\\w){1,20}&k=###_ATTRIBUTE6_###&scid=rm_\\d{1,20}";
    private static final String MU_ATTRIBUTE_IDENTIFIER_TAG = "\\$\\{identifier}";
    private static final java.util.regex.Pattern MU_ATTRIBUTE = java.util.regex.Pattern.compile("###_ATTRIBUTE(\\d+)_###");


    /**
     * Build subject contents from parts and subjects provided by job-data-keeper.
     *
     * @param subjects Subjects.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildSubjectContents(List<Subject> subjects, List<String> parts, Map<Integer, String> replacements, PermissionType permissionType) {
        return subjects.stream()
                       .map(Subject::getPartIds)
                       .flatMap(partIds -> partIds.stream().map(parts::get))
                       .map(revertUnsubscribeTag(permissionType))
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
    public List<String> buildHtmlContents(List<Content> contents, List<String> parts, Map<Integer, String> replacements, PermissionType permissionType) {
        return contents.stream()
                       .filter(Content::getHtml)
                       .findFirst()
                       .map(Content::getPatterns)
                       .map(mapToMergedContents(parts))
                       .map(revertMuAttributesAndUnsubscribeTag(replacements, permissionType))
                       .orElse(Collections.emptyList());
    }

    /**
     * Build text contents from parts and contents provided by job-data-keeper.
     *
     * @param contents Contents.
     * @param parts    Parts.
     * @return Contents of string
     */
    public List<String> buildTextContents(List<Content> contents, List<String> parts, Map<Integer, String> replacements, PermissionType permissionType) {
        return contents.stream()
                       .filter(it -> !it.getHtml())
                       .findFirst()
                       .map(Content::getPatterns)
                       .map(mapToMergedContents(parts))
                       .map(revertMuAttributesAndUnsubscribeTag(replacements, permissionType))
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

    public Map<Integer, String> buildReplacements(Columns columns) {
        val replacements = new HashMap<Integer, String>();
        Optional.ofNullable(columns.getEmail()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getEasyId()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getName()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getUserId()).ifPresent(putAttributes(replacements));
        Optional.ofNullable(columns.getAdditional()).ifPresent(it -> it.forEach(putAttributes(replacements)));
        Optional.ofNullable(columns.getPersonalizer()).ifPresent(it -> it.forEach(putPersonalizeAttributes(replacements)));
        replacements.putIfAbsent(MU_ATTRIBUTE_INDEX_LINE_NUMBER, MU_ATTRIBUTE_LINE_NUMBER_TAG);
        replacements.putIfAbsent(MU_ATTRIBUTE_INDEX_IDENTIFIER, MU_ATTRIBUTE_IDENTIFIER_TAG);
        return replacements;
    }

    private Consumer<Column> putAttributes(Map<Integer, String> replacements) {
        return it -> replacements.putIfAbsent(it.getAttributeIndex(), "%%" + it.getKeyword() + "%%");
    }

    private Consumer<PersonalizerColumn> putPersonalizeAttributes(Map<Integer, String> replacements) {
        return it -> replacements.putIfAbsent(it.getAttributeIndex(), "%%" + it.getKeyword() + "%%");
    }

    private Function<List<String>, List<String>> revertMuAttributesAndUnsubscribeTag(Map<Integer, String> replacements, PermissionType permissionType) {
        return values -> values.stream()
                               .map(revertUnsubscribeTag(permissionType))
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

    private Function<String, String> revertUnsubscribeTag(PermissionType permissionType) {
        return input -> {
            switch (permissionType) {
                case EMAGAZINE:
                    return input.replaceAll(EMAGAZINE_UNSUBSCRIBE_URL, EMAGAZINE_UNSUBSCRIBE_TAG);
                case RMAIL:
                    return input.replaceAll(RMAIL_UNSUBSCRIBE_URL, RMAIL_UNSUBSCRIBE_TAG);
                default:
                    return input;
            }
        };
    }
}
