package ru.maxb.soulmate.profile.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.dto.PhotoObjectDto;
import ru.maxb.soulmate.profile.exception.ProfileException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class ObjectStorageService {

    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(10);
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");

    private final MinioClient minioClient;
    private final String endpoint;

    public ObjectStorageService(MinioClient minioClient, @Value("${minio.endpoint}") String endpoint) {
        this.minioClient = minioClient;
        this.endpoint = endpoint;
    }

    public PhotoObjectDto saveObject(UUID profileId, UUID photoId, MultipartFile file) {
        validateFile(file);
        String bucketName = getBucketName(profileId);
        checkBucket(bucketName);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(String.valueOf(photoId))
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(createMetadata(file))
                    .build());

            log.info("Saved object {} in bucket {}", photoId, bucketName);

            return new PhotoObjectDto(photoId,
                    String.format("%s/%s/%s", endpoint, bucketName, photoId));

        } catch (Exception ex) {
            log.error("Error saving object {} in bucket {}", photoId, bucketName, ex);
            throw new ProfileException("Cannot save object: " + ex.getMessage());
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
                log.info("Created bucket: {}", bucketName);
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException ex) {
            log.error("Error checking/creating bucket: {}", bucketName, ex);
            throw new ProfileException("Cannot create bucket: " + ex.getMessage());
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
            log.error("Error retrieving object {} from bucket {}", photoId, bucketName, ex);
            throw new ProfileException("Cannot retrieve photo");
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

    public List<String> listObjects(UUID profileId) {
        String bucketName = getBucketName(profileId);
        List<String> objectNames = new ArrayList<>();

        try {

            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .build());
            for (Result<Item> result : results) {
                objectNames.add(result.get().objectName());
            }
            return objectNames;
        } catch (Exception ex) {
            log.error("Error listing objects in bucket: {}", bucketName, ex);
            throw new ProfileException("Cannot list objects: " + ex.getMessage());
        }
    }

    public void deleteBucket(UUID profileId) {
        String bucketName = getBucketName(profileId);

        try {
            List<String> objects = listObjects(profileId);
            for (String objectName : objects) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
            }

            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());

            log.info("Deleted bucket: {}", bucketName);

        } catch (Exception ex) {
            log.error("Error deleting bucket: {}", bucketName, ex);
            throw new ProfileException("Cannot delete bucket");
        }
    }

    private String getBucketName(UUID profileId) {
        return "profile" + profileId.toString().replace("-", "");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProfileException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE.toBytes()) {
            throw new ProfileException("File size exceeds limit of " + MAX_FILE_SIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ProfileException("Unsupported file type: " + contentType);
        }
    }

    private Map<String, String> createMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", file.getOriginalFilename());
        metadata.put("uploaded-at", Instant.now().toString());
        metadata.put("size", String.valueOf(file.getSize()));
        metadata.put("content-type", file.getContentType());
        return metadata;
    }
}
