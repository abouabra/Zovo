package me.abouabra.zovo.services.storage;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class AvatarStorageService {
    private final MinioClient minioClient;
    private final String bucketName;
    private final String endpoint;
    public AvatarStorageService(MinioClient minioClient,
                                @Value("${storage.avatars.bucket-name}") String bucketName,
                                @Value("${storage.endpoint}") String endpoint) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.endpoint = endpoint;
    }
    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    public String getAvatarUrl(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return String.format("%s/%s/%s",
                    endpoint,
                    bucketName,
                    objectName
            );
        } catch (Exception e) {
            return null;
        }
    }

    public void uploadAvatar(String objectName, InputStream data, long size, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(data, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }



    public void deleteAvatar(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }
}

