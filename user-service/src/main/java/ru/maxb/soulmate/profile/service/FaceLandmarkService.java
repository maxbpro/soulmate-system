package ru.maxb.soulmate.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.face.api.FaceApiClient;
import ru.maxb.soulmate.face.dto.FaceRequest;
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

    @SneakyThrows
    public FaceResponseFacesInnerLandmark getLandmarks(String base64Image) {
        FaceRequest faceRequest = new FaceRequest();
        faceRequest.setImageBase64(base64Image);
        faceRequest.setReturnLandmark(1);

        ResponseEntity<FaceResponse> faceResponseResponseEntity = FaceApiClient.faceDetection(key, secret,
                faceRequest, 1);

        return Optional.ofNullable(faceResponseResponseEntity.getBody())
                .map(FaceResponse::getFaces)
                .map(v -> v.stream().findFirst().orElseThrow())
                .map(FaceResponseFacesInner::getLandmark)
                .orElseThrow(() -> new ProfileException("Face API response has issues"));
    }
}
