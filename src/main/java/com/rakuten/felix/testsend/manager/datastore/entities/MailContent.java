package com.rakuten.felix.testsend.manager.datastore.entities;

import lombok.Value;

import java.util.List;

@Value
public class MailContent {
    Integer scheduleId;
    List<Integer> deviceCodes;
    List<String> subjects;
    List<String> text;
    List<String> html;
}
