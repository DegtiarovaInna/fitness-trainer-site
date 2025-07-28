package com.fitness.services.impl;

import com.fitness.config.MediaProps;
import com.fitness.services.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaProps props;
    private final S3Presigner presigner;


    @Override
    public URL generateUploadUrl(String key) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType("image/jpeg")
                .build();

        return presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .putObjectRequest(putReq)
                        .signatureDuration(props.getPresignDuration())
                        .build()
        ).url();
    }

    @Override
    public URL generateDownloadUrl(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build();

        return presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .getObjectRequest(getReq)
                        .signatureDuration(props.getPresignDuration())
                        .build()
        ).url();
    }
}
