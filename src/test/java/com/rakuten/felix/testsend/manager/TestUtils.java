package com.rakuten.felix.testsend.manager;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class TestUtils {
    @SneakyThrows
    public static String readFileToString(String path) {
        val resource = new ClassPathResource(path);
        val inputStream = resource.getInputStream();
        byte[] fileBytes = FileCopyUtils.copyToByteArray(inputStream);
        return new String(fileBytes, StandardCharsets.UTF_8);
    }
}
