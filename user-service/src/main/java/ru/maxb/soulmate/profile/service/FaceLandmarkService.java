package ru.maxb.soulmate.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.face.api.FaceApiClient;
import ru.maxb.soulmate.face.dto.FaceResponse;
import ru.maxb.soulmate.face.dto.FaceResponseFacesInner;
import ru.maxb.soulmate.face.dto.FaceResponseFacesInnerLandmark;
import ru.maxb.soulmate.profile.exception.ProfileException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceLandmarkService {

    private final FaceApiClient FaceApiClient;

    @Value("${face.key}")
    private String key;

    @Value("${face.secret}")
    private String secret;

    public void getLandmarks(MultipartFile file) {
        ResponseEntity<FaceResponse> faceResponseResponseEntity = FaceApiClient.faceDetection(key, secret, file, 1);

        FaceResponseFacesInnerLandmark landmarksDto = Optional.ofNullable(faceResponseResponseEntity.getBody())
                .map(FaceResponse::getFaces)
                .map(v -> v.stream().findFirst().orElseThrow())
                .map(FaceResponseFacesInner::getLandmark)
                .orElseThrow(() -> new ProfileException("Face API response has issues"));


        landmarksDto.get
    }
}
