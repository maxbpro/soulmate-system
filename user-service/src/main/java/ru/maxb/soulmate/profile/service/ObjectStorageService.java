package ru.maxb.soulmate.profile.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.exception.ProfileException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final MinioClient minioClient;

    public void saveObject(UUID profileId, UUID photoId, MultipartFile file) {
        String bucketName = getBucketName(profileId);
        checkBucket(bucketName);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(String.valueOf(photoId))
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException ex) {
            log.error("An error occurred in saving in Object Storage: {}", ex.getMessage());
            throw new ProfileException("Can not save object in Object Storage: " + ex.getMessage());
        }
    }


    private void checkBucket(String bucketName) {
        try {
            boolean found = false;

            found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!found) {
                //todo lock?
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException ex) {
            log.error("An error occurred in Object Storage: {}", ex.getMessage());
            throw new ProfileException("Can not create a bucket: " + ex.getMessage());
        }
    }

    public InputStream findObject(UUID profileId, UUID photoId) {
        String bucketName = getBucketName(profileId);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(String.valueOf(photoId))
                    .build());

        } catch (Exception ex) {
            log.error("An error occurred in searching in Object Storage: {}", ex.getMessage());
            throw new ProfileException(MessageFormat.format("Can not find a photo: {0} for profile: {1}", photoId, profileId));
        }
    }

    public void deleteObject(UUID profileId, UUID photoId) {
        String bucketName = getBucketName(profileId);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(String.valueOf(photoId))
                            .build());

        } catch (Exception ex) {
            log.error("An error occurred in removing in Object Storage: {}", ex.getMessage());
            throw new ProfileException(MessageFormat.format("Can not remove a photo: {0} for profile: {1}", photoId, profileId));
        }
    }

    public List<String> listObjects(UUID profileId) throws Exception {
        String bucketName = getBucketName(profileId);
        List<String> objectNames = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());
        for (Result<Item> result : results) {
            objectNames.add(result.get().objectName());
        }
        return objectNames;
    }

    private String getBucketName(UUID profileId) {
        return "bucket" + profileId;
    }
}
