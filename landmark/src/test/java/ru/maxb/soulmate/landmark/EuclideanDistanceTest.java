package ru.maxb.soulmate.landmark;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class EuclideanDistanceTest {

    @Test
    public void testEuclideanDistanceEquals() {
        EuclideanDistance ev = new EuclideanDistance();

        double[] line1 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        double[] line2 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

        double distance = ev.compute(line1, line2);

        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    public void testEuclideanDistance() {
        EuclideanDistance ev = new EuclideanDistance();

        List<Double> landMarks1 = getLandMarks(FACE1);
        List<Double> landMarks2 = getLandMarks(FACE2);

        double[] array1 = landMarks1.stream().mapToDouble(Double::doubleValue).toArray();
        double[] array2 = landMarks2.stream().mapToDouble(Double::doubleValue).toArray();

        double distance = ev.compute(array1, array2);

        assertThat(distance).isEqualTo(2919.8388654170626);
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

    private static final String FACE1 = """
            {
              "faces": [
                {
                  "landmark": {
                    "contour_chin": {
                      "x": 536,
                      "y": 544
                    },
                    "contour_left1": {
                      "x": 508,
                      "y": 422
                    },
                    "contour_left2": {
                      "x": 507,
                      "y": 435
                    },
                    "contour_left3": {
                      "x": 505,
                      "y": 449
                    },
                    "contour_left4": {
                      "x": 506,
                      "y": 464
                    },
                    "contour_left5": {
                      "x": 507,
                      "y": 479
                    },
                    "contour_left6": {
                      "x": 510,
                      "y": 493
                    },
                    "contour_left7": {
                      "x": 513,
                      "y": 508
                    },
                    "contour_left8": {
                      "x": 518,
                      "y": 522
                    },
                    "contour_left9": {
                      "x": 524,
                      "y": 536
                    },
                    "contour_right1": {
                      "x": 664,
                      "y": 415
                    },
                    "contour_right2": {
                      "x": 665,
                      "y": 438
                    },
                    "contour_right3": {
                      "x": 663,
                      "y": 460
                    },
                    "contour_right4": {
                      "x": 658,
                      "y": 483
                    },
                    "contour_right5": {
                      "x": 647,
                      "y": 503
                    },
                    "contour_right6": {
                      "x": 630,
                      "y": 519
                    },
                    "contour_right7": {
                      "x": 609,
                      "y": 530
                    },
                    "contour_right8": {
                      "x": 585,
                      "y": 539
                    },
                    "contour_right9": {
                      "x": 560,
                      "y": 544
                    },
                    "left_eye_bottom": {
                      "x": 519,
                      "y": 419
                    },
                    "left_eye_center": {
                      "x": 519,
                      "y": 415
                    },
                    "left_eye_left_corner": {
                      "x": 508,
                      "y": 416
                    },
                    "left_eye_lower_left_quarter": {
                      "x": 513,
                      "y": 418
                    },
                    "left_eye_lower_right_quarter": {
                      "x": 525,
                      "y": 418
                    },
                    "left_eye_pupil": {
                      "x": 518,
                      "y": 413
                    },
                    "left_eye_right_corner": {
                      "x": 530,
                      "y": 415
                    },
                    "left_eye_top": {
                      "x": 518,
                      "y": 409
                    },
                    "left_eye_upper_left_quarter": {
                      "x": 512,
                      "y": 411
                    },
                    "left_eye_upper_right_quarter": {
                      "x": 524,
                      "y": 411
                    },
                    "left_eyebrow_left_corner": {
                      "x": 500,
                      "y": 395
                    },
                    "left_eyebrow_lower_left_quarter": {
                      "x": 505,
                      "y": 393
                    },
                    "left_eyebrow_lower_middle": {
                      "x": 512,
                      "y": 394
                    },
                    "left_eyebrow_lower_right_quarter": {
                      "x": 520,
                      "y": 395
                    },
                    "left_eyebrow_right_corner": {
                      "x": 529,
                      "y": 396
                    },
                    "left_eyebrow_upper_left_quarter": {
                      "x": 504,
                      "y": 387
                    },
                    "left_eyebrow_upper_middle": {
                      "x": 512,
                      "y": 386
                    },
                    "left_eyebrow_upper_right_quarter": {
                      "x": 522,
                      "y": 388
                    },
                    "mouth_left_corner": {
                      "x": 521,
                      "y": 498
                    },
                    "mouth_lower_lip_bottom": {
                      "x": 533,
                      "y": 506
                    },
                    "mouth_lower_lip_left_contour1": {
                      "x": 526,
                      "y": 495
                    },
                    "mouth_lower_lip_left_contour2": {
                      "x": 523,
                      "y": 502
                    },
                    "mouth_lower_lip_left_contour3": {
                      "x": 527,
                      "y": 505
                    },
                    "mouth_lower_lip_right_contour1": {
                      "x": 548,
                      "y": 495
                    },
                    "mouth_lower_lip_right_contour2": {
                      "x": 556,
                      "y": 501
                    },
                    "mouth_lower_lip_right_contour3": {
                      "x": 545,
                      "y": 505
                    },
                    "mouth_lower_lip_top": {
                      "x": 533,
                      "y": 495
                    },
                    "mouth_right_corner": {
                      "x": 566,
                      "y": 496
                    },
                    "mouth_upper_lip_bottom": {
                      "x": 531,
                      "y": 490
                    },
                    "mouth_upper_lip_left_contour1": {
                      "x": 524,
                      "y": 481
                    },
                    "mouth_upper_lip_left_contour2": {
                      "x": 519,
                      "y": 488
                    },
                    "mouth_upper_lip_left_contour3": {
                      "x": 525,
                      "y": 492
                    },
                    "mouth_upper_lip_right_contour1": {
                      "x": 536,
                      "y": 480
                    },
                    "mouth_upper_lip_right_contour2": {
                      "x": 551,
                      "y": 486
                    },
                    "mouth_upper_lip_right_contour3": {
                      "x": 547,
                      "y": 491
                    },
                    "mouth_upper_lip_top": {
                      "x": 530,
                      "y": 481
                    },
                    "nose_contour_left1": {
                      "x": 527,
                      "y": 414
                    },
                    "nose_contour_left2": {
                      "x": 515,
                      "y": 439
                    },
                    "nose_contour_left3": {
                      "x": 516,
                      "y": 459
                    },
                    "nose_contour_lower_middle": {
                      "x": 525,
                      "y": 464
                    },
                    "nose_contour_right1": {
                      "x": 552,
                      "y": 414
                    },
                    "nose_contour_right2": {
                      "x": 549,
                      "y": 444
                    },
                    "nose_contour_right3": {
                      "x": 539,
                      "y": 463
                    },
                    "nose_left": {
                      "x": 510,
                      "y": 453
                    },
                    "nose_right": {
                      "x": 554,
                      "y": 459
                    },
                    "nose_tip": {
                      "x": 518,
                      "y": 446
                    },
                    "right_eye_bottom": {
                      "x": 580,
                      "y": 416
                    },
                    "right_eye_center": {
                      "x": 580,
                      "y": 411
                    },
                    "right_eye_left_corner": {
                      "x": 563,
                      "y": 414
                    },
                    "right_eye_lower_left_quarter": {
                      "x": 571,
                      "y": 415
                    },
                    "right_eye_lower_right_quarter": {
                      "x": 589,
                      "y": 414
                    },
                    "right_eye_pupil": {
                      "x": 577,
                      "y": 408
                    },
                    "right_eye_right_corner": {
                      "x": 596,
                      "y": 410
                    },
                    "right_eye_top": {
                      "x": 579,
                      "y": 403
                    },
                    "right_eye_upper_left_quarter": {
                      "x": 570,
                      "y": 406
                    },
                    "right_eye_upper_right_quarter": {
                      "x": 589,
                      "y": 405
                    },
                    "right_eyebrow_left_corner": {
                      "x": 553,
                      "y": 392
                    },
                    "right_eyebrow_lower_left_quarter": {
                      "x": 568,
                      "y": 392
                    },
                    "right_eyebrow_lower_middle": {
                      "x": 582,
                      "y": 389
                    },
                    "right_eyebrow_lower_right_quarter": {
                      "x": 597,
                      "y": 390
                    },
                    "right_eyebrow_right_corner": {
                      "x": 612,
                      "y": 393
                    },
                    "right_eyebrow_upper_left_quarter": {
                      "x": 565,
                      "y": 382
                    },
                    "right_eyebrow_upper_middle": {
                      "x": 582,
                      "y": 380
                    },
                    "right_eyebrow_upper_right_quarter": {
                      "x": 598,
                      "y": 383
                    }
                  }
                }
              ]
            }
            """;


    private static final String FACE2 = """
            {
              "faces": [
                  "landmark": {
                    "contour_chin": {
                      "x": 259,
                      "y": 416
                    },
                    "contour_left1": {
                      "x": 165,
                      "y": 291
                    },
                    "contour_left2": {
                      "x": 168,
                      "y": 309
                    },
                    "contour_left3": {
                      "x": 172,
                      "y": 327
                    },
                    "contour_left4": {
                      "x": 177,
                      "y": 345
                    },
                    "contour_left5": {
                      "x": 183,
                      "y": 362
                    },
                    "contour_left6": {
                      "x": 192,
                      "y": 378
                    },
                    "contour_left7": {
                      "x": 205,
                      "y": 392
                    },
                    "contour_left8": {
                      "x": 220,
                      "y": 404
                    },
                    "contour_left9": {
                      "x": 237,
                      "y": 413
                    },
                    "contour_right1": {
                      "x": 334,
                      "y": 279
                    },
                    "contour_right2": {
                      "x": 334,
                      "y": 298
                    },
                    "contour_right3": {
                      "x": 332,
                      "y": 316
                    },
                    "contour_right4": {
                      "x": 329,
                      "y": 334
                    },
                    "contour_right5": {
                      "x": 326,
                      "y": 353
                    },
                    "contour_right6": {
                      "x": 319,
                      "y": 370
                    },
                    "contour_right7": {
                      "x": 309,
                      "y": 385
                    },
                    "contour_right8": {
                      "x": 295,
                      "y": 399
                    },
                    "contour_right9": {
                      "x": 279,
                      "y": 410
                    },
                    "left_eye_bottom": {
                      "x": 211,
                      "y": 296
                    },
                    "left_eye_center": {
                      "x": 212,
                      "y": 290
                    },
                    "left_eye_left_corner": {
                      "x": 197,
                      "y": 288
                    },
                    "left_eye_lower_left_quarter": {
                      "x": 202,
                      "y": 293
                    },
                    "left_eye_lower_right_quarter": {
                      "x": 221,
                      "y": 295
                    },
                    "left_eye_pupil": {
                      "x": 212,
                      "y": 288
                    },
                    "left_eye_right_corner": {
                      "x": 229,
                      "y": 293
                    },
                    "left_eye_top": {
                      "x": 213,
                      "y": 281
                    },
                    "left_eye_upper_left_quarter": {
                      "x": 203,
                      "y": 283
                    },
                    "left_eye_upper_right_quarter": {
                      "x": 222,
                      "y": 285
                    },
                    "left_eyebrow_left_corner": {
                      "x": 184,
                      "y": 266
                    },
                    "left_eyebrow_lower_left_quarter": {
                      "x": 197,
                      "y": 265
                    },
                    "left_eyebrow_lower_middle": {
                      "x": 209,
                      "y": 266
                    },
                    "left_eyebrow_lower_right_quarter": {
                      "x": 221,
                      "y": 268
                    },
                    "left_eyebrow_right_corner": {
                      "x": 234,
                      "y": 269
                    },
                    "left_eyebrow_upper_left_quarter": {
                      "x": 196,
                      "y": 259
                    },
                    "left_eyebrow_upper_middle": {
                      "x": 210,
                      "y": 258
                    },
                    "left_eyebrow_upper_right_quarter": {
                      "x": 223,
                      "y": 261
                    },
                    "mouth_left_corner": {
                      "x": 228,
                      "y": 373
                    },
                    "mouth_lower_lip_bottom": {
                      "x": 256,
                      "y": 385
                    },
                    "mouth_lower_lip_left_contour1": {
                      "x": 242,
                      "y": 373
                    },
                    "mouth_lower_lip_left_contour2": {
                      "x": 236,
                      "y": 379
                    },
                    "mouth_lower_lip_left_contour3": {
                      "x": 245,
                      "y": 384
                    },
                    "mouth_lower_lip_right_contour1": {
                      "x": 269,
                      "y": 372
                    },
                    "mouth_lower_lip_right_contour2": {
                      "x": 276,
                      "y": 378
                    },
                    "mouth_lower_lip_right_contour3": {
                      "x": 267,
                      "y": 383
                    },
                    "mouth_lower_lip_top": {
                      "x": 255,
                      "y": 374
                    },
                    "mouth_right_corner": {
                      "x": 282,
                      "y": 372
                    },
                    "mouth_upper_lip_bottom": {
                      "x": 255,
                      "y": 373
                    },
                    "mouth_upper_lip_left_contour1": {
                      "x": 248,
                      "y": 365
                    },
                    "mouth_upper_lip_left_contour2": {
                      "x": 237,
                      "y": 368
                    },
                    "mouth_upper_lip_left_contour3": {
                      "x": 242,
                      "y": 373
                    },
                    "mouth_upper_lip_right_contour1": {
                      "x": 262,
                      "y": 364
                    },
                    "mouth_upper_lip_right_contour2": {
                      "x": 273,
                      "y": 366
                    },
                    "mouth_upper_lip_right_contour3": {
                      "x": 269,
                      "y": 372
                    },
                    "mouth_upper_lip_top": {
                      "x": 255,
                      "y": 366
                    },
                    "nose_contour_left1": {
                      "x": 240,
                      "y": 292
                    },
                    "nose_contour_left2": {
                      "x": 236,
                      "y": 327
                    },
                    "nose_contour_left3": {
                      "x": 242,
                      "y": 347
                    },
                    "nose_contour_lower_middle": {
                      "x": 255,
                      "y": 350
                    },
                    "nose_contour_right1": {
                      "x": 266,
                      "y": 291
                    },
                    "nose_contour_right2": {
                      "x": 273,
                      "y": 326
                    },
                    "nose_contour_right3": {
                      "x": 267,
                      "y": 346
                    },
                    "nose_left": {
                      "x": 232,
                      "y": 341
                    },
                    "nose_right": {
                      "x": 278,
                      "y": 340
                    },
                    "nose_tip": {
                      "x": 255,
                      "y": 338
                    },
                    "right_eye_bottom": {
                      "x": 294,
                      "y": 292
                    },
                    "right_eye_center": {
                      "x": 292,
                      "y": 286
                    },
                    "right_eye_left_corner": {
                      "x": 276,
                      "y": 291
                    },
                    "right_eye_lower_left_quarter": {
                      "x": 285,
                      "y": 292
                    },
                    "right_eye_lower_right_quarter": {
                      "x": 302,
                      "y": 288
                    },
                    "right_eye_pupil": {
                      "x": 290,
                      "y": 286
                    },
                    "right_eye_right_corner": {
                      "x": 307,
                      "y": 282
                    },
                    "right_eye_top": {
                      "x": 292,
                      "y": 278
                    },
                    "right_eye_upper_left_quarter": {
                      "x": 282,
                      "y": 282
                    },
                    "right_eye_upper_right_quarter": {
                      "x": 301,
                      "y": 279
                    },
                    "right_eyebrow_left_corner": {
                      "x": 272,
                      "y": 268
                    },
                    "right_eyebrow_lower_left_quarter": {
                      "x": 285,
                      "y": 264
                    },
                    "right_eyebrow_lower_middle": {
                      "x": 296,
                      "y": 261
                    },
                    "right_eyebrow_lower_right_quarter": {
                      "x": 308,
                      "y": 260
                    },
                    "right_eyebrow_right_corner": {
                      "x": 320,
                      "y": 260
                    },
                    "right_eyebrow_upper_left_quarter": {
                      "x": 282,
                      "y": 259
                    },
                    "right_eyebrow_upper_middle": {
                      "x": 295,
                      "y": 254
                    },
                    "right_eyebrow_upper_right_quarter": {
                      "x": 309,
                      "y": 253
                    }
                  }
                }
              ]
            }
            """;
}

