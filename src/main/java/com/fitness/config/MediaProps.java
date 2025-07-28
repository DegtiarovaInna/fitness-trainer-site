package com.fitness.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "media")
public class MediaProps {
    private String bucket;
    private String region;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private Duration presignDuration;
}
