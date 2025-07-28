package com.fitness.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(MediaProps.class)
@RequiredArgsConstructor
public class S3Config {
    private final MediaProps props;

    @Bean
    public S3Client s3Client() {
        S3Configuration s3Conf = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        S3ClientBuilder clientBuilder = S3Client.builder()
                .serviceConfiguration(s3Conf)
                .region(Region.of(props.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        props.getAccessKey(),
                                        props.getSecretKey()
                                )
                        )
                );

        if (props.getEndpoint() != null && !props.getEndpoint().isEmpty()) {
            clientBuilder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return clientBuilder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Configuration s3Conf = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        Builder presignerBuilder = S3Presigner.builder()
                .serviceConfiguration(s3Conf)
                .region(Region.of(props.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        props.getAccessKey(),
                                        props.getSecretKey()
                                )
                        )
                );

        if (props.getEndpoint() != null && !props.getEndpoint().isEmpty()) {
            presignerBuilder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return presignerBuilder.build();
    }
}
