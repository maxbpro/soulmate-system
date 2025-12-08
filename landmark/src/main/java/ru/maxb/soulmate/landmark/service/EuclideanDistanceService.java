package ru.maxb.soulmate.landmark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.face.dto.FaceResponseFacesInnerLandmark;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EuclideanDistanceService {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public double compare(FaceResponseFacesInnerLandmark face1, FaceResponseFacesInnerLandmark face2) {
        EuclideanDistance ev = new EuclideanDistance();

        String face1String = objectMapper.writeValueAsString(face1);
        String face2String = objectMapper.writeValueAsString(face2);

        List<Double> landMarks1 = getLandMarks(face1String);
        List<Double> landMarks2 = getLandMarks(face2String);

        double[] array1 = landMarks1.stream().mapToDouble(Double::doubleValue).toArray();
        double[] array2 = landMarks2.stream().mapToDouble(Double::doubleValue).toArray();

        return ev.compute(array1, array2);
    }

    private List<Double> getLandMarks(String face) {
        Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(face);
        List<Double> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        return numbers;
    }
}
