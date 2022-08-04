package com.rakuten.felix.testsend.manager.web.config;

import com.rakuten.felix.common.storage.FelixStorageConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FelixStorageConfig.class)
public class StorageConfig {

}
