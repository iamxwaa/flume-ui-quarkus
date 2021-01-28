package org.github.iamxwaa.config;

import java.util.Map;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;

@Data
@ConfigProperties(prefix = "tourist")
public class TouristConfig {

    private Map<String, String> sinkMap;

    private Map<String, String> channelMap;

    private Map<String, String> sourceMap;

    private Map<String, String> interceptorMap;

    private Map<String, String> componentMap;

}
