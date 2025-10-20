package ru.maxb.soulmate.profile.service;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class ObjectStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public ObjectStorageService(MinioClient minioClient,
                                @Value("${minio.bucket.name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @SneakyThrows
    public void saveObject(String objectName, InputStream inputStream) {
        objectName = objectName.trim();
//        try (InputStream is = new FileInputStream(filePath)) {
//            // Get the file size
//            long fileSize = new java.io.File(filePath).length();
//
//
//            PutObjectArgs args = PutObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(objectName)
//                    .stream(is, fileSize, -1) // -1 indicates unknown part size for multipart upload
//                    .contentType("image/jpeg")
//                    .build();
//
//            // Upload the image
//            minioClient.putObject(args);
//
//
//        } catch (MinioException e) {
//           //todo
//        }

    }
}
